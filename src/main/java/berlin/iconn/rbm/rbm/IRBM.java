package berlin.iconn.rbm.rbm;

/**
 * 
 * Interface to be implemented by all RBM classes and wrapper classes
 */
public interface IRBM {

        /**
         * train RBM until stopping condition terminates
         * @param data
         * @param stop
         * @param binarizeHidden
         * @param binarizeVisible 
         */
	public void train(float[][] data, StoppingCondition stop, boolean binarizeHidden, boolean binarizeVisible);
        
        /**
         * calculate RBM error by given data
         * @param data
         * @param binarizeHidden
         * @param binarizeVisible
         * @return 
         */
	public float error(float[][] data, boolean binarizeHidden, boolean binarizeVisible);
        
        /**
         * returns data representation in hidden layer
         * @param data
         * @param binarizeHidden
         * @return 
         */
	public float[][] getHidden(float[][] data, boolean binarizeHidden);
        
        /**
         * returns reconstructed data in visible layer
         * @param data
         * @param binarizeVisible
         * @return 
         */
	public float[][] getVisible(float[][] data, boolean binarizeVisible);
	
        /**
         * return current weights of RBM
         * @return 
         */
	public float[][] getWeights();

}
