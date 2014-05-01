package berlin.iconn.rbm.views;

import berlin.iconn.rbm.main.AController;
import berlin.iconn.rbm.main.BenchmarkModel;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;

public class InImageDetectorController extends AController {
	
    @FXML
    private ImageView imgv_Image;
	
    @FXML
    private Label lbl_Recognition;
    
    @FXML
    private GridPane grid_Probabilities;
    
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
            
            int idx = 0;
            for(String name : this.model.getBenchmarkModel().getImageManager().getGroupNames()) {
            	Label label = new Label(name);
            	
            	grid_Probabilities.add(label, 0, idx);
            	idx++;
            }
 
        } else {
            System.out.println("error");
        }
    }
    
    @FXML
    private void imgv_ImageMouseMovedAction(MouseEvent event) {
        
        if(this.model.getImageData() == null) return;
        
    	HashMap<String, Double> probabilityMap = this.model.getProbabilityMap(event.getX(), event.getY());
        
        List<String> keys = new ArrayList(probabilityMap.keySet());
        int index = 0;
        
        Double max = 0.0;
        int maxIdx = -1;
        
        int counter = 0;
        for(String key : keys) {
        	Double value = probabilityMap.get(key); 
        	if(value > max) {
        		max = value;
        		maxIdx = counter;
        	}
        	counter++; 
        }
        
        for(String key : keys) {
            
            final ProgressBar progressBar = new ProgressBar();
            progressBar.setProgress(probabilityMap.get(key));
            
            if(index == maxIdx) {
            	progressBar.setStyle("-fx-accent: red;");
            } else {
            	progressBar.setStyle("-fx-accent: blue;");
            }
            
            this.grid_Probabilities.add(progressBar, 1, index);
            index++;
        }
    	
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