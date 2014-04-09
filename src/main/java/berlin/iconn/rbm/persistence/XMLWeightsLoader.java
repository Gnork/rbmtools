/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.persistence;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author christoph
 */
public class XMLWeightsLoader {
    
    public float[][] loadWeightsFromXML(File file) throws ParserConfigurationException, SAXException, IOException{
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(file);
        Element rootElement = doc.getDocumentElement();
        
        NodeList rows = rootElement.getElementsByTagName("row");
        int inputSize = rows.getLength();
        int outputSize = rows.item(0).getTextContent().split(",").length;

        float[][] weights = new float[inputSize][outputSize];
        
        for(int i = 0; i < weights.length; ++i){
            String[] weightRow = rows.item(i).getTextContent().split(",");
            for(int j = 0; j < weights[0].length; ++j){
                weights[i][j] = Float.parseFloat(weightRow[j]);
            }
        }
        
        return weights;
    }
}
