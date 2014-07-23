package berlin.iconn.rbm.persistence;

import berlin.iconn.rbm.enhancement.RBMInfoPackage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * can save RBM weights to hard drive in XML format
 */

public final class XMLWeightsSaver {

    private final String baseFolder;
    
    public XMLWeightsSaver() {
        this.baseFolder = "RBMLogs";
    }
    
    public void singleWeights(RBMInfoPackage info) throws IOException, ParserConfigurationException, TransformerException{
        this.singleWeights(info.getWeights());
    }

    public void singleWeights(float[][] weights) throws IOException, ParserConfigurationException, TransformerException {
        String dateString = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        String xmlFolder = this.baseFolder + "/SingleWeights";
        String xmlLocation = dateString + ".xml";

        Path xmlFolderPath = FileSystems.getDefault().getPath(xmlFolder);
        Path baseFolderPath = FileSystems.getDefault().getPath(baseFolder);
        
        if (Files.notExists(baseFolderPath, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectory(baseFolderPath);
        }
        if (Files.notExists(xmlFolderPath, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectory(xmlFolderPath);
        }
        
        StringBuffer rowSB;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc;
        Element rootElement;

        doc = docBuilder.newDocument();
        rootElement = doc.createElement("weights");
        doc.appendChild(rootElement);

        for (int i = 0; i < weights.length; ++i) {
            rowSB = new StringBuffer();
            for (int j = 0; j < weights[i].length; ++j) {
                rowSB.append(weights[i][j]);
                if (j < weights[i].length - 1) {
                    rowSB.append(",");
                }
            }
            Element row = doc.createElement("row");
            rootElement.appendChild(row);

            Attr num = doc.createAttribute("num");
            num.setValue(new Integer(i).toString());
            row.setAttributeNode(num);

            row.appendChild(doc.createTextNode(rowSB.toString()));
        }
   
        File xmlFile = new File(xmlFolder + "/" + xmlLocation);
        StreamResult result = new StreamResult(xmlFile);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
    }
}
