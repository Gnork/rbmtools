package berlin.iconn.rbm.rbm;

public interface IRBM {

	public void train(float[][] data, StoppingCondition stop, boolean binarizeHidden, boolean binarizeVisible);
	public float error(float[][] data, boolean binarizeHidden, boolean binarizeVisible);
	public float[][] getHidden(float[][] data, boolean binarizeHidden);
	public float[][] getVisible(float[][] data, boolean binarizeVisible);
	
	public float[][] getWeights();

}
