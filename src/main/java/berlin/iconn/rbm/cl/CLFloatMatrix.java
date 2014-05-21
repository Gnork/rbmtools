package berlin.iconn.rbm.cl;

import berlin.iconn.rbm.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Random;

import berlin.iconn.rbm.cl.OCL;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLProgram;
import org.jblas.FloatMatrix;


public class CLFloatMatrix {

	private static final String DEFAULT_BUILD_OPTIONS = CLProgram.CompilerOptions.FAST_RELAXED_MATH;
	private static final String DEFAULT_PROGRAM_NAME = "CLFloatMatrix.cl";
	private static final int SIZE_OF_FLOAT = 4;

	public static OCL ocl;
	private static CLBuffer<FloatBuffer> sharedBuffer;
	private static int blockSize = 1;
	private static int blockDimSize = 1;

	public final int rows;
	public final int columns;
	public final float[] elements;
	public final int clRows;
	public final int clColumns;
	public final CLBuffer<FloatBuffer> clBuffer;

	public CLFloatMatrix(final int rows, final int columns, final float[] elements) {
		this.rows = rows;
		this.columns = columns;
		this.elements = elements;
		this.clRows = this.roundToMultipleOf(this.rows, blockDimSize);
		this.clColumns = this.roundToMultipleOf(this.columns, blockDimSize);
		this.clBuffer = ocl.context.createFloatBuffer(this.clRows * this.clColumns);
	}

	public CLFloatMatrix(final int rows, final int columns) {
		this(rows, columns, new float[columns * rows]);
	}

	public CLFloatMatrix(final float[][] elements2D) {
		this(elements2D.length, elements2D[0].length, array2DTo1D(elements2D));
	}

	public static void setUpOCL(final OCL ocl) throws IOException {
		CLFloatMatrix.ocl = ocl;
		CLFloatMatrix.sharedBuffer = ocl.context.createFloatBuffer(ocl.device.getMaxComputeUnits());
		CLFloatMatrix.blockDimSize = calculateBlockDimSize(ocl.device.getMaxWorkGroupSize());
		CLFloatMatrix.blockSize = blockDimSize * blockDimSize;
		ocl.buildProgram(DEFAULT_PROGRAM_NAME, DEFAULT_BUILD_OPTIONS, CLProgram.define("BLOCK_DIM", blockDimSize));
	}

	public static CLFloatMatrix zeros(final int rows, final int columns) {
		return new CLFloatMatrix(rows, columns);
	}

	public static CLFloatMatrix ones(final int rows, final int columns) {
		final float[] elements = new float[rows * columns];
		Arrays.fill(elements, 1.0f);
		return new CLFloatMatrix(rows, columns, elements);
	}

	public static CLFloatMatrix randn(final int rows, final int columns, float variance) {
		final Random random = new Random();
		final float[] elements = new float[columns * rows];

		for (int i = 0; i < elements.length; i++) {
			elements[i] = (float) random.nextGaussian() * variance;
		}

		return new CLFloatMatrix(rows, columns, elements);
	}

	public static CLFloatMatrix rand(final int rows, final int columns, float variance) {
		return rand(rows, columns, variance, new Random());
	}

	public static CLFloatMatrix rand(final int rows, final int columns, float variance, Random random) {
		final float[] elements = new float[columns * rows];

		for (int i = 0; i < elements.length; i++) {
			elements[i] = (float) random.nextFloat() * variance;
		}

		return new CLFloatMatrix(rows, columns, elements);
	}

	public CLFloatMatrix mmul(final CLFloatMatrix matrixA, final CLFloatMatrix matrixB) {
		return this.blockMmul(ocl.getKernel("mmul_kernel"), matrixA, matrixB, matrixA.getClColumns());
	}
	public CLFloatMatrix mmullt(final CLFloatMatrix matrixA, final CLFloatMatrix matrixB) {
		return this.blockMmul(ocl.getKernel("mmul_lt_kernel"), matrixA, matrixB, matrixA.getClRows());
	}
	public CLFloatMatrix mmulrt(final CLFloatMatrix matrixA, final CLFloatMatrix matrixB) {
		return this.blockMmul(ocl.getKernel("mmul_rt_kernel"), matrixA, matrixB, matrixA.getClColumns());
	}

