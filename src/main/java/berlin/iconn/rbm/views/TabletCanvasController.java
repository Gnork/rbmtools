package berlin.iconn.rbm.views;

import berlin.iconn.rbm.main.AController;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by G on 12.07.14.
 */
public class TabletCanvasController extends AController implements EventHandler {

    @FXML
    private AnchorPane view;

    @FXML
    private TabletCanvasModel model;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.model = new TabletCanvasModel(this);

       // Parent root = (Parent) this.getView();
       // Scene scene = new Scene(root, 600, 400);

        this.update();
    }
    @Override
    public void update() {
    }

    @Override
    public Node getView() {
        return this.view;
    }

    public TabletCanvasModel getModel() {
        return this.model;
    }

    @Override
    public void handle(Event t) {
    }
}
