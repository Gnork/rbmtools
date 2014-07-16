/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.main;

import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.persistence.Creator;
import berlin.iconn.rbm.persistence.Persistor;
import berlin.iconn.rbm.settings.RBMSettingsController;
import berlin.iconn.rbm.tools.Chooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import berlin.iconn.rbm.views.TabletCanvasController;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

/**
 * FXML Controller class
 *
 * @author christoph
 */
public class ControlCenterController extends AController {

    @FXML
    private MenuItem mnu_newRbm;
    @FXML
    private AnchorPane view;
    @FXML
    private VBox vbox;

    private BenchmarkController benchmarkController;
    @FXML
    private MenuItem mnu_saveConfiguration;
    @FXML
    private MenuItem mnu_loadConfiguration;
    @FXML
    private Menu mnu_removeRBM;

    private Persistor persistor;
    private Creator creator;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.persistor = new Persistor();
        this.creator = new Creator();
        this.createBenchmark();
    }

    private void createBenchmark() {
        try {
            benchmarkController = (BenchmarkController) loadController("fxml/Benchmark.fxml");
            AnchorPane benchmarkView = (AnchorPane) (benchmarkController.getView());
            benchmarkView.prefWidthProperty().bind(this.view.widthProperty().subtract(15));
            vbox.getChildren().add(benchmarkView);

        } catch (IOException ex) {
            Logger.getLogger(ControlCenterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addRemover() {
        ObservableList<MenuItem> items = mnu_removeRBM.getItems();
        int rbmIndex = items.size();
        MenuItem mnu = new MenuItem("RBM " + rbmIndex);
        mnu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                MenuItem item = (MenuItem) t.getSource();
                removeRBM(item);
            }

        });
        items.add(mnu);
    }

    public RBMSettingsController createRBM() {
        try {
            RBMSettingsController controller = (RBMSettingsController) loadController("fxml/RBMSettings.fxml");
            AnchorPane rbmSettingsView = (AnchorPane) (controller.getView());
            rbmSettingsView.prefWidthProperty().bind(this.view.widthProperty().subtract(15));
            benchmarkController.getModel().add(controller);
            vbox.getChildren().add(rbmSettingsView);
            addRemover();
            return controller;
        } catch (IOException ex) {
            System.err.println("ERROR: could not load RBMSettingsController");
            Logger.getLogger(ControlCenterController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @FXML
    private void mnu_newRbmAction(ActionEvent event) {
        this.createRBM();
    }


    @Override
    public Node getView() {
        return this.view;
    }

    public BenchmarkController getBenchmarkController() {
        return benchmarkController;
    }

    @FXML
    private void mnu_saveConfigurationAction(ActionEvent event) {
        try {
            this.persistor.save(this.benchmarkController);
        } catch (IOException | ParserConfigurationException | TransformerException ex) {
            System.err.println("ERROR: could not save configuration to file");
            Logger.getLogger(ControlCenterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void mnu_loadConfigurationAction(ActionEvent event) {
        this.reset();
        File file = Chooser.openFileChooser("Persistor");
        if (file != null) {
            try {
                this.creator.load(this, file);
                BenchmarkModel benchmarkModel = benchmarkController.getModel();

                ImageManager imageManager = benchmarkModel.getImageManager();
                String path = imageManager.getImageSetName();

                benchmarkModel.setImageManager(new File("images/" + path));
                benchmarkModel.updateAllViews();

            } catch (ParserConfigurationException | SAXException | IOException ex) {
                System.err.println("ERROR: could not parse file");
                Logger.getLogger(ControlCenterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void reset() {
        ObservableList<Node> children = vbox.getChildren();
        children.remove(0, children.size());
        createBenchmark();
    }

    private void removeRBM(MenuItem menuItem) {
        BenchmarkModel benchmarkModel = benchmarkController.getModel();
        ObservableList<MenuItem> items = mnu_removeRBM.getItems();
        int i = 0;
        for (MenuItem item : items) {
            if (menuItem == item) {
                System.out.println(i);
                benchmarkModel.remove(i);
                vbox.getChildren().remove(i + 1);
                items.remove(i);
                break;
            }
            ++i;
        }
        i = 0;
        for (MenuItem item : items) {
            item.setText("RBM " + (i++));
        }
        benchmarkModel.globalUpdate();
    }

    @Override
    public void update() {

    }
}
