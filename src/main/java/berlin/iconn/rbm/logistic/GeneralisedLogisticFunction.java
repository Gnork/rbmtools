package berlin.iconn.rbm.logistic;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

public class GeneralisedLogisticFunction extends MatrixFunctions implements ILogistic {
	
	/*
	http://en.wikipedia.org/wiki/Generalised_logistic_function
	A + (K-A) / ((1+exp(-B *x)) 1/v)
	
	A: the lower asymptote;
	K: the upper asymptote. If A=0 then K is called the carrying capacity;
	B: the growth rate;
	m>0 : affects near which asymptote maximum growth occurs.
	*/
	
	@Override
	public FloatMatrix function(FloatMatrix m) {
		return function(m, 0, 1, 1);
	}
	
	public FloatMatrix function(FloatMatrix m, float A, float K, float B) {
		final FloatMatrix negM = m.neg().mmul(B);
		final FloatMatrix negExpM = MatrixFunctions.exp(negM);
		final FloatMatrix negExpPlus1M = negExpM.add(1.0f);
		
		final FloatMatrix oneDivideNegExpPlusOneM = MatrixFunctions.pow(negExpPlus1M, -1 * (A + (K-A))); 
		
		return oneDivideNegExpPlusOneM;
	}

}
