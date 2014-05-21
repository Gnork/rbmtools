// Literatur
// http://www.fixstars.com/en/opencl/book/OpenCLProgrammingBook/opencl-c/
// http://www.informit.com/articles/article.aspx?p=1732873&seqNum=3
// https://developer.apple.com/library/mac/samplecode/OpenCL_Parallel_Reduction_Example/Listings/reduce_float4_kernel_cl.html


// Matrices are stored in column-major order:
// M(row, col) = *(M.elements + col * M.stride + row)
typedef struct {
	int height;
	int width;
	int stride;
	__global float* elements;
} Matrix;

// Get a matrix element
float get_element(const Matrix A, int row, int col)
{
	return A.elements[col * A.stride + row];
}

// Set a matrix element
void set_element(Matrix A, int row, int col, float value)
{
	A.elements[col * A.stride + row] = value;
}

// Get the block_dim x block_dim sub-matrix Asub of A that is
// located block_col sub-matrices to the right and block_row sub-matrices down
// from the upper-left corner of A
Matrix get_sub_matrix(Matrix A, int block_row, int block_col)
{
	Matrix Asub;
	Asub.height = BLOCK_DIM;
	Asub.width = BLOCK_DIM;
	Asub.stride = A.stride;
	Asub.elements = &A.elements[A.stride * BLOCK_DIM * block_col + BLOCK_DIM * block_row];
	return Asub;
}

// Matrix multiplication function called by MatMulKernel()
void mmul(
		const Matrix A,
		const Matrix B,
		Matrix C,
		__const int M,
		__local float* As,
		__local float* Bs)
{
	// Block row and column
	int block_row = get_group_id(0);
	int block_col = get_group_id(1);

	// Each thread block computes one sub-matrix Csub of C
	Matrix Csub = get_sub_matrix(C, block_row, block_col);

	// Each thread computes one element of Csub
	// by accumulating results into Cvalue
	float Cvalue = 0;

	// Thread row and column within Csub
	int row = get_local_id(0);
	int col = get_local_id(1);

	// Loop over all the sub-matrices of A and B that are
	// required to compute Csub
	// Multiply each pair of sub-matrices together
	// and accumulate the results
	for (int m = 0; m < (M / BLOCK_DIM); ++m) {

		// Get sub-matrix Asub of A
		Matrix Asub = get_sub_matrix(A, block_row, m);

		// Get sub-matrix Bsub of B
		Matrix Bsub = get_sub_matrix(B, m, block_col);

		// Load Asub and Bsub from device memory to shared memory
		// Each thread loads one element of each sub-matrix
		As[col * BLOCK_DIM + row] = get_element(Asub, row, col);
		Bs[col * BLOCK_DIM + row] = get_element(Bsub, row, col);

		// Synchronize to make sure the sub-matrices are loaded
		// before starting the computation
		barrier(CLK_LOCAL_MEM_FENCE);

		// Multiply Asub and Bsub together

		for (int e = 0; e < BLOCK_DIM; ++e) {
			Cvalue += As[e * BLOCK_DIM + row] * Bs[col * BLOCK_DIM + e];
		}

		// Synchronize to make sure that the preceding
		// computation is done before loading two new
		// sub-matrices of A and B in the next iteration
		barrier(CLK_LOCAL_MEM_FENCE);
	}

	// Write Csub to device memory
	// Each thread writes one element
	set_element(Csub, row, col, Cvalue);
}

