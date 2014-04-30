package berlin.iconn.rbm.views.imageviewer;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import com.badlogic.gdx.math.Vector2;
import berlin.iconn.rbm.main.AController;

public class ImageViewerController extends AController implements EventHandler {
  
  private final Stage      viewStage = new Stage();
  
  private ImageViewerModel model;
  
  @FXML
  private AnchorPane       view;
  Canvas                   canvas;
  Scene                    scene;
  
  private final int width = 600;
  private final int height = 400;
  
  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    
    canvas = new Canvas(width, height);
    
    Group root = new Group();
    root.getChildren().add(canvas);
    scene = new Scene(root, width, height);
    
    viewStage.setScene(scene);
    viewStage.setOnCloseRequest(this);
    model = new ImageViewerModel(this);
    
    // add event listener:
    ChangeListener<Number> onResize = new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newHeight) {
        
        ImageViewerController.this.model.setSize(new Vector2((float) ImageViewerController.this.scene.widthProperty().get(),
            (float) ImageViewerController.this.scene.heightProperty().get()));
        ImageViewerController.this.model.draw();
      }
    };
    
    scene.widthProperty().addListener(onResize);
    scene.heightProperty().addListener(onResize);
    
    scene.setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouse) {
        ImageViewerController.this.model.onMouseDown(mouse);
        ImageViewerController.this.model.draw();
      }
    });
    
    scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouse) {
      }
    });
    
    scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouse) {
        ImageViewerController.this.model.onMouseDragging(mouse);
        ImageViewerController.this.model.draw();
      }
    });
    
    scene.setOnScroll(new EventHandler<ScrollEvent>() {
      @Override
      public void handle(ScrollEvent scroll) {
        ImageViewerController.this.model.onMouseWheel(scroll);
        ImageViewerController.this.model.draw();
        
        String zf = ImageViewerController.this.model.camera.getZoomFactor() + "";
        if (zf.length() > 3)
          zf = zf.substring(0, 3);
        ImageViewerController.this.viewStage.setTitle("Zoomfactor: " + zf + "x");
      }
    });
    
    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent k) {
        ImageViewerController.this.model.onKeyPressed(k);
        ImageViewerController.this.model.draw();
      }
    });
    
  }
  
  public ImageViewerModel getModel() {
    return this.model;
  }
  
  public void show() {
    viewStage.show();
  }
  
  public void close() {
    viewStage.close();
  }
  
  public Stage getStage(){
      return viewStage;
  }
  
  @Override
  public Node getView() {
    return view;
  }
  
  @Override
  public void handle(Event arg0) {
    // TODO Auto-generated method stub
  }
  
  @Override
  public void update() {
    // TODO Auto-generated method stub
  }
  
}
