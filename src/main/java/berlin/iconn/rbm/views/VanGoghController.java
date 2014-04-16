/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.views;

import berlin.iconn.rbm.main.AController;
import berlin.iconn.rbm.main.BenchmarkModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author christoph
 */
public class VanGoghController extends AController {

    /**
     * Initializes the controller class.
     */
    
    private BenchmarkModel benchmarkModel;
    @FXML
    private AnchorPane view;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Node getView() {
        return this.view;
    }
    
    public void setBenchmarkModel(BenchmarkModel model){
        this.benchmarkModel = model;
    }
    
}
