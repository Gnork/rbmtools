package berlin.iconn.rbm.rbm.cuda;

import org.jblas.FloatMatrix;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * Helper class and data holder for matrices and their operations.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MatrixDimensions {

  // dimensions
  private final int m;
  private final int n;
  private final int k;

  // leading dimensions
  private final int ldA;
  private final int ldB;
  private final int ldC;

  // transpose?
  private final boolean transposeA;
  private final boolean transposeB;

  /**
   * Creates matrix dimensions from two matrices. Transpose behaviour is that
   * nothing will be transposed.
   */
  public MatrixDimensions(FloatMatrix a, FloatMatrix b) {
    this(a, b, false, false);
  }

  /**
   * Creates matrix dimensions from two matrices.
   * 
   * @param a matrix A
   * @param b matrix B
   * @param transposeA true if transpose A
   * @param transposeB true if tranpose B
   */
  public MatrixDimensions(FloatMatrix a, FloatMatrix b, boolean transposeA, boolean transposeB) {
    this.transposeA = transposeA;
    this.transposeB = transposeB;
    int m = a.getRows();
    int n = b.getColumns();
    int k = a.getColumns();

    // leading dimensions
    int ldA = a.getRows();
    int ldB = b.getRows();
    int ldC = a.getRows();

    // recalculate the parameters for transposes
    if (transposeA && transposeB) {
      m = a.getColumns();
      n = b.getRows();
      k = b.getColumns();
      ldC = a.getColumns();
    } else if (transposeB) {
      n = b.getRows();
    } else if (transposeA) {
      m = a.getColumns();
      k = a.getRows();
      ldC = a.getColumns();
    }

    this.m = m;
    this.n = n;
    this.k = k;
    this.ldA = ldA;
    this.ldB = ldB;
    this.ldC = ldC;
  }

  public int getM() {
    return m;
  }

  public int getN() {
    return n;
  }

  public int getK() {
    return k;
  }

  public int getLdA() {
    return ldA;
  }

  public int getLdB() {
    return ldB;
  }

  public int getLdC() {
    return ldC;
  }

  public boolean isTransposeA() {
    return transposeA;
  }

  public boolean isTransposeB() {
    return transposeB;
  }

  @Override
  public String toString() {
    return "MatrixDimension [m=" + this.m + ", n=" + this.n + ", k=" + this.k
        + ", ldA=" + this.ldA + ", ldB=" + this.ldB + ", ldC=" + this.ldC
        + ", transposeA=" + this.transposeA + ", transposeB=" + this.transposeB
        + "]";
  }

}
