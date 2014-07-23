package berlin.iconn.rbm.enhancement;

/**
 * 
 * Interface for enhancements to be updated during training
 */
public interface IRBMTrainingEnhancement extends IRBMEnhancement{

	int getUpdateInterval();
}
