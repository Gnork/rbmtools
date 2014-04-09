/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.settings;

import berlin.iconn.rbm.persistence.Conserve;
import berlin.iconn.rbm.persistence.XMLWeightsLoader;
import berlin.iconn.rbm.persistence.XMLWeightsSaver;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 *
 * @author Moritz
 */
public class RBMSettingsWeightsModel{
    private final RBMSettingsWeightsController controller;
    private final XMLWeightsLoader loader;
    private final XMLWeightsSaver saver;
    
    @Conserve
    private boolean binarizeHidden = false;
    @Conserve
    private boolean binarizeVisible = false;
    @Conserve
    private boolean useSeed = false;
    @Conserve
    private int seed = 0;
    @Conserve
    private float[][] weights; 

    public RBMSettingsWeightsModel(RBMSettingsWeightsController controller) {
        this.loader = new XMLWeightsLoader();
        this.saver = new XMLWeightsSaver();
        this.controller = controller;
    }

    public boolean isUseSeed() {
        return useSeed;
    }
    
    public boolean isBinarizeHidden() {
		return binarizeHidden;
	}

	public void setBinarizeHidden(boolean binarizeHidden) {
		this.binarizeHidden = binarizeHidden;
	}

	public boolean isBinarizeVisible() {
		return binarizeVisible;
	}

	public void setBinarizeVisible(boolean binarizeVisible) {
		this.binarizeVisible = binarizeVisible;
	}

    public void setUseSeed(boolean useSeed) {
        this.useSeed = useSeed;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public float[][] getWeights() {
        return this.weights;
    }

    public void setWeights(float[][] weights) {
        this.weights = weights;
    }
    
    public void loadWeights(File file) {
        try {
            weights = this.loader.loadWeightsFromXML(file);
            System.out.println("Load Weights");
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(RBMSettingsWeightsModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveWeights() {
        try {
            this.saver.singleWeights(this.weights);
            System.out.println("Save Weights");
        } catch (IOException | ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(RBMSettingsWeightsModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
