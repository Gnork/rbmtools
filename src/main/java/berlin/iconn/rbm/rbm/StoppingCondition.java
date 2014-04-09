/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.rbm;

import berlin.iconn.rbm.enhancement.RBMInfoPackage;

/**
 *
 * @author Moritz
 */
public class StoppingCondition {
    private final boolean errorDisabled;
    private final boolean epochsDisabled;
    
    private final int maxEpochs;
    private final float minError;
    private int currentEpochs;
    private float currentError = Float.POSITIVE_INFINITY;

    public StoppingCondition(int maxEpochs, float minError) {
        this.errorDisabled = false;
        this.epochsDisabled = false;
        this.maxEpochs = maxEpochs;
        this.minError = minError;
    } 
    
    public StoppingCondition(int maxEpochs) {
        this.errorDisabled = true;
        this.epochsDisabled = false;
        this.maxEpochs = maxEpochs;
        this.minError = 0;
    }
    
     public StoppingCondition(float minError) {
        this.errorDisabled = false;
        this.epochsDisabled = true;
        this.maxEpochs = Integer.MAX_VALUE;
        this.minError = minError;
    }

    public StoppingCondition(boolean errorDisabled, boolean epochsDisabled, int maxEpochs, float minError, int currentEpochs, float currentError) {
        this.errorDisabled = errorDisabled;
        this.epochsDisabled = epochsDisabled;
        this.maxEpochs = maxEpochs;
        this.minError = minError;
        this.currentEpochs = currentEpochs;
        this.currentError = currentError;
    }

    public StoppingCondition() {
        this.errorDisabled = true;
        this.epochsDisabled = true;
        this.maxEpochs = Integer.MAX_VALUE;
        this.minError = 0;
    }
    
     public boolean isNotDone() {
         return (isErrorDisabled() ? true : getCurrentError() > getMinError()) &&
                 (isEpochsDisabled() ? true : getCurrentEpochs() < getMaxEpochs());
     }
     
     public void update(float error) {
         setCurrentEpochs(getCurrentEpochs() + 1);
         setCurrentError(error);
     }

    /**
     * @return the maxEpochs
     */
    public int getMaxEpochs() {
        return maxEpochs;
    }

    /**
     * @return the minError
     */
    public float getMinError() {
        return minError;
    }

    /**
     * @return the currentEpochs
     */
    public int getCurrentEpochs() {
        return currentEpochs;
    }

    /**
     * @param currentEpochs the currentEpochs to set
     */
    public void setCurrentEpochs(int currentEpochs) {
        this.currentEpochs = currentEpochs;
    }

    /**
     * @return the currentError
     */
    public float getCurrentError() {
        return currentError;
    }

    /**
     * @param currentError the currentError to set
     */
    public void setCurrentError(float currentError) {
        this.currentError = currentError;
    }

    /**
     * @return the errorDisabled
     */
    public boolean isErrorDisabled() {
        return errorDisabled;
    }

    /**
     * @return the epochsDisabled
     */
    public boolean isEpochsDisabled() {
        return epochsDisabled;
    }
    
    public int epochsRemaining() {
    
        return maxEpochs - currentEpochs;
    }
    
    
    
  
    
    
}
