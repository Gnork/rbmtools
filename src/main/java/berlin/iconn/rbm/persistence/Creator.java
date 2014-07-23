/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.persistence;

import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.main.AController;
import berlin.iconn.rbm.main.BenchmarkModel;
import berlin.iconn.rbm.main.ControlCenterController;
import berlin.iconn.rbm.settings.RBMSettingsController;
import berlin.iconn.rbm.settings.RBMSettingsModel;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * Creator loads xml configuration stored by Persistor class
 */
public class Creator {
    
    /**
     * load configuration from hard drive
     * @param controller
     * @param file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public void load(ControlCenterController controller, File file) throws ParserConfigurationException, SAXException, IOException{     
        System.out.println("Load Configuration");
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        
        NodeList benchmarkList = doc.getElementsByTagName("BenchmarkModel");
        NodeList rbmList = doc.getElementsByTagName("rbm");
        
        if(benchmarkList.getLength() == 1){
            loadBenchmark(controller, benchmarkList.item(0));
        }else{
            if(benchmarkList.getLength() > 1){
                System.err.println("ERROR: too many benchmarks defined in data");
            }else{
                System.err.println("ERROR: no benchmark defined in data");
            }
        }
             
        loadRBMs(controller, rbmList);
    }
    
    private void loadBenchmark(ControlCenterController controller, Node benchmark){
        BenchmarkModel benchmarkModel = controller.getBenchmarkController().getModel();
        create(benchmarkModel, (Element)benchmark);
    }
    
    private void loadRBMs(ControlCenterController controller, NodeList rbmNodes){
        BenchmarkModel benchmarkModel = controller.getBenchmarkController().getModel();
        for(int i = 0; i < rbmNodes.getLength(); ++i){
            RBMSettingsController settingsController = controller.createRBM();
            loadRBM(settingsController, (Element)rbmNodes.item(i));
        }        
    }
    
    private void loadRBM(RBMSettingsController settingsController, Element rbmElement){
        RBMSettingsModel settingsModel = settingsController.getModel();
        AController[] controllers = settingsModel.getControllers();
        for(int i = 0; i < controllers.length; ++i){
            try {
                Method method = controllers[i].getClass().getMethod("getModel");
                Object model = method.invoke(controllers[i], new Object[]{});
                String className = model.getClass().getSimpleName();
                NodeList modelNodes = rbmElement.getElementsByTagName(className);
                create(model, (Element)modelNodes.item(0));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                System.err.println("ERROR: Could not get Model with Reflection");
                Logger.getLogger(Persistor.class.getName()).log(Level.SEVERE, null, ex);
            }          
        }
    }
    
    private void create(Object model, Element modelElement){
        String[][] data = parseData(modelElement.getChildNodes());
        Field[] fields = model.getClass().getDeclaredFields();
        
        //Remember matches for validation
        boolean[] dataFound = new boolean[data.length];
        boolean[] fieldFound = new boolean[fields.length];
        
        for(int i = 0; i < fields.length; ++i){
            Field field = fields[i];
            Annotation[] annotations = field.getDeclaredAnnotations();
            
            for(int j = 0; j < annotations.length; ++j){
                if(annotations[j].annotationType().equals(Conserve.class)){
                    fieldFound[i] = true;
                    String type = field.getType().getSimpleName();
                    String name = field.getName();
                    
                    //Find data set belonging to field
                    for(int k = 0; k < data.length; ++k){
                        //correct data set is equivalent in name and type
                        if(data[k][0].equals(name) && data[k][1].equals(type)){
                            writeDataToField(field, model, data[k][2]);
                            
                            dataFound[k] = true;
                            fieldFound[i] = false;
                            break;
                        }
                    }
                    break;
                }           
            }
        }
        // Check if the same fields exist in model and data
        for(int i = 0; i < dataFound.length; ++i){
            if(!dataFound[i]){
                System.err.println("Found data which is not existent in the model anymore:");
                System.err.println("Class: " + model.getClass().getSimpleName()
                + ", Name: " + data[i][0]
                + ", Type: " + data[i][1]);
            }
        }
        for(int i = 0; i < fieldFound.length; ++i){
            if(fieldFound[i]){
                System.err.println("Found field which is not existent in the data:");
                System.err.println("Class: " + model.getClass().getSimpleName()
                + ", Name: " + fields[i].getName()
                + ", Type: " + fields[i].getType().getSimpleName());
            }
        }
    }
    
    private void writeDataToField(Field field, Object model, String value){
        field.setAccessible(true);
        Class type = field.getType();
        if(type.isArray()){
            parseArray(field, value, model);
        }else {
            Object o = parseString(field, value);
            if(o != null){
                try {
                    field.set(model, o);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Creator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                System.err.println("ERROR: could not parse field of type " + type.getSimpleName());
            }
        }
    }
    
    private void parseArray(Field field, String value, Object model){
        int numOfDimensions = getArrayDimensionsFromType(field);
        try {
            Object o = null;
            if(numOfDimensions == 1){
                o = parseGenericArray1d(field, value);      
            }else if(numOfDimensions == 2){
                o = parseGenericArray2d(field, value);
            }
            if(o != null){
                field.set(model, o);
            }else{
                System.err.println("ERROR: can't parse array of type " + field.getType().getSimpleName());
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(Creator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int getArrayDimensionsFromType(Field field){
        int result = 0;
        char[] typeName = field.getType().getSimpleName().toCharArray();
        for(int i = 0; i < typeName.length; ++i){
            if(typeName[i] == '['){
                ++result;
            }
        }
        return result;
    }
    
    private String cropArrayTypeName(Field field){
        char[] typeName = field.getType().getSimpleName().toCharArray();
        int i = 0;
        for(; i < typeName.length; ++i){
            if(typeName[i] == '[') break;
        }
        return new String(typeName, 0, i);
    }
    
    private Object parseGenericArray1d(Field field, String value){
        String[] split = value.split(",");
        int size = split.length;
        Class clazz = getClassFromName(field);
        if(clazz == null) return null;
        Object result = (Object) Array.newInstance(clazz, size);
        for(int i = 0; i < Array.getLength(result); ++i){
            Array.set(result, i, parseString(field, split[i]));
        }
        return result;
    }
    
    private Object parseGenericArray2d(Field field, String value){
        String[] split = value.split(";");
        int size = split.length;
        Class clazz = getClassFromName(field);
        Class arrayClazz = Array.newInstance(clazz, 0).getClass();
        Object result = Array.newInstance(arrayClazz, size);
        for(int i = 0; i < Array.getLength(result); ++i){
            Array.set(result, i, parseGenericArray1d(field, split[i]));
        }
        return result;
    }
    
    
    // not used anymore because of the generic parsing methods
    private float[][] parseFloatArray2d(String value){
        String[] banan = value.split(";");
        int firstDimensionSize = banan.length;
        int secondDimensionSize = banan[0].split(",").length;
        float[][] result = new float[firstDimensionSize][secondDimensionSize];
        for(int a = 0; a < firstDimensionSize; ++a){
            String[] bananasplit = banan[a].split(","); //haha, banan[a].split
            for(int b = 0; b < secondDimensionSize; ++b){
                result[a][b] = new Float(bananasplit[b]);
            }
        }      
        return result;
    }
    
    private Class getClassFromName(Field field){
        String typeName = cropArrayTypeName(field);
        if(typeName.equals("int")) return int.class;
        if(typeName.equals("float")) return float.class;
        if(typeName.equals("double")) return double.class;       
        if(typeName.equals("long")) return long.class;
        if(typeName.equals("byte")) return byte.class;
        if(typeName.equals("short")) return short.class;
        if(typeName.equals("boolean")) return boolean.class;
        if(typeName.equals("char")) return char.class;
        if(typeName.equals("String")) return String.class;
        return null;
    }
    
    private Object parseString(Field field, String value){
        String typeName = cropArrayTypeName(field);
        // Number classes
        if(typeName.equals("int")) return new Integer(value);
        if(typeName.equals("float")) return new Float(value);
        if(typeName.equals("double")) return new Double(value);       
        if(typeName.equals("long")) return new Long(value);
        if(typeName.equals("byte")) return new Byte(value);
        if(typeName.equals("short")) return new Short(value);
        // other basic types
        if(typeName.equals("boolean")) return Boolean.valueOf(value);
        if(typeName.equals("char") && value.length() == 1) return new Character(value.charAt(0));
        if(typeName.equalsIgnoreCase("String")) return value;
        // parsing custom classes
        
        /*
        if(typeName.equals("ImageManager")){
            String path = "images/" + value;
            System.out.println(path);
            File imageFile = new File(path);
            return new ImageManager(imageFile);
        }
        */
        
        // no type found
        return null;
    }
    
    private String[][] parseData(NodeList dataNodes){
        int len = dataNodes.getLength();
        String[][] data = new String[len][3];
        for(int i = 0; i < len; ++i){
            Element dataElement = (Element)dataNodes.item(i);
            data[i][0] = dataElement.getAttribute("name");
            data[i][1] = dataElement.getAttribute("type");
            data[i][2] = dataElement.getTextContent();            
        }
        return data;
    }
}
