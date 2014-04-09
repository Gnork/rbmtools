package berlin.iconn.rbm.views;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import berlin.iconn.rbm.main.AController;

public class HiddenImageItemController extends AController {

	HiddenImageItemModel model;
	
	@FXML
	private ImageView imgv_HiddenImage;
	@FXML
	private CheckBox cbx_UseHiddenImage;
	@FXML
	private TextField txt_Weight;
	
    @FXML
    private AnchorPane view;

	private ImageBuilderController imageBuilderController;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	model = new HiddenImageItemModel(this);
    	txt_Weight.setDisable(true);
    }
    
    @FXML
    private void cbx_UseHiddenImageAction(ActionEvent event) {
        this.model.setUseFeature(this.cbx_UseHiddenImage.isSelected());
        this.update();
    }
    
    @FXML
    private void txt_WeightKeyReleased(KeyEvent event) {
        try {
            this.model.setWeight(Float.parseFloat(this.txt_Weight.getText()));
        } catch (NumberFormatException ex) {
        	ex.printStackTrace();
        }
    }
	
	@Override
	public Node getView() {
		return this.view;
	}

	@Override
	public void update() {
		this.imgv_HiddenImage.setImage(this.model.getHiddenImage());
		this.cbx_UseHiddenImage.setSelected(this.model.isUseFeature());
		this.txt_Weight.setDisable(!this.model.isUseFeature());
		this.txt_Weight.setText(Float.valueOf(this.model.getWeight()).toString());
		this.imageBuilderController.update();
	}

	public HiddenImageItemModel getModel() {
		return model;
	}

	public void setImageBuilderController(ImageBuilderController imageBuilderController) {
		this.imageBuilderController = imageBuilderController;
	}
	
}
