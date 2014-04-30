/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.settings;

import berlin.iconn.rbm.enhancement.RBMEnhancer;
import berlin.iconn.rbm.views.ErrorViewController;
import berlin.iconn.rbm.main.AController;
import berlin.iconn.rbm.views.WeightsVisualizationController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author moritz
 */
public class RBMSettingsVisualizationsController extends AController {

    private RBMSettingsVisualizationsModel model;
    @FXML
    private TextField txt_errorInterval;

    @FXML
    private AnchorPane view;
    @FXML
    private CheckBox cbx_showErrorGraph;
    @FXML
    private Label lbl_errorInterval;


    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ErrorViewController errorViewController = null;
        try {
            errorViewController = (ErrorViewController) loadController("fxml/ErrorView.fxml");
        } catch (IOException ex) {
            Logger.getLogger(RBMSettingsVisualizationsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        lbl_errorInterval.setText("x " + RBMEnhancer.BASE_INTERVAL);
        this.model = new RBMSettingsVisualizationsModel(this, errorViewController);
        
        this.update();
    }
    
    @FXML
    private void cbx_showErrorGraphAction(ActionEvent event) {
        this.model.setShowErrorGraph(cbx_showErrorGraph.isSelected());
        if(cbx_showErrorGraph.isSelected()) {
            this.model.getErrorViewController().show();
        }
        else {
            this.model.getErrorViewController().hide();
        }
    }

    @Override
    public Node getView() {
        return view;
    }
    
    public RBMSettingsVisualizationsModel getModel(){
        return this.model;
       
    }

    @Override
    public void update() {
        this.cbx_showErrorGraph.setSelected(this.model.isShowErrorGraph());
        this.txt_errorInterval.setText(new Integer(this.model.getErrorInterval()).toString());
    }

    @FXML
    private void txt_errorIntervalKey(KeyEvent event) {
        try {
            this.model.setErrorInterval(Integer.parseInt(this.txt_errorInterval.getText()));
        } catch (NumberFormatException e) {

        }
    }
}
