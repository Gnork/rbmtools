package berlin.iconn.rbm.views;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import berlin.iconn.rbm.enhancement.IVisualizeObserver;
import berlin.iconn.rbm.enhancement.RBMInfoPackage;
import berlin.iconn.rbm.image.DataConverter;
import berlin.iconn.rbm.image.ImageViewer;
import berlin.iconn.rbm.image.Pic;
import berlin.iconn.rbm.main.AController;
import berlin.iconn.rbm.main.BenchmarkController;
import berlin.iconn.rbm.rbm.RBMTrainer;
import berlin.iconn.rbm.settings.RBMSettingsController;
import berlin.iconn.rbm.settings.RBMSettingsMainController;
import berlin.iconn.rbm.views.imageviewer.ImageViewerController;

public class FeatureViewer extends AController  {
  
  private BenchmarkController benchmarkController;
  ImageViewerController       imageViewerController;
  
  public FeatureViewer(BenchmarkController benchmarkController) {
    try {
      imageViewerController = (ImageViewerController) loadController("fxml/ImageViewer.fxml");
    } catch (IOException ex) {
      Logger.getLogger(BenchmarkController.class.getName()).log(Level.SEVERE, null, ex);
    }
    this.benchmarkController = benchmarkController;
  }
  
  @Override
  public void update() {
    List<RBMSettingsController> rbmSettingsControllers = this.benchmarkController.getModel().getRbmSettingsList();
    
    int outputSize = ((RBMSettingsMainController) (rbmSettingsControllers.get(rbmSettingsControllers.size() - 1).getModel()
        .getController(RBMSettingsMainController.class))).getModel().getOutputSize();
    
    RBMTrainer rbmTrainer = new RBMTrainer();
    
    Pic[] pics = new Pic[outputSize];
    
    for (int i = 0; i < outputSize; i++) {
      float[] hiddenData = new float[outputSize];
      hiddenData[i] = 1.0f;
      
      float[] visibleData = rbmTrainer.getVisibleAllRBMs1D(benchmarkController.getModel(), hiddenData, false);
      
      BufferedImage image = DataConverter.pixelDataToImage(visibleData, 0, this.benchmarkController.getModel().isRgb());
      
      Pic pic = new Pic();
      pic.setDisplayImage(image);
      pic.setOrigWidth(image.getWidth());
      pic.setOrigHeight(image.getHeight());
      pic.setRank(i);
      pics[i] = pic;
    }
    
    imageViewerController.getModel().setImages(pics);
    // return pics;
  }
  
  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public Node getView() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public void show() {
    if (this.benchmarkController.getModel().getRbmSettingsList().size() == 0) {
      System.out.println("Please create a RBM first. (Edit > add RBM)");
      return;
    }
    update();
    imageViewerController.show();
  }
  
  public void close() {
    imageViewerController.close();
  }
  
}
