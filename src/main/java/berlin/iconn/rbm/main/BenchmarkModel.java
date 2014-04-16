/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.main;

import berlin.iconn.rbm.settings.RBMSettingsController;
import berlin.iconn.rbm.statistics.PrecisionRecallTester;
import berlin.iconn.rbm.statistics.PrecisionRecallTestResult;
import berlin.iconn.rbm.evaluation.TrainingQualityTest;
import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.image.Pic;
import berlin.iconn.rbm.persistence.Conserve;
import berlin.iconn.rbm.rbm.RBMTrainer;
import berlin.iconn.rbm.settings.RBMSettingsMainController;
import berlin.iconn.rbm.settings.RBMSettingsWeightsController;
import berlin.iconn.rbm.views.FeatureViewer;
import berlin.iconn.rbm.views.PRTMAPController;
import berlin.iconn.rbm.views.imageviewer.ImageViewerController;

import java.io.File;
import java.util.LinkedList;

/**
 * 
 * @author Moritz
 */
public class BenchmarkModel {
  
  private final BenchmarkController               controller;
  private final ImageViewerController                   imageViewerController;
  private FeatureViewer                           featureViewer;
  private final PRTMAPController                  prtmapController;
  private final LinkedList<RBMSettingsController> rbmSettingsList;
  private RBMTrainer                              rbmTrainer;
  
  @Conserve
  private ImageManager                            imageManager      = null;
  @Conserve
  private boolean                                 binarizeImages    = false;
  private boolean                                 showImageViewer   = false;
  private boolean                                 showFeatureViewer = false;
  @Conserve
  private int                                     selectedMAPTest   = 0;
  private boolean                                 isPRTMAPViewerVisible;
  @Conserve
  private boolean                                 sorted            = true;
  @Conserve
  private boolean                                 invertImages      = false;
  @Conserve
  private float                                   minData           = 0.0f;
  @Conserve
  private float                                   maxData           = 1.0f;
  @Conserve
  private int                                     imageEdgeSize     = 28;
private boolean isRgb;
  
  public boolean isBinarizeImages() {
    return binarizeImages;
  }
  
  public int getImageEdgeSize() {
    return imageEdgeSize;
  }
  
  public float getMinData() {
    return minData;
  }
  
  public float getMaxData() {
    return maxData;
  }
  
  public boolean isSorted() {
    return sorted;
  }
  
  public boolean isInvertImages() {
    return invertImages;
  }
  
  public boolean isRgb() {
	  return isRgb;
  }

  public void setRgb(boolean isRgb) {
	  this.isRgb = isRgb;
	    for (int i = 0; i < rbmSettingsList.size(); ++i) {
	      rbmSettingsList.get(i).getModel().getController(RBMSettingsMainController.class).getModel().setRgb(this.isRgb);
	    }
	    this.imageManager.applyChanges(this);
	    this.imageViewerController.getModel().setImages(this.imageManager);
	    this.globalUpdate();
  }
  
  public void setImageEdgeSize(int imageEdgeSize) {
    this.imageEdgeSize = imageEdgeSize;
    this.imageManager.applyChanges(this);
    this.imageViewerController.getModel().setImages(this.imageManager);
    this.globalUpdate();
  }
  
  public int getSelectedMAPTest() {
    return selectedMAPTest;
  }
  
  public void setSelectedMAPTest(int selectedMAPTest) {
    this.selectedMAPTest = selectedMAPTest;
  }
  
  public BenchmarkModel(BenchmarkController controller, PRTMAPController prtmapController, ImageViewerController ivc) {
    this.rbmSettingsList = new LinkedList<>();
    this.controller = controller;
    this.prtmapController = prtmapController;
    this.imageViewerController = ivc;
    this.rbmTrainer = new RBMTrainer();
  }
  
  public void add(RBMSettingsController rbmSettings) {
    int inputSize = this.getInputSize();
    if (rbmSettingsList.size() > 0) {
      inputSize = rbmSettingsList.getLast().getModel().getController(RBMSettingsMainController.class).getModel().getOutputSize();
    }
    rbmSettings.getModel().getController(RBMSettingsMainController.class).getModel().setInputSize(inputSize);
    this.rbmSettingsList.add(rbmSettings);
  }
  
