/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.settings;

import berlin.iconn.rbm.persistence.Conserve;
import berlin.iconn.rbm.views.ErrorViewController;
import berlin.iconn.rbm.views.WeightsVisualizationController;
import berlin.iconn.rbm.views.imageviewer.ImageViewerController;

/**
 *
 * @author christoph
 */
public class RBMSettingsVisualizationsModel {

    private final RBMSettingsVisualizationsController controller;
    private final ErrorViewController errorViewController;
    private final WeightsVisualizationController weightsVisualizationController;
	private final ImageViewerController imageViewController;
    
    @Conserve
    private boolean showWeights = false;
    @Conserve
    private boolean showErrorGraph = false;
    @Conserve
    private boolean showFeatures = false;
    @Conserve
    private int weightsInterval = 1;
    @Conserve
    private int errorInterval = 1;
    @Conserve
    private int featuresInterval = 1;

    RBMSettingsVisualizationsModel(RBMSettingsVisualizationsController controller, ErrorViewController errorViewController, WeightsVisualizationController weightsVisualizationController, ImageViewerController imageViewController) {
        this.weightsVisualizationController = weightsVisualizationController;
        this.errorViewController = errorViewController;
        this.imageViewController = imageViewController;
        this.controller = controller;    
    }

    public int getWeightsInterval() {
        return weightsInterval;
    }

    public void setWeightsInterval(int weightsInterval) {
        this.weightsInterval = weightsInterval;
    }

    public int getErrorInterval() {
        return errorInterval;
    }

    public void setErrorInterval(int errorInterval) {
        this.errorInterval = errorInterval;
    }

    public boolean isShowWeights() {
        return showWeights;
    }

    public void setShowWeights(boolean showWeights) {
        this.showWeights = showWeights;
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

    public boolean isShowFeatures() {
		return showFeatures;
	}

	public void setShowFeatures(boolean showFeatures) {
		this.showFeatures = showFeatures;
	}

	public int getFeaturesInterval() {
		return featuresInterval;
	}

	public void setFeaturesInterval(int featuresInterval) {
		this.featuresInterval = featuresInterval;
	}

	/**
     * @return the weightsVisualizationController
     */
    public WeightsVisualizationController getWeightsVisualizationController() {
        return weightsVisualizationController;
    }
    
    public ImageViewerController getImageViewController() {
        return imageViewController;
    }

}
