package berlin.iconn.rbm.logistic;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;


public class GaussMatrixFunction extends MatrixFunctions implements ILogistic {
	
	// e^(-x^2)
	
    /**
     *
     * @param m
     * @return
     */
    	
        @Override
	public  FloatMatrix function(FloatMatrix m) {
		
		final FloatMatrix mPow2 = MatrixFunctions.pow(m, 2.0f);
		final FloatMatrix negMPow2 = mPow2.neg();
		final FloatMatrix ePowNegMPow2 = MatrixFunctions.exp(negMPow2);
		 
		return ePowNegMPow2;
		
	}
	
}