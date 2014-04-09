package berlin.iconn.rbm.logistic;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;


public class RectifierMatrixFunction extends MatrixFunctions implements ILogistic {

	// log(1 + e^x);
	
	public  FloatMatrix function(FloatMatrix m) {
		
		final FloatMatrix ExpM = MatrixFunctions.exp(m);
		final FloatMatrix ExpPlus1M = ExpM.add(1.0f);
		final FloatMatrix LogExpPlusOneM = MatrixFunctions.log10(ExpPlus1M); 
		
		return LogExpPlusOneM;
	}
}