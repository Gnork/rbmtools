package berlin.iconn.rbm.views;

import berlin.iconn.rbm.main.AController;
import berlin.iconn.rbm.statistics.PrecisionRecallTestResult;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Gregor Altastädt
 */
public class PRTMAPController extends AController implements EventHandler {

    @FXML
    private AnchorPane view;

    @FXML

    private PRTMAPModel model;
    private final Stage prtmapStage = new Stage();

    @FXML
    private Button btn_clear;
    
    @FXML
    LineChart<Number, Number> cha_PRTable;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.model = new PRTMAPModel(this);

        Parent root = (Parent) this.getView();
        Scene scene = new Scene(root, 600, 400);
        prtmapStage.setTitle("Map Viewer");
        prtmapStage.setScene(scene);
        prtmapStage.setOnCloseRequest(this);

        this.update();
    }


    public void addGraph(PrecisionRecallTestResult testAll) {
        
        
        XYChart.Series tmpGraph = new XYChart.Series();

        // set title of graph
        tmpGraph.setName(String.format(Locale.ENGLISH, "%s mAP = %6.3f", testAll.getTestGroup(), testAll.getmAP()));

        int precisionIndex = 2, recallIndex = 3;
        // transfer the float array data to the tmpGraph
        
        float [][] pUeberR = testAll.getPrTable();
        for (int i = 0; i < pUeberR[precisionIndex].length; i++) {

            double x = pUeberR[recallIndex][i];
            double y = pUeberR[precisionIndex][i];

            tmpGraph.getData().add(new XYChart.Data(x, y));
        }

        // finally add and draw the tmpGraph to the PRChart
        this.cha_PRTable.getData().add(tmpGraph);
    }
    
    @Override
    public void update() {
    }

    @Override
    public Node getView() {
        return this.view;
    }

    public PRTMAPModel getModel() {
        return this.model;
    }

    @Override
    public void handle(Event t) {
    }

    public void hide() {
        prtmapStage.hide();
    }

    public void show() {
        prtmapStage.show();
    }

    @FXML
    private void btn_clearAction(ActionEvent event) {
        if (cha_PRTable != null) {
            cha_PRTable.getData().clear();
        }
    }
}
