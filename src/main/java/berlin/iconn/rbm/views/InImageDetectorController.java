package berlin.iconn.rbm.views;

import berlin.iconn.rbm.main.AController;
import berlin.iconn.rbm.main.BenchmarkModel;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;

public class InImageDetectorController extends AController {
	
    @FXML
    private ImageView imgv_Image;
	
    @FXML
    private Label lbl_Recognition;
    
    @FXML
    private TextArea txt_Probabilities;
    
    private InImageDetectorModel model;
    
    private Rectangle rect;
    
    @FXML
    private AnchorPane view;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.model = new InImageDetectorModel(this);
        this.update();
    }

    @FXML
    private void btn_loadImageAction(ActionEvent event) {
        Image image = this.model.loadImage((int) imgv_Image.getFitWidth(), (int) imgv_Image.getFitHeight());
        if (!image.isError()) {
            this.imgv_Image.setImage(image);
            this.model.detection();
        } else {
            System.out.println("error");
        }
    }
    
    @FXML
    private void btn_runDetectionAction(ActionEvent event) {
    	this.model.detection();
    }
    
    @FXML
    private void imgv_ImageMouseMovedAction(MouseEvent event) {
        
        if(this.model.getImageData() == null) return;
        
    	TreeMap<Double, String> probabilityMap = this.model.getProbabilityMap(event.getX(), event.getY());
        
        String probabilitiesText = "";
        for(Double distance : probabilityMap.keySet()) {
            probabilitiesText += probabilityMap.get(distance) + " " + distance + '\n';
        }
        
        this.txt_Probabilities.setText(probabilitiesText);
    	
    	if(rect != null) {
    		view.getChildren().remove(rect);
    	}
    	
    	rect = RectangleBuilder.create()
            .x(event.getX())
            .y(event.getY() + 30)
            .width(28)
            .height(28)
            .stroke(Color.RED)
            .fill(Color.TRANSPARENT)
            .build();
    	
    	view.getChildren().add(rect);
    	
    }

    @Override
    public Node getView() {
        return this.view;
    }
    
    @Override
    public void update(){
        
    }

	public void setBenchmarkModel(BenchmarkModel benchmarkModel) {
		this.model.setBenchmarkModel(benchmarkModel);
	}

}