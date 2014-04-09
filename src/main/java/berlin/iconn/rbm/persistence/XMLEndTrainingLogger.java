

package berlin.iconn.rbm.persistence;

import berlin.iconn.rbm.enhancement.IRBMEndTrainingEnhancement;
import berlin.iconn.rbm.enhancement.RBMInfoPackage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 *
 * @author moritz
 */
public class XMLEndTrainingLogger implements IRBMEndTrainingEnhancement  {
    
    private final XMLWeightsSaver logger;


    public XMLEndTrainingLogger() {
        this.logger = new XMLWeightsSaver();
    }


    @Override
    public void action(RBMInfoPackage info) {
        try {
            logger.singleWeights(info);
        } catch (IOException | ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(XMLEndTrainingLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
