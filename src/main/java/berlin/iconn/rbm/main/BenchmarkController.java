/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.main;

import berlin.iconn.rbm.tools.Chooser;
import berlin.iconn.rbm.views.DaydreamController;
import berlin.iconn.rbm.views.ImageBuilderController;
import berlin.iconn.rbm.views.InImageDetectorController;
import berlin.iconn.rbm.views.PRTMAPController;
import berlin.iconn.rbm.views.RunHiddenController;
import berlin.iconn.rbm.views.VanGoghController;
import berlin.iconn.rbm.views.imageviewer.ImageViewerController;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author Moritz
 */
public class BenchmarkController extends AController {

    private BenchmarkModel model;

    @FXML
    private AnchorPane view;

    // Loading
    @FXML
    private ToggleButton btn_OpenShowImages;
    @FXML
    private Label lbl_imageSetSelected;

    // Preprocessing
    @FXML
    private CheckBox cbx_Binarize;
    @FXML
    private CheckBox cbx_Invert;
    @FXML
    private CheckBox cbx_Shuffle;
    @FXML
    private CheckBox cbx_RGB;
    @FXML
    private TextField txt_imageEdgeSize;
    @FXML
    private TextField txt_MinData;
    @FXML
    private TextField txt_MaxData;
    @FXML
    private Label lbl_MseResult;

    // Functions
    @FXML
    private ToggleButton btn_OpenDaydream;
    private DaydreamController daydreamController;
    private Stage daydreamStage;

    @FXML
    private ToggleButton btn_OpenRunHidden;
    private RunHiddenController runHiddenController;
    private Stage runHiddenStage;

    private RunHiddenController testFeaturesController;
    private Stage testFeaturesStage;

    @FXML
    private ToggleButton btn_OpenShowFeatures;
    private RunHiddenController showFeaturesController;
    private Stage showFeaturesStage;

    @FXML
    private ToggleButton btn_OpenImageBuilder;
    private ImageBuilderController imageBuilderController;
    private Stage imageBuilderStage;

    @FXML
    private ToggleButton btn_OpenInImageDetector;
    private InImageDetectorController inImageDetectorController;
    private Stage inImageDetectorStage;

    @FXML
    private ToggleButton btn_vanGogh;
    private VanGoghController vanGoghController;
    private Stage vanGoghStage;

    // Evaluation
    @FXML
    private ComboBox<?> cmb_mAPTests;
    @FXML
    private Font x1;
    @FXML
    private Button btn_cancelTraining;

  // Training
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        PRTMAPController prtmapController = null;
        try {
            prtmapController = (PRTMAPController) loadController("fxml/PRTMAP.fxml");
        } catch (IOException ex) {
            Logger.getLogger(BenchmarkController.class.getName()).log(Level.SEVERE, null, ex);
        }

        ImageViewerController imageViewerController = null;
        try {
            imageViewerController = (ImageViewerController) loadController("fxml/ImageViewer.fxml");
        } catch (IOException ex) {
            Logger.getLogger(BenchmarkController.class.getName()).log(Level.SEVERE, null, ex);
        }
        model = new BenchmarkModel(this, prtmapController, imageViewerController);

