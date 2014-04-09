package berlin.iconn.rbm.logistic;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

public class TanhOfXPow3MatrixFunction extends MatrixFunctions implements ILogistic {

	// tanh(x)

	public FloatMatrix function(FloatMatrix m) {

		final FloatMatrix mPow3 = MatrixFunctions.pow(m, 3);
		final FloatMatrix tanhMPow3 = MatrixFunctions.tanh(mPow3);
		final FloatMatrix tanhM = MatrixFunctions.tanh(tanhMPow3).add(1).div(2);

		return tanhM;
	}

}