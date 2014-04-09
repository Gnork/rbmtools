
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.views;

import berlin.iconn.rbm.main.AController;
import berlin.iconn.rbm.main.BenchmarkModel;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

/**
 * FXML Controller class
 *
 * @author Radek
 */
public class ImageBuilderController extends AController {
	
    @FXML
    private ToggleButton btn_hiddenStates;
    @FXML
    private ToggleButton btn_visibleStates;
    @FXML
    private ImageView imgv_Result;
    @FXML
    private GridPane grid;
    @FXML
    private AnchorPane view;

	ImageBuilderModel model;
	
	private List<HiddenImageItemController> hiddenImageItemControllers;
	
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	this.model = new ImageBuilderModel(this);
        this.hiddenImageItemControllers = new ArrayList<HiddenImageItemController>();
    }
    
    @FXML
    private void btn_hiddenStatesAction(ActionEvent event) {
        this.model.setUseHiddenStates(this.btn_hiddenStates.isSelected());
    }

    @FXML
    private void btn_visibleStatesAction(ActionEvent event) {
        this.model.setUseVisibleStates(this.btn_visibleStates.isSelected());
    }
    
    public void setBenchmarkModel(BenchmarkModel benchmarkModel) {
    	this.model.setBenchmarkModel(benchmarkModel);
    	
        List<Image> hiddenImages = this.model.getHiddenImages(80, 80);
        int index = 0;
        for(Image hiddenImage : hiddenImages) {
			try {
				HiddenImageItemController hiddenImageItemController = (HiddenImageItemController)loadController("HiddenImageItem.fxml");
				hiddenImageItemController.getModel().setHiddenImage(hiddenImage);
				hiddenImageItemController.getModel().setUseFeature(false);
				hiddenImageItemController.getModel().setWeight(0.0f);
				hiddenImageItemController.getModel().setIndex(index);
				hiddenImageItemController.setImageBuilderController(this);
				this.hiddenImageItemControllers.add(hiddenImageItemController);
				index++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
    	int amountOfHiddenImages = this.hiddenImageItemControllers.size();
    	
    	int cols = 3;
    	int plusOne = (amountOfHiddenImages % cols != 0) ? 1 : 0;
    	int rows = (int)(amountOfHiddenImages / cols) + plusOne;
    	
    	for(int i = 0; i < rows; i++) {
    		RowConstraints row = new RowConstraints();
    		row.setPrefHeight(80);
    		row.setMaxHeight(80);
    		row.setMinHeight(80);
    		grid.getRowConstraints().add(row);
    	}

    	for(int i = 0; i < cols; i++) {
    		ColumnConstraints column = new ColumnConstraints();
    		column.setPrefWidth(165);
    		column.setMaxWidth(165);
    		column.setMinWidth(165);
    		grid.getColumnConstraints().add(column);
   		}
    	
    	grid.setMaxSize(500, Region.USE_COMPUTED_SIZE);
    	
    	for(int i = 0; i < amountOfHiddenImages; i++) {
    		int row = i % cols;
    		int col = i / cols;
    		
    		HiddenImageItemController hiddenImageItemController = hiddenImageItemControllers.get(i);
    		hiddenImageItemController.update();
    		
    		grid.add(hiddenImageItemController.getView(), row, col);
    	}
    }

    @Override
    public Node getView() {
        return this.view;
    }

    @Override
    public void update() {
    	float[] hiddenData = new float[this.hiddenImageItemControllers.size()];
    	for(int i = 0; i < hiddenData.length; i++) {
    		hiddenData[i] = this.hiddenImageItemControllers.get(i).getModel().getWeight();
    	}
    	this.imgv_Result.setImage(this.model.getVisibleImage(hiddenData, (int)this.imgv_Result.getFitWidth(), (int)this.imgv_Result.getFitHeight()));
    }

}
