/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.settings;

import berlin.iconn.rbm.persistence.Conserve;

/**
 *
 * @author moritz
 */
public class RBMSettingsLearningRateModel{
    
    private final RBMSettingsLearningRateController controller;
    @Conserve
    private float constantLearningRate = 0.1f;

    public RBMSettingsLearningRateModel(RBMSettingsLearningRateController controller) {
        this.controller = controller;
    }

    
    /**
     * @return the constantLearningRate
     */
    public float getConstantLearningRate() {
        return constantLearningRate;
    }

    /**
     * @param constantLearningRate the constantLearningRate to set
     */
    public void setConstantLearningRate(float constantLearningRate) {
        this.constantLearningRate = constantLearningRate;
    }
}
