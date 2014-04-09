/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.tools;

import java.io.File;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Radek
 */
public class Chooser {
    
    public static File openFileChooser(String initialDirectory){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(initialDirectory));
        Stage fileChooserStage = new Stage();
        File file = fileChooser.showOpenDialog(fileChooserStage);
        return file;
    }
    
    public static File openDirectoryChooser(String initialDirectory){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(initialDirectory));
        Stage fileChooserStage = new Stage();
        File file = directoryChooser.showDialog(fileChooserStage);
        return file;
    }
}
