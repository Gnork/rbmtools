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
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
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
    @FXML
    private TextField txt_minEdgeSize;
    @FXML
    private CheckBox cbx_binarize;
    @FXML
    private Button btn_export;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.model = new VanGoghModel(this);
        this.update();
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

    @FXML
    private void txt_minEdgeSizeAction(ActionEvent event) {
        this.model.setMinEdgeSize(new Integer(this.txt_minEdgeSize.getText()));
    }

    @FXML
    private void cbx_binarizeAction(ActionEvent event) {
        this.model.setBinarize(this.cbx_binarize.isSelected());
    }
    
    @Override
    public void update(){
        txt_minEdgeSize.setText(Integer.toString(this.model.getMinEdgeSize()));
        cbx_binarize.setSelected(this.model.isBinarize());
    }

    @FXML
    private void btn_exportAction(ActionEvent event) {
        this.model.exportImage();
    }
    
}
