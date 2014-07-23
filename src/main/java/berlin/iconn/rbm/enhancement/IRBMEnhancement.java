package berlin.iconn.rbm.enhancement;
/**
 * 
 * IRBMEnhancement can be added to RBMEnhancer, to be called on training updates
 */
public abstract interface IRBMEnhancement {

	void action(RBMInfoPackage info);
}
