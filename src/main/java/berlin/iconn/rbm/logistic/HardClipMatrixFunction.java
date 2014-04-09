package berlin.iconn.rbm.logistic;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

public class HardClipMatrixFunction extends MatrixFunctions implements
		ILogistic {

	// x < 0 = 0 and x > 0 = 1

	public FloatMatrix function(FloatMatrix m) {
		float[][] duplicateM = m.dup().toArray2();
		for (int y = 0; y < duplicateM.length; y++) {
			for (int x = 0; x < duplicateM[y].length; x++) {
				if (duplicateM[y][x] < 0)
					duplicateM[y][x] = 0;
				else
					duplicateM[y][x] = 1;
			}
		}
		return new FloatMatrix(duplicateM);
	}

}