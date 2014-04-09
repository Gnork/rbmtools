/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.views;

import berlin.iconn.rbm.main.AController;
import berlin.iconn.rbm.enhancement.IVisualizeObserver;
import berlin.iconn.rbm.enhancement.RBMInfoPackage;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Moritz
 */
public class WeightsVisualizationController extends AController implements EventHandler {

    @FXML
    private AnchorPane view;

    private Canvas canvas;

    WeightsVisualizationModel model;
    
    private final Stage weightViewStage = new Stage();

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        Parent root = (Parent) this.getView();
        final int width = 600;
        final int height = 400;
        Scene scene = new Scene(root, width, height);
        this.canvas = new Canvas(width, height);
        view.getChildren().add(canvas);
        weightViewStage.setScene(scene);
        
        this.model = new WeightsVisualizationModel(this, width, height);
        
        update();
        
        weightViewStage.setOnCloseRequest(this);
        
        scene.widthProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                canvas.setWidth(t.doubleValue());
                model.setViewWidth(t.intValue());
            }
        
        });
        
        scene.heightProperty().addListener(new ChangeListener<Number>(){

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                canvas.setHeight(t.doubleValue());
                model.setViewHeight(t.intValue());

            }
        });      
    }


    @Override
    public void update() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        final double width = canvas.getWidth();
        final double height = canvas.getHeight();
        
        g.fillRect(0, 0, width, height);
        g.drawImage(model.getImage(), 0, 0);
    }

    @Override
    public Node getView() {
        return this.view;
    }

    public WeightsVisualizationModel getModel() {
        return this.model;
    }

    public void show() {
        weightViewStage.show();
    }

    public void hide() {
        weightViewStage.hide();
    }

    @Override
    public void handle(Event t) {
        hide();
    }

}
