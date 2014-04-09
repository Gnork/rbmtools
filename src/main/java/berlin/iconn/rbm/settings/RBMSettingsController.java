/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.settings;

import berlin.iconn.rbm.rbm.RBMTrainer;
import berlin.iconn.rbm.main.AController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Moritz
 */
public class RBMSettingsController extends AController {
    @FXML
    private AnchorPane view;
    @FXML
    private TreeView<String> trv_rbmSettingsMenue;
    @FXML
    private VBox vbox_rbmSettingsTemplatePane;
    @FXML
    private Button btn_startRBMTraining;
    @FXML
    private Button btn_cancelRBMTraining;
    
    private RBMSettingsModel model;
    
    private RBMTrainer trainer;
    @FXML
    private ScrollPane scp_settings;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        TreeItem<String> settingsRBM = new TreeItem<>("RBM");
        settingsRBM.setExpanded(true);
        
        TreeItem<String> settingsMain = new TreeItem<>("Main");
        TreeItem<String> settingsWeights = new TreeItem<>("Weights");
        TreeItem<String> settingsStoppingCondition = new TreeItem<>("Stopping Conditions");
        TreeItem<String> settingsLearningRate = new TreeItem<>("Learning Rate");
        TreeItem<String> settingsVisualizations = new TreeItem<>("Training Visualizations");
        TreeItem<String> settingsLogger = new TreeItem<>("Logger");
       
        TreeItem[] items = new TreeItem[]{
            settingsMain,
            settingsWeights,
            settingsStoppingCondition,
            settingsLearningRate,
            settingsVisualizations,
            settingsLogger
        }; 
        
        AController[] controllers = new AController[]{
            addSettings(settingsRBM, settingsMain, "fxml/RBMSettingsMain.fxml"),
            addSettings(settingsRBM, settingsWeights, "fxml/RBMSettingsWeights.fxml"),
            addSettings(settingsRBM, settingsStoppingCondition, "fxml/RBMSettingsStoppingCondition.fxml"),
            addSettings(settingsRBM, settingsLearningRate, "fxml/RBMSettingsLearningRate.fxml"),
            addSettings(settingsRBM, settingsVisualizations, "fxml/RBMSettingsVisualizations.fxml"),
            addSettings(settingsRBM, settingsLogger, "fxml/RBMSettingsLogger.fxml")
        };
        
        this.model = new RBMSettingsModel(items, controllers, this);
        
        trv_rbmSettingsMenue.setRoot(settingsRBM);

        trv_rbmSettingsMenue.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);   
        
        trv_rbmSettingsMenue.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>(){

            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> ov, TreeItem<String> oldItem, TreeItem<String> newItem) { 
                TreeItem[] items = model.getTreeItems();
                
                int idx = 0;
                for (int i = 0; i < items.length; i++) {
                    if(items[i].equals(newItem)) {
                        idx = i;
                        break;
                    }
                }        
                vbox_rbmSettingsTemplatePane.getChildren().clear();
                AnchorPane settingsView = (AnchorPane)(model.getControllers()[idx].getView());
                //settingsView.prefWidthProperty().bind(vbox_rbmSettingsTemplatePane.widthProperty());
                vbox_rbmSettingsTemplatePane.getChildren().add(settingsView);    
            }
            
        });
        
        this.trainer = new RBMTrainer();
        
        //vbox_rbmSettingsTemplatePane.prefWidthProperty().bind(scp_settings.widthProperty().subtract(15));
        
        this.update();
    }    

    private AController addSettings(TreeItem<String> root, TreeItem<String> child, String controllerURL) {
            
        AController controller = null;
        try {
            controller = (AController)loadController(controllerURL);
           
        } catch (IOException ex) {
            Logger.getLogger(RBMSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }

               
        root.getChildren().add(child);
        return (AController) controller;
    }
    
    @FXML
    private void btn_startRBMTrainingAction(ActionEvent event) {
//    	trainer.trainSingleRBM(this);
    }

	@FXML
    private void btn_cancelRBMTrainingAction(ActionEvent event) {
		// TODO
		throw new UnsupportedOperationException();
    }

    @Override
    public Node getView() {
        return view;
    }

    public RBMSettingsModel getModel() {
       return this.model;
    }  

    public void update() {
    
    }
}
