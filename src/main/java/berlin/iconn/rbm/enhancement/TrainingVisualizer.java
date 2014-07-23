package berlin.iconn.rbm.enhancement;

/**
 * concrete training visualizer
 * 
 */
public class TrainingVisualizer implements IRBMTrainingEnhancement {

	private final int updateInterval;
	private final IVisualizeObserver visObserver;

	public TrainingVisualizer(int updateInterval, IVisualizeObserver visObserver) {
		super();
		this.updateInterval = updateInterval;
		this.visObserver = visObserver;
	}

	@Override
	public void action(RBMInfoPackage info) {
		System.out.println("Action performed on: " + visObserver.getClass().getName());
		this.visObserver.update(info);
		
	}

	@Override
	public int getUpdateInterval() {
		return updateInterval;
	}
	
	

}