	public CLFloatMatrix naivMmul(final CLFloatMatrix a, final CLFloatMatrix b) {
		final CLKernel kernel = ocl.getKernel("naiv_mmul_kernel");

		kernel
			.putArg(a.clBuffer)
			.putArg(b.clBuffer)
			.putArg(this.clBuffer)
			.putArg(a.clRows)
			.putArg(b.clRows)
			.putArg(this.clColumns);

		ocl.queue
			.putBarrier()
			.put2DRangeKernel(kernel, 0, 0, this.clRows, this.clColumns, blockDimSize, blockDimSize);

		return this;
	}

	public CLFloatMatrix rowsGlobalColumnsLokalElementWiseMmul(final CLFloatMatrix a, final CLFloatMatrix b) {
		final CLKernel kernel = ocl.getKernel("rows_global_columns_lokal_element_wise_mmul_kernel");

		kernel
			.putArg(a.clBuffer)
			.putArg(b.clBuffer)
			.putArg(this.clBuffer)
			.putArg(a.clRows)
			.putArg(b.clRows)
			.putArg(this.clColumns)
			.putNullArg(b.clColumns * SIZE_OF_FLOAT);

		ocl.queue
			.putBarrier()
			.put2DRangeKernel(kernel, 0, 0, roundToMultipleOf(this.clRows, blockSize), this.clColumns, blockSize, 1);

		return this;
	}

	public CLFloatMatrix rowsGlobalColumnsLokalRowWiseMmul(final CLFloatMatrix a, final CLFloatMatrix b) {
		final CLKernel kernel = ocl.getKernel("rows_global_columns_lokal_row_wise_mmul_kernel");

		kernel
			.putArg(a.clBuffer)
			.putArg(b.clBuffer)
			.putArg(this.clBuffer)
			.putArg(a.clRows)
			.putArg(b.clRows)
			.putArg(this.clColumns)
			.putNullArg(b.clRows * SIZE_OF_FLOAT);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, roundToMultipleOf(this.clRows, blockSize), blockSize);

