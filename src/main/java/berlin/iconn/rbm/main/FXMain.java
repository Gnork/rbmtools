package berlin.iconn.rbm.main;

import berlin.iconn.rbm.cl.CLFloatMatrix;
import berlin.iconn.rbm.cl.OCL;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author christoph
 */
public class FXMain extends Application{
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
        try(final OCL ocl = new OCL()) {
            
            CLFloatMatrix.setUpOCL(ocl);
            
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/ControlCenter.fxml"));         
            Scene scene = new Scene(root, 700, 700); 
            stage.setTitle("ICONN");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            Logger.getLogger(FXMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
}
