/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.settings;

import berlin.iconn.rbm.persistence.Conserve;
import berlin.iconn.rbm.views.ErrorViewController;
import berlin.iconn.rbm.views.WeightsVisualizationController;

/**
 *
 * @author christoph
 */
public class RBMSettingsVisualizationsModel {

    private final RBMSettingsVisualizationsController controller;
    private final ErrorViewController errorViewController;
    
    @Conserve
    private boolean showErrorGraph = false;
    @Conserve
    private int errorInterval = 1;

    RBMSettingsVisualizationsModel(RBMSettingsVisualizationsController controller, ErrorViewController errorViewController) {
        this.errorViewController = errorViewController;
        this.controller = controller;    
    }

    public int getErrorInterval() {
        return errorInterval;
    }

    public void setErrorInterval(int errorInterval) {
        this.errorInterval = errorInterval;
    }

    public boolean isShowErrorGraph() {
        return showErrorGraph;
    }

    public void setShowErrorGraph(boolean showErrorGraph) {
        this.showErrorGraph = showErrorGraph;
    }

    public ErrorViewController getErrorViewController() {
        return this.errorViewController;
    }
}