        loadImageSet(new File("images/Test_10x5/"));
        this.update();
    }

  // Loading
    @FXML
    private void btn_loadImageSetAction(ActionEvent event) {
        loadImageSet(Chooser.openDirectoryChooser("images"));
    }

    private void loadImageSet(File file) {
        if (file != null) {
            this.model.setImageManager(file);
            this.lbl_imageSetSelected.setText(this.model.getImageManager().getImageSetName());

            this.initCmbImageManager();

            if (this.model.isShowImageViewer()) {
                this.model.getImageViewerController().show();
            }
            this.model.globalUpdate();
        }
    }

    @FXML
    private void btn_OpenShowImagesAction(ActionEvent event) {
        this.model.setShowImageViewer(this.btn_OpenShowImages.isSelected());
        if (this.model.getImageViewerController() != null) {
            if (this.model.isShowImageViewer()) {
                this.model.getImageViewerController().show();
            } else {
                this.model.getImageViewerController().close();
            }
        }
    }

  // Preprocessing
    @FXML
    private void cbx_BinarizeAction(ActionEvent event) {
        this.model.setBinarizeImages(this.cbx_Binarize.isSelected());
    }

    @FXML
    private void cbx_InvertAction(ActionEvent event) {
        this.model.setInvertImages(this.cbx_Invert.isSelected());
    }

    @FXML
    private void cbx_ShuffleAction(ActionEvent event) {
        this.model.setShuffleImages(this.cbx_Shuffle.isSelected());
    }

    @FXML
    private void cbx_RGBAction(ActionEvent event) {
        this.model.setRgb(this.cbx_RGB.isSelected());
    }

    @FXML
    private void txt_imageEdgeSizeKey(KeyEvent event) {
        try {
            this.model.setImageEdgeSize(Integer.parseInt(this.txt_imageEdgeSize.getText()));
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void txt_MinDataKey(KeyEvent event) {
        try {
            this.model.setMinData(Float.parseFloat(this.txt_MinData.getText()));
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            this.model.setMinData(0.0f);
        }
    }

    @FXML
    private void txt_MaxDataKey(KeyEvent event) {
        try {
            this.model.setMaxData(Float.parseFloat(this.txt_MaxData.getText()));
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            this.model.setMaxData(1.0f);
        }
    }

  // Functions
    @FXML
    private void btn_OpenDaydreamAction(ActionEvent event) {
        try {
            if (!btn_OpenDaydream.isSelected()) {
                this.daydreamController.stopDreaming();
                this.daydreamStage.close();
                return;
            }

            this.daydreamController = (DaydreamController) new DaydreamController().loadController("fxml/DaydreamView.fxml");
            Parent root = (Parent) this.daydreamController.getView();

            this.daydreamController.setBenchmarkModel(this.getModel());

            Scene scene = new Scene(root, 600, 400);
            this.daydreamStage = new Stage();
            this.daydreamStage.setTitle("Daydream");
            this.daydreamStage.setScene(scene);

            if (btn_OpenDaydream.isSelected()) {
                this.daydreamStage.show();
            }

            daydreamStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    btn_OpenDaydream.setSelected(false);
                    daydreamController.stopDreaming();
                    daydreamStage.close();
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void btn_OpenRunHiddenAction(ActionEvent event) {
        try {
            if (!btn_OpenRunHidden.isSelected()) {
                this.runHiddenStage.close();
                return;
            }

            this.runHiddenController = (RunHiddenController) new DaydreamController().loadController("fxml/RunHiddenView.fxml");
            Parent root = (Parent) this.runHiddenController.getView();

            this.runHiddenController.setBenchmarkModel(this.getModel());

            Scene scene = new Scene(root, 600, 400);
            this.runHiddenStage = new Stage();
            this.runHiddenStage.setTitle("Run Hidden");
            this.runHiddenStage.setScene(scene);

            if (btn_OpenRunHidden.isSelected()) {
                this.runHiddenStage.show();
            }

            runHiddenStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    btn_OpenRunHidden.setSelected(false);
                    runHiddenStage.close();
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void btn_OpenShowFeaturesAction(ActionEvent event) {

        if (!btn_OpenShowFeatures.isSelected()) {
            this.model.getFeatureViewer().close();
            return;
        }

        this.model.setShowFeatureViewer(this.btn_OpenShowFeatures.isSelected());

        if (this.model.getFeatureViewer() == null) {
            this.model.initFeatureViewer(this);
        }

        if (this.model.isShowFeatureViewer()) {
            this.model.getFeatureViewer().show();
        } else {
            this.model.getFeatureViewer().close();
        }
    }

    @FXML
    private void btn_OpenImageBuilderAction(ActionEvent event) {
        try {
            if (!btn_OpenImageBuilder.isSelected()) {
                this.imageBuilderStage.close();
                return;
            }

            this.imageBuilderController = (ImageBuilderController) new ImageBuilderController().loadController("fxml/ImageBuilderView.fxml");
            Parent root = (Parent) this.imageBuilderController.getView();

            Scene scene = new Scene(root, 667, 400);
            this.imageBuilderStage = new Stage();
            this.imageBuilderStage.setTitle("Image Builder");
            this.imageBuilderStage.setScene(scene);

            if (btn_OpenImageBuilder.isSelected()) {
                this.imageBuilderStage.show();
                this.imageBuilderController.setBenchmarkModel(this.getModel());
            }

            imageBuilderStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    btn_OpenImageBuilder.setSelected(false);
                    imageBuilderStage.close();
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void btn_OpenInImageDetectorAction(ActionEvent event) {
        try {
            if (!btn_OpenInImageDetector.isSelected()) {
                this.inImageDetectorStage.close();
                return;
            }

            this.inImageDetectorController = (InImageDetectorController) new InImageDetectorController().loadController("fxml/InImageDetectorView.fxml");
            Parent root = (Parent) this.inImageDetectorController.getView();

            Scene scene = new Scene(root, 610, 400);
            this.inImageDetectorStage = new Stage();
            this.inImageDetectorStage.setTitle("Image Builder");
            this.inImageDetectorStage.setScene(scene);

            if (btn_OpenInImageDetector.isSelected()) {
                this.inImageDetectorStage.show();
                this.inImageDetectorController.setBenchmarkModel(this.getModel());
            }

            inImageDetectorStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    btn_OpenInImageDetector.setSelected(false);
                    inImageDetectorStage.close();
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void btn_perfomMseTestAction(ActionEvent event) {
        lbl_MseResult.setText(String.valueOf((this.model.getMSE())));
    }

    // Evaluation
    private void initCmbImageManager() {
        List<String> mapTest;
        if (this.model.getImageManager() != null) {
            mapTest = new LinkedList<>(this.model.getImageManager().getGroupNames());
            mapTest.add(0, "All");
        } else {
            mapTest = new LinkedList<>();
        }
        ObservableList mapTestObs = FXCollections.observableList(mapTest);
        this.cmb_mAPTests.setItems(mapTestObs);
        this.model.setSelectedMAPTest(0);
        this.cmb_mAPTests.getSelectionModel().select(this.model.getSelectedMAPTest());

    }

    @FXML
    private void cmb_mAPTestsAction(ActionEvent event) {
        this.model.setSelectedMAPTest(this.cmb_mAPTests.getSelectionModel().getSelectedIndex());
    }

    @FXML
    private void btn_startmAPTestAction(ActionEvent event) {

        // who can do it better?:
        int index = this.cmb_mAPTests.getSelectionModel().getSelectedIndex();
        String name = this.model.getImageManager().getNameFromIndex(index);
        this.model.startMAPTest(name);
        this.model.getPRTMAPController().show();
    }

  // Training
    @FXML
    private void btn_trainAllAction(ActionEvent event) {
        model.trainRBMs();

    }

    @FXML
    private void btn_UpdateAction(ActionEvent event) {
        System.out.println("update action");
        this.model.globalUpdate();
    }

    @Override
    public Node getView() {
        return view;
    }

    /**
     * @return the model
     */
    public BenchmarkModel getModel() {
        return model;
    }

    public void update() {
        this.cbx_Binarize.setSelected(this.model.isBinarizeImages());
        this.cbx_Invert.setSelected(this.model.isInvertImages());
        this.cbx_Shuffle.setSelected(!this.model.isSorted());
        this.btn_OpenShowImages.setSelected(this.model.isShowImageViewer());
        this.btn_OpenShowFeatures.setSelected(this.model.isShowFeatureViewer());
        this.cmb_mAPTests.getSelectionModel().select(this.model.getSelectedMAPTest());
        this.txt_imageEdgeSize.setText(new Integer(this.model.getImageEdgeSize()).toString());
        this.txt_MinData.setText(new Float(this.model.getMinData()).toString());
        this.txt_MaxData.setText(new Float(this.model.getMaxData()).toString());
        this.cbx_RGB.setSelected(this.model.isRgb());
        if (this.model.getImageManager() == null) {
            lbl_imageSetSelected.setText("no image set selected");
        } else {
            lbl_imageSetSelected.setText(this.model.getImageManager().getImageSetName());
        }
    }

    @FXML
    private void btn_cancelAction(ActionEvent event) {
        this.model.cancelTraining();
    }

    @FXML
    private void btn_vanGoghAction(ActionEvent event) {
        try {
            if (!btn_vanGogh.isSelected()) {
                if(this.vanGoghStage != null){
                    this.vanGoghStage.close();
                }
                return;
            }
            
            if(this.vanGoghStage == null || this.vanGoghController == null){

                this.vanGoghController = (VanGoghController) new VanGoghController().loadController("fxml/VanGogh.fxml");
                Parent root = (Parent) this.vanGoghController.getView();

                this.vanGoghController.setBenchmarkModel(this.getModel());

                Scene scene = new Scene(root, 800, 600);
                this.vanGoghStage = new Stage();
                this.vanGoghStage.setTitle("Van Gogh Generator");
                this.vanGoghStage.setScene(scene);
                this.vanGoghStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    public void handle(WindowEvent we) {
                        btn_vanGogh.setSelected(false);
                        vanGoghStage.close();
                    }
                });
            }

            this.vanGoghStage.show();

            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
