/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.views;

import berlin.iconn.rbm.rbm.RBMTrainer;
import berlin.iconn.rbm.settings.RBMSettingsMainController;
import berlin.iconn.rbm.settings.RBMSettingsWeightsController;
import berlin.iconn.rbm.image.DataConverter;
import berlin.iconn.rbm.image.ImageHelper;
import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.image.ImageScaler;
import berlin.iconn.rbm.image.Pic;
import berlin.iconn.rbm.main.BenchmarkModel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

/**
 *
 * @author Radek
 */
public class RunHiddenModel {
    
    private final RunHiddenController controller;
    
    float[] calcImageData;
    BufferedImage visibleImage;
    BufferedImage hiddenImage;
	private float mse = 0;
    
    private boolean useHiddenStates;
	private boolean useVisibleStates;

	private BenchmarkModel benchmarkModel;
    
    public RunHiddenModel(RunHiddenController controller) {
    	useHiddenStates = false;
    	useVisibleStates = false;
        this.controller = controller;
    }
    
    public Image loadImage(int visWidth, int visHeight) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("CBIR_Project/images"));
        Stage fileChooserStage = new Stage();

        File file = fileChooser.showOpenDialog(fileChooserStage);
        if (file != null) {
            this.calcImageData = DataConverter.processPixelData(ImageHelper.loadImage(file), this.benchmarkModel.getImageEdgeSize(), this.benchmarkModel.isBinarizeImages(), this.benchmarkModel.isInvertImages(), this.benchmarkModel.getMinData(), this.benchmarkModel.getMaxData(), this.benchmarkModel.isRgb());

            ImageScaler imageScaler = new ImageScaler();
            WritableImage image = SwingFXUtils.toFXImage(imageScaler.getScaledImageNeirestNeighbour(DataConverter.pixelDataToImage(this.calcImageData, this.benchmarkModel.getMinData(), this.benchmarkModel.isRgb()), visWidth, visHeight), null);

            return image;
        } else {
            return null;
        }
    }
    
    public void runHidden() {
    	RBMTrainer trainer = new  RBMTrainer();
    	
    	int width = this.benchmarkModel.getImageEdgeSize();
    	int height = this.benchmarkModel.getImageEdgeSize();
        
        // Create hidden and visible daydream data, which is used for visualization
        float[] hiddenDataForVis = trainer.getHiddenAllRBMs1D(this.benchmarkModel, this.calcImageData, false);
        float[] visibleDataForVis = trainer.getVisibleAllRBMs1D(this.benchmarkModel, hiddenDataForVis, false);
        
        // Convert hiddenData to pixels
        int hiddenImageEdgeLength = (int)Math.sqrt(hiddenDataForVis.length);
        int[] hiddenImagePixels = new int[hiddenImageEdgeLength * (hiddenImageEdgeLength + 1)];
        
        int counter = 0;
        for(int y = 0; y < hiddenImageEdgeLength + 1; y++) {
        	for(int x = 0; x < hiddenImageEdgeLength; x++) {
        		int pos = y*hiddenImageEdgeLength+x;
        		if(counter < hiddenDataForVis.length) {
            		int hiddenValue = (int) Math.round(hiddenDataForVis[pos] * 255);
            		hiddenImagePixels[pos] = (0xFF << 24) | (hiddenValue << 16) | (hiddenValue << 8) | hiddenValue;
        		} else {
            		hiddenImagePixels[pos] = (0xFF << 24) | (255 << 16) | (0 << 8) | 0;        			
        		}
        		counter++;
        	}
        }
        
        this.mse = calcMSE(this.calcImageData, visibleDataForVis);
        this.visibleImage = DataConverter.pixelDataToImage(visibleDataForVis, 0.0f, this.benchmarkModel.isRgb());
        BufferedImage hiddenImage = new BufferedImage(hiddenImageEdgeLength, hiddenImageEdgeLength + 1, BufferedImage.TYPE_INT_RGB);
        hiddenImage.setRGB(0, 0, hiddenImageEdgeLength, hiddenImageEdgeLength + 1, hiddenImagePixels, 0, hiddenImageEdgeLength);
        this.hiddenImage = hiddenImage;
    }
    
    private float calcMSE(float[] data1, float[] data2) {
    	float mse = 0;
    	int n = this.calcImageData.length;
    	for(int i = 0; i < n; i++) {
    		float error = data1[i] - data2[i];
    		mse += error * error;
    	}
    	mse /= (float)n;
    	return mse;
    }
    
    public Image getVisibleImage(int visWidth, int visHeight) {
		ImageScaler imageScaler = new ImageScaler();

        WritableImage visibleImage = new WritableImage(visWidth, visHeight);
        SwingFXUtils.toFXImage(imageScaler.getScaledImageNeirestNeighbour(this.visibleImage, visWidth, visHeight), visibleImage);
    	
    	return visibleImage;
    }
    
    public Image getHiddenImage(int scalingFactor) {
		ImageScaler imageScaler = new ImageScaler();
		
		int width = this.hiddenImage.getWidth();
		int height = this.hiddenImage.getHeight();
		
		int visWidth = this.hiddenImage.getWidth() * scalingFactor;
		int visHeight = this.hiddenImage.getHeight() * scalingFactor;

        WritableImage hiddenImage = new WritableImage(visWidth, visHeight);
        SwingFXUtils.toFXImage(imageScaler.getScaledImageNeirestNeighbour(this.hiddenImage, visWidth, visHeight), hiddenImage);
    	
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(hiddenImage, null);
        		
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.BLACK);
        BasicStroke bs = new BasicStroke(1);
        g2d.setStroke(bs);
        
        for(int y = 0; y < height; y++) {
        	if(y != 0) {
        		g2d.drawLine(0, (y) * scalingFactor, visWidth, (y) * scalingFactor);
        	}
        }
        
    	for(int x = 0; x < width; x++) {
    		if(x != 0) {
    			g2d.drawLine((x) * scalingFactor, 0, (x) * scalingFactor, visHeight);
    		}
    	}
        
    	return SwingFXUtils.toFXImage(bufferedImage, null);
    }
    
    public float getMSE() {
		return mse;
	}

	public void setUseHiddenStates(boolean useHiddenStates) {
		this.useHiddenStates = useHiddenStates;
	}

	public void setUseVisibleStates(boolean useVisibleStates) {
		this.useVisibleStates = useVisibleStates;
	}

	public void setBenchmarkModel(BenchmarkModel benchmarkModel) {
		this.benchmarkModel = benchmarkModel;
	}

	public Image getStateImage(int index, int visWidth, int visHeight) {
		float[] hiddenData = new float[this.benchmarkModel.getRbmSettingsList().getLast().getModel().getController(RBMSettingsMainController.class).getModel().getOutputSize()];  
		hiddenData[index] = 1.0f;
		
		RBMTrainer trainer = new  RBMTrainer();
		float[] visibleData = trainer.getVisibleAllRBMs1D(this.benchmarkModel, hiddenData, false);

		BufferedImage image = DataConverter.pixelDataToImage(visibleData, 0, this.benchmarkModel.isRgb());
		
		ImageScaler imageScaler = new ImageScaler();

        WritableImage visibleImage = new WritableImage(visWidth, visHeight);
        SwingFXUtils.toFXImage(imageScaler.getScaledImageNeirestNeighbour(image, visWidth, visHeight), visibleImage);
	
        return visibleImage;
	}
}