void mmul_lt(
		const Matrix A,
		const Matrix B,
		Matrix C,
		__const int M,
		__local float* As,
		__local float* Bs)
{
	int block_row = get_group_id(0);
	int block_col = get_group_id(1);

	Matrix Csub = get_sub_matrix(C, block_row, block_col);

	float Cvalue = 0;

	int row = get_local_id(0);
	int col = get_local_id(1);

	for (int m = 0; m < (M / BLOCK_DIM); ++m) {

		Matrix Asub = get_sub_matrix(A, m, block_row); // transpose A step 1

		Matrix Bsub = get_sub_matrix(B, m, block_col);

		As[col * BLOCK_DIM + row] = get_element(Asub, col, row); // transpose A step 2
		Bs[col * BLOCK_DIM + row] = get_element(Bsub, row, col);

		barrier(CLK_LOCAL_MEM_FENCE);

		for (int e = 0; e < BLOCK_DIM; ++e) {
			Cvalue += As[e * BLOCK_DIM + row] * Bs[col * BLOCK_DIM + e];
		}

		barrier(CLK_LOCAL_MEM_FENCE);
	}

	set_element(Csub, row, col, Cvalue);
}

void mmul_rt(
		const Matrix A,
		const Matrix B,
		Matrix C,
		__const int M,
		__local float* As,
		__local float* Bs)
{
	int block_row = get_group_id(0);
	int block_col = get_group_id(1);

	Matrix Csub = get_sub_matrix(C, block_row, block_col);

	float Cvalue = 0;

	int row = get_local_id(0);
	int col = get_local_id(1);

	for (int m = 0; m < (M / BLOCK_DIM); ++m) {

		Matrix Asub = get_sub_matrix(A, block_row, m);

		Matrix Bsub = get_sub_matrix(B, block_col, m); // transpose B step 1

		As[col * BLOCK_DIM + row] = get_element(Asub, row, col);
		Bs[col * BLOCK_DIM + row] = get_element(Bsub, col, row);  // transpose B step 2

		barrier(CLK_LOCAL_MEM_FENCE);

		for (int e = 0; e < BLOCK_DIM; ++e) {
			Cvalue += As[e * BLOCK_DIM + row] * Bs[col * BLOCK_DIM + e];
		}

		barrier(CLK_LOCAL_MEM_FENCE);
	}

	set_element(Csub, row, col, Cvalue);
}


// http://stackoverflow.com/questions/11268023/random-numbers-with-opencl-using-random123
uint rand_uint(__global uint2* rvec) {
	#define A 4294883355U

	// http://stackoverflow.com/questions/9788806/access-vector-type-opencl
	uint x = ((__global const uint*)rvec)[0]; //Unpack the state
	uint c = ((__global const uint*)rvec)[1]; //Unpack the state

    uint res = x ^ c;          //Calculate the result
    uint hi = mul_hi(x,A);     //Step the RNG
    x = x*A + c;
    c = hi + (x<c);

    *rvec = (uint2)(x,c);      //Pack the state back up
    return res;                //Return the next result

	#undef A
}

inline float rand_float(__global uint2* rvec) {
	return (float)(rand_uint(rvec)) / (float)(0xFFFFFFFF);
}


uint rand_uint_local(uint2* rvec) {
	#define A 4294883355U

	// http://stackoverflow.com/questions/9788806/access-vector-type-opencl
	uint x = ((const uint*)rvec)[0]; //Unpack the state
	uint c = ((const uint*)rvec)[1]; //Unpack the state

    uint res = x ^ c;          //Calculate the result
    uint hi = mul_hi(x,A);     //Step the RNG
    x = x*A + c;
    c = hi + (x<c);

    *rvec = (uint2)(x,c);      //Pack the state back up
    return res;                //Return the next result

	#undef A
}

inline float rand_float_local(uint2* rvec) {
	return (float)(rand_uint_local(rvec)) / (float)(0xFFFFFFFF);
}

// #############################################################
// #                          KERNEL                           #
// #############################################################

__kernel void mmul_kernel(
	__global float* Aelements,
	__global float* Belements,
	__global float* Celements,
	__const int L,
	__const int M,
	__const int N,
	__local float* As,
	__local float* Bs)
{
	Matrix A = { L, M, L, Aelements };
	Matrix B = { M, N, M, Belements };
	Matrix C = { L, N, L, Celements };
	mmul(A, B, C, M, As, Bs);
}

