/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.main;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;

/**
 *
 * @author Moritz
 */
public abstract class AController implements Initializable {
   
    protected Object loadController(String url) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(url));
        loader.load();
        return loader.getController();
    }

    abstract public void update();
    abstract public Node getView();
    
}
