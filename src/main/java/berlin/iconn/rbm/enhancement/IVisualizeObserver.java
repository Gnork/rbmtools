package berlin.iconn.rbm.enhancement;

/**
 * Interface to be implemented by TrainingVisualizations
 * @author christoph
 */
public interface IVisualizeObserver {

    /**
     * update visualization with data in info package
     * @param pack 
     */
	public void update(RBMInfoPackage pack);

}