__kernel void mmul_lt_kernel(
	__global float* Aelements,
	__global float* Belements,
	__global float* Celements,
	__const int L,
	__const int M,
	__const int N,
	__local float* As,
	__local float* Bs)
{
	Matrix A = { M, L, M, Aelements };
	Matrix B = { M, N, M, Belements };
	Matrix C = { L, N, L, Celements };
	mmul_lt(A, B, C, M, As, Bs);
}

__kernel void mmul_rt_kernel(
	__global float* Aelements,
	__global float* Belements,
	__global float* Celements,
	__const int L,
	__const int M,
	__const int N,
	__local float* As,
	__local float* Bs)
{
	Matrix A = { L, M, L, Aelements };
	Matrix B = { N, M, N, Belements };
	Matrix C = { L, N, L, Celements };
	mmul_rt(A, B, C, M, As, Bs);
}

__kernel void naiv_mmul_kernel(
	__global float* aelements,
	__global float* belements,
	__global float* celements,
	int L,
	int M,
	int N)
{
	int i = get_global_id(0);
	int j = get_global_id(1);

	if(i<L && j<N) {
		Matrix A = { L, M, L, aelements };
		Matrix B = { M, N, M, belements };
		Matrix C = { L, N, L, celements };

		float tmp = 0.0f;
		for(int k=0;k<M;k++) {
			tmp += get_element(A,i,k) * get_element(B,k,j);
		}
		set_element(C,i,j,tmp);
	}
}

__kernel void rows_global_columns_lokal_element_wise_mmul_kernel(
	__global float* aelements,
	__global float* belements,
	__global float* celements,
	int L,
	int M,
	int N,
	__local float* bcolumn)
{
	Matrix A = { L, M, L, aelements };
	Matrix B = { M, N, M, belements };
	Matrix C = { L, N, L, celements };

	int k;

	int row_idx = get_global_id(0);
	int col_idx = get_global_id(1);
	int local_index = get_local_id(0);
	int local_size = get_local_size(0);
	float tmp;

	// make every workitem copy only the least amount possible
	for(k=local_index; k<M; k+=local_size) {
		bcolumn[k] = get_element(B,k,col_idx);
	}
	barrier(CLK_LOCAL_MEM_FENCE);

	if(row_idx < L) {
		tmp = 0.0f;
		// iterate over every elements of a.row/b.column
		for(k=0; k<M; k++) {
			tmp += get_element(A,row_idx,k) * bcolumn[k];
		}
		set_element(C,row_idx, col_idx,tmp);
	}
}

__kernel void rows_global_columns_lokal_row_wise_mmul_kernel(
	__global float* aelements,
	__global float* belements,
	__global float* celements,
	int L,
	int M,
	int N,
	__local float* bcolumn)
{
	Matrix A = { L, M, L, aelements };
	Matrix B = { M, N, M, belements };
	Matrix C = { L, N, L, celements };

	int k;
	int col_idx;
	int row_idx = get_global_id(0);
	int local_index = get_local_id(0);
	int local_size = get_local_size(0);
	float tmp;

	for(col_idx=0; col_idx<N;col_idx++) {
		// make every workitem copy only the least amount possible
		for(k=local_index; k<M; k+=local_size) {
			bcolumn[k] = get_element(B,k,col_idx);
		}
		barrier(CLK_LOCAL_MEM_FENCE);

		if(row_idx < L) {
			tmp = 0.0f;
			// iterate over every elements of a.row/b.column
			for(k=0; k<M; k++) {
				tmp += get_element(A,row_idx,k) * bcolumn[k];
			}
			set_element(C,row_idx, col_idx,tmp);
		}
	}
}

