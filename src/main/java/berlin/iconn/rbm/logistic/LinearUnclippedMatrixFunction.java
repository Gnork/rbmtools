package berlin.iconn.rbm.logistic;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

public class LinearUnclippedMatrixFunction extends MatrixFunctions implements
		ILogistic {

	// linear and no clipping

	public FloatMatrix function(FloatMatrix m) {
		return MatrixFunctions.abs(m);
	}

}