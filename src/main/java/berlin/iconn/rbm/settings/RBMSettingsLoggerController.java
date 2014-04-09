/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.settings;

import berlin.iconn.rbm.main.AController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author christoph
 */
public class RBMSettingsLoggerController extends AController {

    @FXML
    private AnchorPane view;
    //       private CheckBox cbx_continuousLogger;
    @FXML
    private CheckBox cbx_finalLogger;
    private TextField txt_continuousInterval;

    private RBMSettingsLoggerModel model;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.model = new RBMSettingsLoggerModel(this);
        this.update();
    }

    @FXML
    private void cbx_finalLoggerAction(ActionEvent event) {
        this.model.setFinalLoggerOn(cbx_finalLogger.isSelected());
    }

    @Override
    public Node getView() {
        return this.view;
    }

    public RBMSettingsLoggerModel getModel() {
        return model;
    }


    @Override
    public void update() {
        this.cbx_finalLogger.setSelected(this.model.isFinalLoggerOn());
    }

}