__kernel void rows_private_columns_lokal_row_wise_mmul_kernel(
	__global float* aelements,
	__global float* belements,
	__global float* celements,
	int L,
	int M,
	int N,
	__local float* bcolumn)
{
	Matrix A = { L, M, L, aelements };
	Matrix B = { M, N, M, belements };
	Matrix C = { L, N, L, celements };

	int k;
	int col_idx;
	int row_idx = get_global_id(0);
	int local_index = get_local_id(0);
	int local_size = get_local_size(0);
	float tmp;
	float arow[1024];

	for(k=0; k<M;k++) {
		arow[k] = get_element(A,row_idx,k);
	}

	for(col_idx=0; col_idx<N;col_idx++) {
		// make every workitem copy only the least amount possible
		for(k=local_index; k<M; k+=local_size) {
			bcolumn[k] = get_element(B,k,col_idx);
		}
		barrier(CLK_LOCAL_MEM_FENCE);

		if(row_idx < L) {
			tmp = 0.0f;
			// iterate over every elements of a.row/b.column
			for(k=0; k<M; k++) {
				tmp += arow[k] * bcolumn[k];
			}
			set_element(C,row_idx, col_idx,tmp);
		}
	}
}

__kernel void two_stage_sum_kernel(
		__global float* buffer,
		__global float* result,
		__const int length,
		__local float* scratch)
{
	int global_index = get_global_id(0);
	int global_size = get_global_size(0);
	float accumulator = 0.0f;

	// Loop sequentially over chunks of input vector
	while (global_index < length) {
		accumulator += buffer[global_index];
		global_index += global_size;
	}

	// Perform parallel reduction
	int local_index = get_local_id(0);
	scratch[local_index] = accumulator;
	barrier(CLK_LOCAL_MEM_FENCE);
	for(int offset = get_local_size(0) >> 1; offset > 0; offset >>= 1) {
		if (local_index < offset) {
			scratch[local_index] += scratch[local_index + offset];
		}
		barrier(CLK_LOCAL_MEM_FENCE);
	}

	if (local_index == 0) {
		result[get_group_id(0)] = scratch[0];
	}
}

__kernel void multi_stage_sum_kernel(
		__global float* buffer,
		__global float* result,
		__const int length,
		__const int factor,
		__local float* scratch)
{
	int global_index = get_global_id(0);
	int local_index = get_local_id(0);

	// Load data into local memory
	if (global_index < length) {
		scratch[local_index] = buffer[global_index * factor];
	} else {
		scratch[local_index] = 0.0f;
	}
	barrier(CLK_LOCAL_MEM_FENCE);

	for(int offset = 1; offset < get_local_size(0); offset <<= 1) {
		int mask = (offset << 1) - 1;
		if ((local_index & mask) == 0) {
			scratch[local_index] += scratch[local_index + offset];
		}
		barrier(CLK_LOCAL_MEM_FENCE);
	}

	if (local_index == 0) {
		result[global_index * factor] = scratch[0];
	}
}

__kernel void transpose_kernel(
	__global float* orig_elements,
	__global float* trans_elements)
{
	int rows = get_global_size(0);
	int cols = get_global_size(1);

	int i = get_global_id(0);
	int j = get_global_id(1);

	Matrix orig = { cols, rows, cols, orig_elements };
	Matrix trans = { rows, cols, rows, trans_elements };

	set_element(trans,i,j,get_element(orig,j,i));
}

__kernel void sigmoid_function_kernel(
	__global float* elements,
	__const int row_range,
	__const int col_range)
{
	const int row_index = get_global_id(0);
	const int col_index = get_global_id(1);



	if(row_index < row_range && col_index < col_range) {
		const int rows = get_global_size(0);
		const int idx = col_index * rows + row_index;
		elements[idx] = 1 / (1 + exp(-elements[idx]));
	}
}

__kernel void put_column_vector_ones_kernel(
	__global float* elements,
	__const int row_range)
{
	const int global_index = get_global_id(0);
	if(global_index < row_range) {
		elements[global_index] = 1.0f;
	}
}

