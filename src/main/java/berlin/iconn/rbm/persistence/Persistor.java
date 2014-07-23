/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.persistence;

import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.main.AController;
import berlin.iconn.rbm.main.BenchmarkController;
import berlin.iconn.rbm.main.BenchmarkModel;
import berlin.iconn.rbm.settings.RBMSettingsController;
import berlin.iconn.rbm.settings.RBMSettingsModel;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 *
 * saves configuration which can be loaded by Creator class
 *
 */
public class Persistor {
    private final String baseFolder = "Persistor";
    
    
    /**
     * save all model fields with Conserve annotation
     * @param benchmarkController
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerConfigurationException
     * @throws TransformerException 
     */
    public void save(BenchmarkController benchmarkController) throws IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException{     
        BenchmarkModel benchmarkModel = benchmarkController.getModel();
        benchmarkModel.globalUpdate();
        LinkedList<RBMSettingsController> rbmSettingsList = benchmarkModel.getRbmSettingsList();
        
        String dateString = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());		
        String xmlFolder = this.baseFolder;
        String xmlLocation = dateString + ".xml";
        
        Path xmlFolderPath = FileSystems.getDefault().getPath(xmlFolder);
        Path xmlLocationPath = FileSystems.getDefault().getPath(xmlFolder, xmlLocation);

        if(Files.notExists(xmlFolderPath, LinkOption.NOFOLLOW_LINKS)){
                Files.createDirectory(xmlFolderPath);
        }
        File xmlFile = new File(xmlFolder + "/" + xmlLocation);
        StreamResult result = new StreamResult(xmlFile);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // root
        Element rootElement = doc.createElement("root");
        doc.appendChild(rootElement);
        
        conserve(benchmarkModel, rootElement, doc);
        
        int id = 0;
        for(RBMSettingsController settingsController : rbmSettingsList){
            Element rbmElement = doc.createElement("rbm");
            rootElement.appendChild(rbmElement);

            Attr idAttr = doc.createAttribute("id");
            idAttr.setValue(Integer.toString(id++));
            rbmElement.setAttributeNode(idAttr);
            
            saveSettings(settingsController, rbmElement, doc);           
        }
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        transformer.transform(source, result);
        System.out.println("Save Configuration");
    }
    
    private void saveSettings(RBMSettingsController settingsController, Element parent, Document doc){
        RBMSettingsModel settingsModel = settingsController.getModel();
        
        AController[] controllers = settingsModel.getControllers();
        for(int i = 0; i < controllers.length; ++i){
            try {
                Method method = controllers[i].getClass().getMethod("getModel");
                Object model = method.invoke(controllers[i], new Object[]{});
                conserve(model, parent, doc);               
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                System.err.println("ERROR: Could not get Model with Reflection");
                Logger.getLogger(Persistor.class.getName()).log(Level.SEVERE, null, ex);
            }          
        }        
    }
    
    private void conserve(Object model, Element parent, Document doc){
        boolean hasConserveAnnotation = false;
        String className = model.getClass().getSimpleName();
        Element modelElement = doc.createElement(className);
        
        for(Field field : model.getClass().getDeclaredFields()){
            Annotation[] annotations = field.getDeclaredAnnotations();
            
            for(int i = 0; i < annotations.length; ++i){
                if(annotations[i].annotationType().equals(Conserve.class)){                   
                    field.setAccessible(true);
                    Class type = field.getType();
                    String name = field.getName();
                    String value = getFieldValue(field, model);
                    if(value != null){
                        hasConserveAnnotation = true;
                        createDataElement(type.getSimpleName(), name, value, modelElement, doc);
                    }else{
                        System.err.println("ERROR: Could not conserve field " + name + " of type " + type.getSimpleName());
                    }
                    break;
                }
            }         
        }
        if(hasConserveAnnotation){
           parent.appendChild(modelElement);
        }
    }
    
    private void createDataElement(String type, String name, String value, Element parent, Document doc){
        Element dataElement = doc.createElement("data");
        parent.appendChild(dataElement);
        
        Attr typeAttr = doc.createAttribute("type");
        typeAttr.setValue(type);
        dataElement.setAttributeNode(typeAttr);
        
        Attr nameAttr = doc.createAttribute("name");
        nameAttr.setValue(name);
        dataElement.setAttributeNode(nameAttr);
        
        Text valueText = doc.createTextNode(value);
        dataElement.appendChild(valueText);
    }
    
    private String getFieldValue(Field field, Object model){
        Object value = null;
        try {
            value = field.get(model);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            System.err.println("ERROR: Could not get value from field");
            Logger.getLogger(Persistor.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(value == null) return null;
        if(value.getClass().isArray()){
            int len1 = Array.getLength(value);
            if(len1 > 0){
                Object[] array1d = new Object[len1];
                for (int i = 0; i < len1; ++i) {
                    array1d[i] = Array.get(value, i);
                }
                if(array1d[0].getClass().isArray()){
                    int len2 = Array.getLength(array1d[0]);
                    if(len2 > 0){
                        Object[][] array2d = new Object[len1][len2];
                        for(int i = 0; i < len1; ++i){
                            for(int j = 0; j < len2; ++j){
                                array2d[i][j] = Array.get(array1d[i], j);
                            }
                        }                  
                        return array2dToString(array2d);
                    }
                }
                return arrayToString(array1d);
            }
        }
        return objectToString(value);
    }
    // image viewer to string
    private String imageManagerToString(ImageManager im){
        return im.getImageSetName();
    }
    // object to string
    private String objectToString(Object o){
        if(o instanceof ImageManager) {
        	return imageManagerToString((ImageManager)o);
        }
        return o.toString();
    }
    // array to string
    private String arrayToString(Object[] n){
        if(n.length == 0) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(n[0]);
        for(int i = 1; i < n.length; ++i){
            sb.append(",");
            sb.append(objectToString(n[i]));
        }
        return sb.toString();
    }
    // two dimensional array to string
    private String array2dToString(Object[][] n){
        if(n.length == 0 || n[0].length == 0) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(arrayToString(n[0]));
        for(int i = 1; i < n.length; ++i){
            sb.append(";");
            sb.append(arrayToString(n[i]));
        }
        return sb.toString();
    }
}