  public void remove(int rbm) {
    System.out.println(rbmSettingsList.size());
    this.rbmSettingsList.remove(rbm);
    System.out.println(rbmSettingsList.size());
    for (int i = rbm; i < rbmSettingsList.size(); ++i) {
      rbmSettingsList.get(i).getModel().getController(RBMSettingsWeightsController.class).getModel().setWeights(null);
    }
    this.globalUpdate();
  }
  
  public LinkedList<RBMSettingsController> getRbmSettingsList() {
    return rbmSettingsList;
  }
  
  public void setImageManager(File file) {
    this.imageManager = new ImageManager(file, this);
    this.imageViewerController.getModel().setImages(imageManager);
    this.rbmTrainer = new RBMTrainer();
    this.globalUpdate();
  }
  
  public boolean isShowImageViewer() {
    return this.showImageViewer;
  }
  
  public boolean isShowFeatureViewer() {
    return this.showFeatureViewer;
  }
  
  public void setInvertImages(boolean invertImages) {
    this.invertImages = invertImages;
    this.imageManager.applyChanges(this);
    this.imageViewerController.getModel().setImages(this.imageManager);
    this.globalUpdate();
  }
  
  public void setBinarizeImages(boolean binarizeImages) {
    this.binarizeImages = binarizeImages;
    this.imageManager.applyChanges(this);
    this.imageViewerController.getModel().setImages(this.imageManager);
    this.globalUpdate();
  }
  
  public void setShuffleImages(boolean shuffled) {
    this.sorted = !shuffled;
    this.imageManager.applyChanges(this);
    this.imageViewerController.getModel().setImages(this.imageManager);
    this.globalUpdate();
  }
  
  public void setMinData(float minData) {
    this.minData = minData;
    this.imageManager.applyChanges(this);
    this.imageViewerController.getModel().setImages(this.imageManager);
    this.globalUpdate();
  }
  
  public void setMaxData(float maxData) {
    this.maxData = maxData;
    this.imageManager.applyChanges(this);
    this.imageViewerController.getModel().setImages(this.imageManager);
    this.globalUpdate();
  }
  
  public void setShowImageViewer(boolean showImageViewer) {
    this.showImageViewer = showImageViewer;
  }
  
  public void setShowFeatureViewer(boolean showFeatureViewer) {
    this.showFeatureViewer = showFeatureViewer;
  }
  
  public void setVisibilityPRTMAPViewer(boolean b) {
    this.isPRTMAPViewerVisible = b;
  }
  
  public boolean isPRTMAPViewerVisible() {
    return isPRTMAPViewerVisible;
  }
  
  public ImageManager getImageManager() {
    return imageManager;
  }
  
  public void startMAPTest(String imageCategory) {
    
    float[][] features = this.rbmTrainer.getHiddenAllRBMs(controller.getModel(), null, showImageViewer);
    PrecisionRecallTester prTester = new PrecisionRecallTester(features, imageManager);
    
    PrecisionRecallTestResult result;
    
    if (imageCategory.equalsIgnoreCase("All")) {
      result = prTester.testAll();
    } else {
      result = prTester.test(imageCategory);
    }
    
    this.prtmapController.addGraph(result);
  }
  
  public ImageViewerController getImageViewerController() {
    return this.imageViewerController;
  }
  
  public void initFeatureViewer(BenchmarkController benchmarkController) {
    this.featureViewer = new FeatureViewer(benchmarkController);
  }
  
  public FeatureViewer getFeatureViewer() {
    return this.featureViewer;
  }
  
  public int getInputSize() {
	  int size = this.imageEdgeSize * this.imageEdgeSize;
	  return (this.isRgb) ? size * 3 : size;
  }
  
  public float[][] getInputData() {
    return imageManager.getImageData();
  }
  
  PRTMAPController getPRTMAPController() {
    return prtmapController;
  }
  
  public void globalUpdate() {
    this.rbmTrainer.updateRBMs(this);
  }
  
  public void updateAllViews() {
    this.controller.update();
    for (RBMSettingsController settingsController : rbmSettingsList) {
      for (AController c : settingsController.getModel().getControllers()) {
        c.update();
      }
    }
  }
  
  public void trainRBMs() {
    this.rbmTrainer.trainAllRBMs(this);
  }
  
  public void setImages(Pic[] images) {
    this.imageViewerController.getModel().setImages(images);
  }

  public float getMSE() {
	  return TrainingQualityTest.getMSE(this);
  }

    void cancelTraining() {
        this.rbmTrainer.cancelTraining();
    }
}
