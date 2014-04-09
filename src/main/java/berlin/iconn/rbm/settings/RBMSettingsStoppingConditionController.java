/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.settings;

import berlin.iconn.rbm.main.AController;
import java.net.URL;
import java.util.Observable;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author christoph
 */
public class RBMSettingsStoppingConditionController extends AController{
    @FXML
    private TextField txt_epochs;
    @FXML
    private TextField txt_error;
    @FXML
    private AnchorPane view;   
    @FXML
    private CheckBox cbx_epochs;
    @FXML
    private CheckBox cbx_error;
    
    private RBMSettingsStoppingConditionModel model;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.model = new RBMSettingsStoppingConditionModel(this);
        this.update();
    }    

    @FXML
    private void txt_epochsKey(KeyEvent event) {
        try{
            this.model.setEpochs(Integer.parseInt(this.txt_epochs.getText()));
        }catch(NumberFormatException e){
            
        }
    }

    @FXML
    private void txt_errorKey(KeyEvent event) {
        try{
            this.model.setError(Float.parseFloat(this.txt_error.getText()));
        }catch(NumberFormatException e){
            
        }
    }

    @Override
    public Node getView() {
        return this.view;
    }
    
    public RBMSettingsStoppingConditionModel getModel(){
        return this.model;
    }

    @FXML
    private void cbx_epochsAction(ActionEvent event) {
        this.model.setEpochsOn(cbx_epochs.isSelected());
    }

    @FXML
    private void cbx_errorAction(ActionEvent event) {
        this.model.setErrorOn(cbx_error.isSelected());
    }

    @Override
    public void update() {
        this.cbx_epochs.setSelected(this.model.isEpochsOn());
        this.cbx_error.setSelected(this.model.isErrorOn());
        this.txt_epochs.setText(new Integer(this.model.getEpochs()).toString());
        this.txt_error.setText(new Double(this.model.getError()).toString());
    }
}
