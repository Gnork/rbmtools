package berlin.iconn.rbm.logistic;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

public class LinearInterpolatedMatrixFunction extends MatrixFunctions implements
		ILogistic {

	// Interpolated between 0 and 1

	public FloatMatrix function(FloatMatrix m) {

		float[][] duplicateM = m.dup().toArray2();
		float max = m.max();
		float min = m.min();

		float maxMinusMin = max - min;
		for (int y = 0; y < duplicateM.length; y++) {
			for (int x = 0; x < duplicateM[y].length; x++) {
				duplicateM[y][x] = (duplicateM[y][x] - min) / maxMinusMin;
			}
		}
		return new FloatMatrix(duplicateM);
	}

}