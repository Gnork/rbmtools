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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
    @FXML
    private Button btn_loadImage;
    @FXML
    private ImageView imgv_Image;
    @FXML
    private Button btn_generate;
    
    private VanGoghModel model;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.model = new VanGoghModel(this);
    }    

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Node getView() {
        return this.view;
    }
    
    public ImageView getImageView(){
        return this.imgv_Image;
    }
    
    public void setBenchmarkModel(BenchmarkModel model){
        this.benchmarkModel = model;
    }
    
    public BenchmarkModel getBenchmarkModel(){
        return this.benchmarkModel;
    }

    @FXML
    private void btn_loadImageAction(ActionEvent event) {
        this.model.loadImageFile();
    }

    @FXML
    private void imgv_ImageMouseMovedAction(MouseEvent event) {
    }

    @FXML
    private void btn_generateAction(ActionEvent event) {
        this.model.generateImage();
    }
    
}