__kernel void put_column_vector_zeros_kernel(
	__global float* elements,
	__const int row_range)
{
	const int global_index = get_global_id(0);
	if(global_index < row_range) {
		elements[global_index] = 0.0f;
	}
}

__kernel void put_column_vector_kernel(
	__global float* elements,
	__global float* vector,
	__const int row_range)
{
	const int global_index = get_global_id(0);
	if(global_index < row_range) {
		elements[global_index] = vector[global_index];
	}
}

__kernel void put_column_vector_at_kernel(
	__global float* elements,
	__global float* vector,
	__global float* validPos,
	__const int row_range)
{
	const int global_index = get_global_id(0);
	if(global_index < row_range) {
		elements[global_index] = validPos[global_index] * vector[global_index] + elements[global_index] * (1-validPos[global_index]);
	}
}

kernel void add_kernel(
	__global float* Aelements,
	__global float* Belements,
	__global float* Celements,
	__const int length)
{
	int global_index = get_global_id(0);
	int global_size = get_global_size(0);

	while (global_index < length) {
		Celements[global_index] = Aelements[global_index] + Belements[global_index];
		global_index += global_size;
	}
}

__kernel void sub_kernel(
	__global float* Aelements,
	__global float* Belements,
	__global float* Celements,
	__const int length)
{
	int global_index = get_global_id(0);
	int global_size = get_global_size(0);

	while (global_index < length) {
		Celements[global_index] = Aelements[global_index] - Belements[global_index];
		global_index += global_size;
	}
}

__kernel void mul_kernel(
	__global float* elements,
	__const float value,
	__const int length)
{
	int global_index = get_global_id(0);
	int global_size = get_global_size(0);

	while (global_index < length) {
		elements[global_index] *= value;
		global_index += global_size;
	}
}

__kernel void sqr_kernel(
	__global float* elements,
	__const int length)
{
	int global_index = get_global_id(0);
	int global_size = get_global_size(0);

	while (global_index < length) {
		elements[global_index] *= elements[global_index];
		global_index += global_size;
	}
}


__kernel void random_kernel(
	__global float* elements,
	__global uint2* randoms,
	__const int length)
{
    int global_index = get_global_id(0);
	int global_size = get_global_size(0);
    __global uint2* rvec = (__global uint2*)&randoms[global_index];

    //Call rand_uint or rand_float a number of times with "rvec" as argument.
    //These calls update "rvec" with new state, and return a random number
	while (global_index < length) {
		float element = elements[global_index];
		float rand = rand_float(rvec);
		elements[global_index] = element > rand;
		global_index += global_size;
	}

    randoms[global_index] = *rvec;
}

__kernel void random_quantisation_kernel(
	__global float* elements,
	__const uint seed1,
	__const uint seed2,
	__const int length)
{
    int global_index = get_global_id(0);
	int global_size = get_global_size(0);

	uint2 seeds = (uint2)(seed1*global_index, seed2*global_index);
    uint2* rvec = (uint2*)&seeds;

    //Call rand_uint or rand_float a number of times with "rvec" as argument.
    //These calls update "rvec" with new state, and return a random number
	while (global_index < length) {
		float element = elements[global_index];
		float rand = rand_float_local(rvec);
		elements[global_index] = element > rand;
		global_index += global_size;
	}
}

__kernel void hard_quantisation_kernel(
	__global float* elements,
	__const float threshold,
	__const int length)
{
    int global_index = get_global_id(0);
	int global_size = get_global_size(0);

	while (global_index < length) {
		float element = elements[global_index];
		elements[global_index] = element > threshold;
		global_index += global_size;
	}
}


