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
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author christoph
 */
public class FaceRepairController extends AController {

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
    
    private FaceRepairModel model;
    @FXML
    private TextField txt_minEdgeSize;
    @FXML
    private CheckBox cbx_binarize;
    @FXML
    private Button btn_export;
    @FXML
    private Button btn_repair;
    @FXML
    private Label lbl_mousePos;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.model = new FaceRepairModel(this);
        this.imgv_Image.setPreserveRatio(true);
        Rectangle2D viewPort = new Rectangle2D(0, 0, 400, 300);
        this.imgv_Image.setViewport(viewPort);
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
    private void btn_repairAction(ActionEvent event) {
        this.model.repairImage();
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

    @FXML
    private void imgv_ImageMouseClicked(MouseEvent event) {
        lbl_mousePos.setText("x: " + event.getX() + ", y: " + event.getY());
    }
    
}

