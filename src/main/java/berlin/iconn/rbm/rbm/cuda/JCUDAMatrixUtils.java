package berlin.iconn.rbm.rbm.cuda;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas;
import jcuda.jcublas.JCublas2;
import jcuda.jcublas.cublasHandle;
import jcuda.jcublas.cublasOperation;
import jcuda.jcublas.cublasPointerMode;
import jcuda.runtime.JCuda;
import jcuda.runtime.cudaDeviceProp;
import org.jblas.FloatMatrix;


/**
 * Matrix utilities for CUDA graphics card greater version 400, e.g. Nvidia
 * 480gtx.
 *
 * <br/>
 * -Djava.library.path="/lib/;${env_var:PATH}" must be added to the running VM.
 * If you have the cuda libs in the /lib folder or under windows in your path
 * variables.
 *
 * @author thomas.jungblut
 *
 */
public final class JCUDAMatrixUtils {

    public static boolean EXCEPTIONS_ENABLED = false;
    public static boolean CUBLAS2_AVAILABLE = false;

    private static cublasHandle handle;

    static {
        try {
            JCuda.setExceptionsEnabled(EXCEPTIONS_ENABLED);
            cudaDeviceProp cudaDeviceProp = new cudaDeviceProp();
            JCuda.cudaGetDeviceProperties(cudaDeviceProp, 0);
            // verify that compute capability of 1.3 is available, because only
            // here is the float precision operation allowed.
            if (cudaDeviceProp.major <= 1 && cudaDeviceProp.minor < 3) {
                throw new IllegalArgumentException(
                        "WARN Float precision computing only allowed since capability 1.3! You have "
                        + cudaDeviceProp.major
                        + "."
                        + cudaDeviceProp.minor
                        + "! If you have exceptions turned off, then this may result in strange behaviour.");
            }
            // actually here is only cublas2 available.
            if (Integer.parseInt(cudaDeviceProp.getName().replaceAll("[^\\d]", "")) > 400) {
                JCublas2.setExceptionsEnabled(EXCEPTIONS_ENABLED);
                JCublas2.initialize();
                CUBLAS2_AVAILABLE = true;
                handle = new cublasHandle();
                JCublas2.cublasCreate(handle);
                JCublas2.cublasSetPointerMode(handle, cublasPointerMode.CUBLAS_POINTER_MODE_HOST);
            } else {
                JCublas.setExceptionsEnabled(EXCEPTIONS_ENABLED);
                JCublas.cublasInit();
            }

            // cleanup that handle at the end of this process
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    JCUDAMatrixUtils.cublasDestroy(handle);
                }
            });

            System.out.println("Using device " + cudaDeviceProp.getName()
                    + " with total RAM of "
                    + humanReadableByteCount(cudaDeviceProp.totalGlobalMem, false)
                    + ". Compute capability: " + cudaDeviceProp.major + "."
                    + cudaDeviceProp.minor);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Multiplies matrix A with matrix B and returns a new matrix.
     */
    public static FloatMatrix multiply(FloatMatrix a, FloatMatrix b) {
        return multiply(a, b, false, false);
    }

    /**
     * Multiplies matrix A with matrix B (these are pointers, thus the dimension
     * must be passed and returns a new matrix.
     */
    public static FloatMatrix multiply(Pointer a, Pointer b, MatrixDimensions dim) {

        // Prepare the pointer for the result in DEVICE memory
        Pointer deviceResultPointer = new Pointer();
        int resMatrixSize = dim.getM() * dim.getN();
        int transA = dim.isTransposeA() ? cublasOperation.CUBLAS_OP_T : cublasOperation.CUBLAS_OP_N;
        int transB = dim.isTransposeB() ? cublasOperation.CUBLAS_OP_T : cublasOperation.CUBLAS_OP_N;

        if (CUBLAS2_AVAILABLE) {
            JCuda.cudaMalloc(deviceResultPointer, Sizeof.FLOAT * resMatrixSize);
            Pointer alpha = Pointer.to(new float[]{1.0f});
            Pointer beta = Pointer.to(new float[]{0.0f});
            JCublas2.cublasSgemm(handle, transA, transB, dim.getM(), dim.getN(), dim.getK(), alpha, a, dim.getLdA(), b, dim.getLdB(), beta, deviceResultPointer, dim.getLdC());
            freePointer(alpha);
            freePointer(beta);
        } else {
            JCublas.cublasAlloc(resMatrixSize, Sizeof.FLOAT, deviceResultPointer);
            JCublas.cublasSgemm(transA == 0 ? 'n' : 'y', transB == 0 ? 'n' : 'y', dim.getM(), dim.getN(), dim.getK(), 1f, a, dim.getLdA(), b, dim.getLdB(), 0f, deviceResultPointer, dim.getLdC());
        }

        JCuda.cudaDeviceSynchronize();

        FloatMatrix matrix = getMatrix(deviceResultPointer, dim.getM(), dim.getN());

        freePointer(deviceResultPointer);

        return matrix;
    }

    /**
     * Multiplies matrix a with matrix b and returns a new matrix. You can add
     * transpose flags for both matrices.
     */
    public static FloatMatrix multiply(FloatMatrix a, FloatMatrix b, boolean transposeA, boolean transposeB) {
        
//        if(transposeA) {
//            a = a.transpose();
//        }
//        if(transposeB) {
//            b = b.transpose();
//        }
//        
//        return a.mmul(b);
        
        Pointer matrixPointerA = memcpyMatrix(a);
        Pointer matrixPointerB = memcpyMatrix(b);
        FloatMatrix matrix = multiply(matrixPointerA, matrixPointerB, new MatrixDimensions(a, b, transposeA, transposeB));
        freePointer(matrixPointerA);
        freePointer(matrixPointerB);
        return matrix;
    }

    /**
     * Copies the given matrix to the device memory in column major format.
     *
     * @param a 
     * @return a pointer to this matrix.
     */
    public static Pointer memcpyMatrix(FloatMatrix a) {
        int matrixSizeA = a.getColumns() * a.getRows();
        float[] matrix = a.toArray();
        Pointer deviceMatrixA = new Pointer();
        JCuda.cudaMalloc(deviceMatrixA, matrixSizeA * Sizeof.FLOAT);
        if (CUBLAS2_AVAILABLE) {
            JCublas2.cublasSetMatrix(a.getRows(), a.getColumns(), Sizeof.FLOAT, Pointer.to(matrix), a.getRows(), deviceMatrixA, a.getRows());
        } else {
            JCublas.cublasSetMatrix(a.getRows(), a.getColumns(), Sizeof.FLOAT, Pointer.to(matrix), a.getRows(), deviceMatrixA, a.getRows());
        }

        return deviceMatrixA;
    }

    /**
     * Read a matrix from device memory.
     *
     * @param src the head pointer to the matrix.
     * @param rows the number of rows.
     * @param columns the number of columns
     * @return a new matrix with the results from device.
     */
    public static FloatMatrix getMatrix(Pointer src, int rows, int columns) {
        float[] raw = new float[rows * columns];
        Pointer dst = Pointer.to(raw);
        if (CUBLAS2_AVAILABLE) {
            JCublas2.cublasGetMatrix(rows, columns, Sizeof.FLOAT, src, rows, dst, rows);
        } else {
            JCublas.cublasGetMatrix(rows, columns, Sizeof.FLOAT, src, rows, dst, rows);
        }
        return new FloatMatrix(rows, columns, raw);
    }

    /**
     * Frees the given pointer.
     *
     * @param p the pointer to free
     */
    public static void freePointer(Pointer p) {
        JCuda.cudaFree(p);
    }

    private static void cublasDestroy(cublasHandle handle) {
        if (CUBLAS2_AVAILABLE) {
            JCublas2.cublasDestroy(handle);
        } else {
            JCublas.cublasShutdown();
        }
    }

    // thanks aioobe :)
    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /*
     * Simple benchmarking between CPU and GPU.
     */
    public static void main(String[] args) {

        int n = 40000;
        int k = 3072;
        int m = 1024;

        int N = 4096;

        FloatMatrix a = FloatMatrix.rand(4096, 4096).mmul(1000f);
        FloatMatrix b = FloatMatrix.rand(4096, 4096).mmul(1000f);
        long start = System.currentTimeMillis();
        FloatMatrix multiplyGPU = multiply(a, b);
        System.out.println("GPU took: " + (System.currentTimeMillis() - start) / 1000f + "s!");
        start = System.currentTimeMillis();
        FloatMatrix multiplyCPU = a.mmul(b);
        System.out.println("CPU took: " + (System.currentTimeMillis() - start) / 1000f + "s!");
        System.out.println("Matrix difference: " + multiplyCPU.sub(multiplyGPU).sum());
    }

}
