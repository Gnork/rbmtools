package berlin.iconn.rbm.logistic;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;


public class DefaultLogisticMatrixFunction extends MatrixFunctions implements ILogistic {
	
	// 1.0 / (1.0 + Math.exp(-m_ij))
	
	public  FloatMatrix function(FloatMatrix m) {
		
//		final FloatMatrix negM = m.neg();
//		final FloatMatrix negExpM = MatrixFunctions.exp(negM);
//		final FloatMatrix negExpPlus1M = negExpM.add(1.0);
//		final FloatMatrix OneDivideNegExpPlusOneM = MatrixFunctions.pow(negExpPlus1M, -1.0); 		 
//		return OneDivideNegExpPlusOneM;
		
		
		float[] data = m.toArray();
		for (int i = 0; i < data.length; i++)
			data[i] = 1.f / (float)( 1. + Math.exp(-data[i]) ); // 1 / (1 + e^-x) 
		return m;
	}
	
}
