/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.settings;

import berlin.iconn.rbm.main.AController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author christoph
 */
public class RBMSettingsLearningRateController extends AController {

    @FXML
    private TextField txt_learningRate;
    @FXML
    private AnchorPane view;

    private RBMSettingsLearningRateModel model;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.model = new RBMSettingsLearningRateModel(this);
        this.update();
    }

    @FXML
    private void txt_learningRateKey(KeyEvent event) {
        try {
            this.model.setConstantLearningRate(Float.parseFloat(txt_learningRate.getText()));
        } catch (NumberFormatException e) {

        }
    }

    @Override
    public Node getView() {
        return this.view;
    }

    public RBMSettingsLearningRateModel getModel() {
        return model;
    }

    @Override
    public void update() {
        this.txt_learningRate.setText(new Double(this.model.getConstantLearningRate()).toString());
    }
}
