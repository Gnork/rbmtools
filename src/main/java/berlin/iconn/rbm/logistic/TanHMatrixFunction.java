package berlin.iconn.rbm.logistic;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;


public class TanHMatrixFunction extends MatrixFunctions implements ILogistic {
	
	// (tanh(x) + 1) / 2
	
	public  FloatMatrix function(FloatMatrix m) {

		//final FloatMatrix tanhM = MatrixFunctions.tanh(m);
		final FloatMatrix tanhM = MatrixFunctions.tanh(m).add(1).div(2);
		 
		return tanhM;
	}
	
}