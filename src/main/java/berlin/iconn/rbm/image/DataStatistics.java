package berlin.iconn.rbm.image;

import org.jblas.FloatMatrix;

public class DataStatistics {
    
    /**
     * calculate mean vector of FloatMatrix and return as FloatMatrix
     * @param data
     * @return 
     */
	
	public static FloatMatrix getMean(FloatMatrix data) {
		FloatMatrix meanVector = new FloatMatrix(data.getRows());
		
		for(int i = 0; i < data.getRows(); i++) {
			float mean = data.getRow(i).mean();
			meanVector.put(i, mean);
		}
		
		return meanVector;
		
	}
	
}
