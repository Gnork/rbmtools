/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.views;

import berlin.iconn.rbm.main.AController;

import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;

import org.jblas.util.Random;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author turbodiesel
 */
public class ErrorViewController extends AController implements EventHandler {

    @FXML
    private LineChart<Number, Number> chart_line;
    @FXML
    private final NumberAxis xaxis = new NumberAxis();
    @FXML
    private final NumberAxis yaxis = new NumberAxis();
    @FXML
    private AnchorPane view;
    @FXML
    private LineChart.Series<Number, Number> series;

    private ErrorViewModel model;

    private final Stage errorViewStage = new Stage();
    
    private Timeline animation;
    private int inc = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Parent root = (Parent) this.getView();
        Scene scene = new Scene(root, 600, 400);
        errorViewStage.setScene(scene);
        errorViewStage.setOnCloseRequest(this);
        chart_line.setAnimated(false);
        chart_line.setLegendVisible(false);
        
        chart_line.setCreateSymbols(false);
        chart_line.setId("Error Chart");
        chart_line.setTitle("Error Chart");
        xaxis.setAutoRanging(true);
        yaxis.setAutoRanging(true);
        xaxis.setForceZeroInRange(false);

        series = new XYChart.Series<Number, Number>();
        series.setName("error value");

        chart_line.getData().add(series);

        model = new ErrorViewModel(this);
        animation = new Timeline();
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(1000/1), new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                update();
            }
        }));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    @Override
    public Node getView() {
        return view;
    }

    private void buildGraph() {            
          series.getData().add(new XYChart.Data<Number, Number>(
                    getModel().getLastEpoch(), 
                    getModel().getLastError()));
    }

    @Override
    public void update() {
        buildGraph();

    }

    /**
     * @return the model
     */
    public ErrorViewModel getModel() {
        return model;
    }

    public void hide() {
        errorViewStage.hide();
        animation.pause();
    }

    public void show() {
        errorViewStage.show();
        animation.play();
    }

    @Override
    public void handle(Event t) {
    }
    
    public void clear() {
        series.getData().clear();
        
    }

}
