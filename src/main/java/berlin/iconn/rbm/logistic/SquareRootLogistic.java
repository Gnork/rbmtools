package berlin.iconn.rbm.logistic;

import org.jblas.FloatMatrix;

public class SquareRootLogistic implements ILogistic {

	@Override
	public FloatMatrix function(FloatMatrix m) {
		
		FloatMatrix md = m.dup();
		float[] data = md.toArray();
		float x;
		for (int i = 0; i < data.length; i++) {
			x = data[i] * 0.5f;
			data[i] = x / (float)Math.sqrt(x*x + 1.0) * 0.5f + 0.5f;
		}
		return md;
	}

}
