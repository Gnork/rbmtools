package berlin.iconn.rbm.enhancement;

import berlin.iconn.rbm.rbm.IRBM;
import berlin.iconn.rbm.rbm.StoppingCondition;
import java.util.LinkedList;
import javafx.concurrent.Task;

public class RBMEnhancer implements IRBM {

    private final IRBM rbm;
    private final LinkedList<IRBMTrainingEnhancement> traningEnhancements;
    private final LinkedList<IRBMEndTrainingEnhancement> endEnhancements;
    private final RBMInfoPackage info;
    public final static int BASE_INTERVAL = 100;
    private Task<Void> task;

    public RBMEnhancer(IRBM rbm) {
        super();
        this.rbm = rbm;
        this.traningEnhancements = new LinkedList<>();
        this.endEnhancements = new LinkedList<>();
        this.info = new RBMInfoPackage(0, rbm.getWeights(), 0);
    }
    /**
     * add Enhancement to Enhancer, to call its action method on update interval
     * @param enhancement
     * @return 
     */
    public boolean addEnhancement(IRBMEnhancement enhancement) {

        boolean added = false;

        if (enhancement instanceof IRBMTrainingEnhancement) {

            traningEnhancements.add((IRBMTrainingEnhancement) enhancement);
            added = true;
        }

        if (enhancement instanceof IRBMEndTrainingEnhancement) {
            endEnhancements.add((IRBMEndTrainingEnhancement) enhancement);
            added = true;
        }

        return added;
    }

    /**
     * works like a wrapper function for RBM training
     * manages training of RBM and calls enhancements on each update epoch
     * @param trainingData
     * @param stop
     * @param useHiddenStates
     * @param useVisibleStates 
     */
    @Override
    public void train(float[][] trainingData, StoppingCondition stop, boolean useHiddenStates, boolean useVisibleStates) {

        setInfo(rbm, trainingData, 0, useHiddenStates, useVisibleStates);
        for (IRBMEnhancement enhancement : this.traningEnhancements) {
            enhancement.action(this.info);
        }

        boolean updateModel;
        while (stop.isNotDone() && !task.isCancelled()) {
            updateModel = true;

            StoppingCondition intervalStop = new StoppingCondition(
                    stop.isErrorDisabled(),
                    false,
                    stop.epochsRemaining() > BASE_INTERVAL
                    ? (stop.getCurrentEpochs() + BASE_INTERVAL) : stop.getMaxEpochs(),
                    stop.getMinError(),
                    stop.getCurrentEpochs(),
                    stop.getCurrentError());

            rbm.train(trainingData, intervalStop, useHiddenStates, useVisibleStates);

            for (IRBMTrainingEnhancement enhancement : this.traningEnhancements) {
                if (intervalStop.getCurrentEpochs() % enhancement.getUpdateInterval() == 0) {
                    if (updateModel) {
                        updateModel = false;
                        setInfo(rbm, trainingData, intervalStop.getCurrentEpochs(), useHiddenStates, useVisibleStates);
                    }
                    enhancement.action(this.info);
                }
            }
            stop.setCurrentEpochs(intervalStop.getCurrentEpochs());
            stop.setCurrentError(intervalStop.getCurrentError());
        }
        setInfo(rbm, trainingData, stop.getMaxEpochs(), useHiddenStates, useVisibleStates);
        for (IRBMEndTrainingEnhancement enhancement : this.endEnhancements) {
            enhancement.action(this.info);
        }
    }

    private void setInfo(IRBM rbm, float[][] trainingData, int epochs, boolean useHiddenStates, boolean useVisibleStates) {
        this.info.setError(rbm.error(trainingData, useHiddenStates, useVisibleStates));
        this.info.setWeights(rbm.getWeights());
        this.info.setEpochs(epochs);
    }

    /**
     * wrapper function
     * @param trainingData
     * @param useHiddenStates
     * @param useVisibleStates
     * @return 
     */
    @Override
    public float error(float[][] trainingData, boolean useHiddenStates, boolean useVisibleStates) {
        return rbm.error(trainingData, useHiddenStates, useVisibleStates);
    }
    
    /**
     * wrapper function
     * @param userData
     * @param useHiddenStates
     * @return 
     */
    @Override
    public float[][] getHidden(float[][] userData, boolean useHiddenStates) {
        return rbm.getHidden(userData, useHiddenStates);
    }

    /**
     * wrapper function
     * @param hiddenData
     * @param useVisibleStates
     * @return 
     */
    @Override
    public float[][] getVisible(float[][] hiddenData, boolean useVisibleStates) {
        return rbm.getVisible(hiddenData, useVisibleStates);
    }

    /**
     * wrapper function
     * @return 
     */
    @Override
    public float[][] getWeights() {
        return rbm.getWeights();
    }

    /**
     * set a task
     * @param task the task to set
     */
    public void setTask(Task<Void> task) {
        this.task = task;
    }

}