__kernel void random_fill_kernel(
	__global float* elements,
	__const uint seed1,
	__const uint seed2,
	__const int length)
{
	int global_index = get_global_id(0);
	int global_size = get_global_size(0);

	uint2 seeds = (uint2)(seed1*global_index, seed2*global_index);
    uint2* rvec = (uint2*)&seeds;

    //Call rand_uint or rand_float a number of times with "rvec" as argument.
    //These calls update "rvec" with new state, and return a random number
	while (global_index < length) {
		float element = elements[global_index];
		float rand = rand_float_local(rvec);
		elements[global_index] = rand;
		global_index += global_size;
	}
}


__kernel void test_kernel(
	__global float *data)
{
	int a = get_global_id(0);
	__global float *pt;
	pt = (__global float*)&data[a];
	*pt += a+1;
	return;
}


__kernel void dummy_kernel(
	__global float* aelements,
	__global float* belements,
	__global float* celements)
{
}


// https://github.com/ochafik/nativelibs4java/blob/master/libraries/OpenCL/JavaCL/src/main/java/com/nativelibs4java/opencl/util/ParallelRandom.java
// http://nativelibs4java.sourceforge.net/javacl/api/stable/src-html/com/nativelibs4java/opencl/util/XORShiftRandom.html#line.12

#ifndef NUMBERS_COUNT
#define NUMBERS_COUNT 0
#endif

#ifndef WORK_ITEMS_COUNT
#define WORK_ITEMS_COUNT get_global_size(0)
#endif

/**
 * Logic copied from http://en.wikipedia.org/wiki/Xorshift
 * Requires 4 initial random seeds for each work item
 */
__kernel void gen_numbers(__global uint4* seeds, /*size_t nNumbersArg, */__global uint* output)
{
	const uint iWorkItem = get_global_id(0);
#if 1
#define seedsOffset iWorkItem
#define nNumbers NUMBERS_COUNT
#define nWorkItems WORK_ITEMS_COUNT
#define nNumbersByWorkItem (nNumbers / nWorkItems)
#define REMAINDER (nNumbers - nNumbersByWorkItem * WORK_ITEMS_COUNT)
	uint nNumbersInThisWorkItem = nNumbersByWorkItem;
	if (iWorkItem == nWorkItems - 1)
		nNumbersInThisWorkItem += REMAINDER;
#else
	const uint seedsOffset = iWorkItem;
	const uint nNumbers = nNumbersArg;
	const y nWorkItems = get_global_size(0);
	const uint nNumbersByWorkItem = nNumbers / nWorkItems;
	uint nNumbersInThisWorkItem = nNumbersByWorkItem;
	if (iWorkItem == nWorkItems - 1)
		nNumbersInThisWorkItem += nNumbers - nNumbersByWorkItem * nWorkItems;
#endif

	output += iWorkItem * nNumbersByWorkItem;//outputOffset;

	//seeds += seedsOffset;
	//uint4 seed = *seeds;
	uint4 seed = seeds[seedsOffset];
#if 1
	uint x = seed.x, y = seed.y, z = seed.z, w = seed.w;
	for (uint i = 0; i < nNumbersInThisWorkItem; i++) {
	//for (uint i = nNumbersInThisWorkItem; i--;) {
		uint t = x ^ (x << 11);
		x = y; y = z; z = w;
		//output[outputOffset + i] =
		*(output++) =
			w = (w ^ (w >> 19)) ^ (t ^ (t >> 8));
	}
	//*seeds = (uint4)(x, y, z, w);
	seeds[seedsOffset] = (uint4)(x, y, z, w);
#else
	for (uint i = 0; i < nNumbersInThisWorkItem; i++) {
		uint t = seed.x ^ (seed.x << 11);
		seed.xyz = seed.yzw;
		*(output++) = seed.w = (seed.w ^ (seed.w >> 19)) ^ (t ^ (t >> 8));
	}
	seeds[seedsOffset] = seed;
#endif
}

#undef seedsOffset
#undef nNumbers
#undef nWorkItems
#undef nNumbersByWorkItem
#undef REMAINDER