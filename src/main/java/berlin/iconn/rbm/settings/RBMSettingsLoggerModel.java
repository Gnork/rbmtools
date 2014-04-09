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
public class RBMSettingsLoggerModel{
    private final RBMSettingsLoggerController controller;

    @Conserve
    private boolean finalLoggerOn = true;
    @Conserve
    private int continuousInterval = 1000;

    RBMSettingsLoggerModel(RBMSettingsLoggerController controller) {
        this.controller = controller;
    }

    /**
     * @return the finalLoggerOn
     */
    public boolean isFinalLoggerOn() {
        return finalLoggerOn;
    }

    /**
     * @param endLoggerOn
     */
    public void setFinalLoggerOn(boolean endLoggerOn) {
        this.finalLoggerOn = endLoggerOn;
    }

    /**
     * @return the continuousInterval
     */
    public int getContinuousInterval() {
        return continuousInterval;
    }

    /**
     * @param continuousInterval the continuousInterval to set
     */
    public void setContinuousInterval(int continuousInterval) {
        this.continuousInterval = continuousInterval;
    }
}