		return this;
	}

	public CLFloatMatrix rowsPrivateColumnsLokalRowWiseMmul(final CLFloatMatrix a, final CLFloatMatrix b) {
		final CLKernel kernel = ocl.getKernel("rows_private_columns_lokal_row_wise_mmul_kernel");

		kernel
			.putArg(a.clBuffer)
			.putArg(b.clBuffer)
			.putArg(this.clBuffer)
			.putArg(a.clRows)
			.putArg(b.clRows)
			.putArg(this.clColumns)
			.putNullArg(b.clRows * SIZE_OF_FLOAT);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, roundToMultipleOf(this.clRows, blockSize), blockSize);

		return this;
	}

	public float sum() {
		return this.twoStageSum();
	}

	public float multiStageSum(final CLBuffer<FloatBuffer> workBuffer) {
		return this.recursiveSum(this.clBuffer, workBuffer, this.clBuffer.getCLCapacity(), 1);
	}

	public float multiStageSum() {
		final int length = this.clBuffer.getCLCapacity();
		final CLBuffer<FloatBuffer> workBuffer = ocl.context.createFloatBuffer(length);
		final float sum = this.recursiveSum(this.clBuffer, workBuffer, length, 1);
		workBuffer.release();
		return sum;
	}

	public float twoStageSum() {
		final CLKernel kernel = ocl.getKernel("two_stage_sum_kernel");

		final int length = this.clBuffer.getCLCapacity();
		final int numWorkUnits = CLFloatMatrix.sharedBuffer.getCLCapacity();
		final int lws = blockSize;
		final int gws = numWorkUnits * lws;

		kernel
			.putArg(this.clBuffer)
			.putArg(CLFloatMatrix.sharedBuffer)
			.putArg(length)
			.putNullArg(lws * SIZE_OF_FLOAT);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws)
			.putReadBuffer(CLFloatMatrix.sharedBuffer, true);

		float sum = 0;
		final FloatBuffer fb = CLFloatMatrix.sharedBuffer.getBuffer();
		while (fb.hasRemaining()) {
			sum += fb.get();
		}
		fb.rewind();
		return sum;
	}

	public CLFloatMatrix transpose(final CLFloatMatrix original) {
		final CLKernel kernel = ocl.getKernel("transpose_kernel");

		kernel
			.putArg(original.clBuffer)
			.putArg(this.clBuffer);

		ocl.queue
			.putBarrier()
			.put2DRangeKernel(kernel, 0, 0, this.clRows, this.clColumns, blockDimSize, blockDimSize);

		return this;
	}

	public CLFloatMatrix sigfunc() {
		final CLKernel kernel = ocl.getKernel("sigmoid_function_kernel");
		kernel
			.putArg(this.clBuffer)
			.putArg(this.rows)
			.putArg(this.columns);

		ocl.queue
			.putBarrier()
			.put2DRangeKernel(kernel, 0, 0, this.clRows, this.clColumns, blockDimSize, blockDimSize);

		return this;
	}

	public CLFloatMatrix putColVecOnes() {
		final CLKernel kernel = ocl.getKernel("put_column_vector_ones_kernel");
		final int lws = blockSize;
		final int gws = (int) (lws * Math.ceil(rows /(float)lws));

		kernel
			.putArg(this.clBuffer)
			.putArg(this.rows);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);

		return this;
	}

	public CLFloatMatrix putColVecZeros() {
		final CLKernel kernel = ocl.getKernel("put_column_vector_zeros_kernel");
		final int lws = blockSize;
		final int gws = (int) (lws * Math.ceil(rows /(float)lws));

		kernel
			.putArg(this.clBuffer)
			.putArg(this.rows);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);

		return this;
	}

	/**
	 * Puts a column vector with the length of rowRange in the first column of
	 * the given matrix on the RBMs CLDevice.
	 *
	 * @param elements The CLBuffer to which the matrix was written.
	 * @param vector The column vector
	 * @param rows Size of the matrix in the CLBuffer
	 * @param columns Size of the matrix in the CLBuffer
	 * @param rowRange The number of rows of the matrix without padding.
	 */
	public void putColumnVector(final CLFloatMatrix vector)
	{
		final CLKernel kernel = ocl.getKernel("put_column_vector_kernel");
		final int lws = blockSize;
		final int gws = (int) (lws * Math.ceil(this.rows /(float)lws));

		kernel
			.putArg(this.clBuffer)
			.putArg(vector.clBuffer)
			.putArg(this.rows);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);
	}

	public void putColumnVectorAt(
			final CLFloatMatrix vector,
			final CLFloatMatrix validPos)
	{
		final CLKernel kernel = ocl.getKernel("put_column_vector_at_kernel");
		final int lws = blockSize;
		final int gws = (int) (lws * Math.ceil(this.rows /(float)lws));

		kernel
			.putArg(this.clBuffer)
			.putArg(vector.clBuffer)
			.putArg(validPos.clBuffer)
			.putArg(this.rows);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);
	}

	public void randomQuantisation(final int seed1, final int seed2)
	{
		final CLKernel kernel = ocl.getKernel("random_quantisation_kernel");

		final int length = this.clBuffer.getCLCapacity();
		final int numWorkUnits = CLFloatMatrix.sharedBuffer.getCLCapacity();
		final int lws = blockSize;
		final int gws = numWorkUnits * lws;

		kernel
			.putArg(this.clBuffer)
			.putArg(seed1)
			.putArg(seed2)
			.putArg(length);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);
	}

	public void randomQuantisation()
	{
		Random rand = new Random();
		randomQuantisation(rand.nextInt(), rand.nextInt());
	}

	public void hardQuantisation(final float threshold)
	{
		final CLKernel kernel = ocl.getKernel("hard_quantisation_kernel");

		final int length = this.clBuffer.getCLCapacity();
		final int numWorkUnits = CLFloatMatrix.sharedBuffer.getCLCapacity();
		final int lws = blockSize;
		final int gws = numWorkUnits * lws;

		kernel
			.putArg(this.clBuffer)
			.putArg(threshold)
			.putArg(length);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);
	}

	public void rand(final int seed1, int seed2)
	{
		final CLKernel kernel = ocl.getKernel("random_fill_kernel");

		final int length = this.clBuffer.getCLCapacity();
		final int numWorkUnits = CLFloatMatrix.sharedBuffer.getCLCapacity();
		final int lws = blockSize;
		final int gws = numWorkUnits * lws;

		kernel
			.putArg(this.clBuffer)
			.putArg(seed1)
			.putArg(seed2)
			.putArg(length);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);
	}

	public CLFloatMatrix add(final CLFloatMatrix matrixA, final CLFloatMatrix matrixB) {
		final CLKernel kernel = ocl.getKernel("add_kernel");

		final int length = this.clBuffer.getCLCapacity();
		final int numWorkUnits = CLFloatMatrix.sharedBuffer.getCLCapacity();
		final int lws = blockSize;
		final int gws = numWorkUnits * lws;

		kernel
			.putArg(matrixA.getCLBuffer())
			.putArg(matrixB.getCLBuffer())
			.putArg(this.clBuffer)
			.putArg(length);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);

		return this;
	}

	public CLFloatMatrix sub(final CLFloatMatrix matrixA, final CLFloatMatrix matrixB) {
		final CLKernel kernel = ocl.getKernel("sub_kernel");

		final int length = this.clBuffer.getCLCapacity();
		final int numWorkUnits = CLFloatMatrix.sharedBuffer.getCLCapacity();
		final int lws = blockSize;
		final int gws = numWorkUnits * lws;

		kernel
			.putArg(matrixA.getCLBuffer())
			.putArg(matrixB.getCLBuffer())
			.putArg(this.clBuffer)
			.putArg(length);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);

		return this;
	}

	public CLFloatMatrix mul(final float val) {
		final CLKernel kernel = ocl.getKernel("mul_kernel");

		final int length = this.clBuffer.getCLCapacity();
		final int numWorkUnits = CLFloatMatrix.sharedBuffer.getCLCapacity();
		final int lws = blockSize;
		final int gws = numWorkUnits * lws;

		kernel
			.putArg(this.clBuffer)
			.putArg(val)
			.putArg(length);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);

		return this;
	}

	public CLFloatMatrix sqr() {
		final CLKernel kernel = ocl.getKernel("sqr_kernel");

		final int length = this.clBuffer.getCLCapacity();
		final int numWorkUnits = CLFloatMatrix.sharedBuffer.getCLCapacity();
		final int lws = blockSize;
		final int gws = numWorkUnits * lws;

		kernel
			.putArg(this.clBuffer)
			.putArg(length);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);

		return this;
	}

	public CLFloatMatrix produceOverhead(final CLFloatMatrix matrixA, final CLFloatMatrix matrixB) {
		final CLKernel kernel = ocl.getKernel("dummy_kernel");
		kernel
			.putArg(matrixA.getCLBuffer())
			.putArg(matrixB.getCLBuffer())
			.putArg(this.clBuffer);

		ocl.queue
			.putBarrier()
			.put2DRangeKernel(kernel, 0, 0, this.clRows, this.clColumns, blockDimSize, blockDimSize);

		return this;
	}

	public CLFloatMatrix enqueue() {
		return this.enqueue(false);
	}

	public CLFloatMatrix enqueue(final boolean blockingRead) {
		final float[] wrapped = new float[this.clColumns* this.clRows];
		for (int i = 0, j = 0; i < this.elements.length; i += this.rows, j += this.clRows) {
			System.arraycopy(this.elements, i, wrapped, j, this.rows);
		}
		this.clBuffer.getBuffer().put(wrapped).rewind();
		ocl.queue.putWriteBuffer(this.clBuffer, blockingRead);
		return this;
	}

	public CLFloatMatrix dequeue() {
		return this.dequeue(true);
	}

	public CLFloatMatrix dequeue(final boolean blockingRead) {
		final float[] wrapped = new float[this.clColumns* this.clRows];
		ocl.queue.putReadBuffer(this.clBuffer, blockingRead);
		this.clBuffer.getBuffer().get(wrapped).rewind();
		for (int i = 0, j = 0; i < this.elements.length; i += this.rows, j += this.clRows) {
			System.arraycopy(wrapped, j, this.elements, i, this.rows);
		}
		return this;
	}

	public CLBuffer<FloatBuffer> getCLBuffer() {
		return this.clBuffer;
	}

	/**
	 * Wieviele Zeilen werden in der GPU für die Matrix verwendet
	 *
	 * @return
	 */
	public int getClRows() {
		return this.clRows;
	}

	public int getClColumns() {
		return this.clColumns;
	}

	public float[] getElements() {
		return this.elements;
	}

	public int getRows() {
		return this.rows;
	}

	public int getColumns() {
		return this.columns;
	}

	public float get(final int r, final int c) {
		return this.elements[c * this.rows + r];
	}

	public void print(final int maxRows, final int maxColumns) {
		final int rows = Math.min(maxRows, this.rows);
		final int columns = Math.min(maxColumns, this.columns);

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				System.out.printf("%+.1f ", this.get(i, j));
			}
			System.out.printf("\n");
		}
		System.out.printf("\n");
	}

	public void print() {
		this.print(this.rows, this.columns);
	}

	// TODO M is the dimension that a and b share, rename and comment
	private CLFloatMatrix blockMmul(final CLKernel kernel, final CLFloatMatrix matrixA, final CLFloatMatrix matrixB, final int M) {
		kernel
			.putArg(matrixA.getCLBuffer())
			.putArg(matrixB.getCLBuffer())
			.putArg(this.clBuffer)
			.putArg(this.clRows)
			.putArg(M)
			.putArg(this.clColumns)
			.putNullArg(blockSize * SIZE_OF_FLOAT)
			.putNullArg(blockSize * SIZE_OF_FLOAT);

		ocl.queue
			.putBarrier()
			.put2DRangeKernel(kernel, 0, 0, this.clRows, this.clColumns, blockDimSize, blockDimSize);

		return this;
	}

	private float recursiveSum(final CLBuffer<FloatBuffer> in,
			final CLBuffer<FloatBuffer> out,
			int length,
			int factor)
	{
		final CLKernel kernel = ocl.getKernel("multi_stage_sum_kernel");
		final int lws = blockSize;
		final int gws = roundToMultipleOf(length, lws);

		kernel
			.putArg(in)
			.putArg(out)
			.putArg(length)
			.putArg(factor)
			.putNullArg(lws * SIZE_OF_FLOAT);

		ocl.queue
			.putBarrier()
			.put1DRangeKernel(kernel, 0, gws, lws);

		length = gws/lws;
		factor *= lws;

		if(length == 1) {
			ocl.queue.putReadBuffer(out, true);
			return out.getBuffer().get(0);
		} else {
			return recursiveSum(out, out, length, factor);
		}
	}

	private int roundToMultipleOf(final int num, final int multipleOf) {
		return (num + multipleOf - 1) / multipleOf * multipleOf;
	}

	private static int calculateBlockDimSize(final long maxWorkGroupSize) {
		return (int) Math.floor(Math.sqrt(maxWorkGroupSize));
	}

	private static float[] array2DTo1D(float[][] array2D) {
		final float[] array1D = new float[array2D[0].length * array2D.length];
		for (int i = 0; i < array2D[0].length; i++) {
			for (int j = 0; j < array2D.length; j++) {
				array1D[i * array2D.length + j] = array2D[j][i];
			}
		}
		return array1D;
	}
        
        public FloatMatrix getFloatMatrix() {
            float[] floatMatrix1D = this.dequeue().getElements();
            float[][] floatMatrix2D = new float[rows][columns];
            
		for (int i = 0; i < columns; i++) {
			for (int j = 0; j < rows; j++) {
                                floatMatrix2D[j][i] = floatMatrix1D[i * rows + j];
			}
		}
            
                return new FloatMatrix(floatMatrix2D);
        }
}
