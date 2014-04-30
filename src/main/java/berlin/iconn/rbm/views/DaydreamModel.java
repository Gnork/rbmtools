package berlin.iconn.rbm.views;

import berlin.iconn.rbm.main.BenchmarkModel;
import berlin.iconn.rbm.rbm.RBMTrainer;
import berlin.iconn.rbm.image.DataConverter;
import berlin.iconn.rbm.image.ImageHelper;
import berlin.iconn.rbm.image.ImageScaler;
import berlin.iconn.rbm.tools.Chooser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DaydreamModel {

    private final DaydreamController controller;

    Random random = new Random();
	
    float[] calcImageData;
    BufferedImage visibleImage;
    BufferedImage hiddenImage;

    private boolean useHiddenStates;
	private boolean useVisibleStates;

	private BenchmarkModel benchmarkModel;
    
	public DaydreamModel(DaydreamController controller) {
        this.useHiddenStates = false;
        this.useVisibleStates = false;
        this.controller = controller;
	}
    
    public Image loadImage(int visWidth, int visHeight) {

        File file = Chooser.openFileChooser("images");
        if(file == null){
            return null;
        }
        this.calcImageData = DataConverter.processPixelData(ImageHelper.loadImage(file), this.benchmarkModel.getImageEdgeSize(), this.benchmarkModel.isBinarizeImages(), this.benchmarkModel.isInvertImages(), this.benchmarkModel.getMinData(), this.benchmarkModel.getMaxData(), this.benchmarkModel.isRgb());

        ImageScaler imageScaler = new ImageScaler();

        WritableImage image = SwingFXUtils.toFXImage(imageScaler.getScaledImageNeirestNeighbour(DataConverter.pixelDataToImage(this.calcImageData, this.benchmarkModel.getMinData(), this.benchmarkModel.isRgb()), visWidth, visHeight), null);

        return image;
    }

    public Image generateImage(int visWidth, int visHeight) {
        int width = this.benchmarkModel.getImageEdgeSize();
        int height = this.benchmarkModel.getImageEdgeSize();
        int scaling = (this.benchmarkModel.isRgb()) ? 3 : 1;
        
        float[] imageData = new float[width * height * scaling];
        for (int i = 0; i < imageData.length; i++) {
        	imageData[i] = random.nextFloat();
        }
        
        this.calcImageData = DataConverter.processPixelData(imageData, this.benchmarkModel.getImageEdgeSize(), this.benchmarkModel.isBinarizeImages(), this.benchmarkModel.isInvertImages(), this.benchmarkModel.getMinData(), this.benchmarkModel.getMaxData(), this.benchmarkModel.isRgb());
		
		ImageScaler imageScaler = new ImageScaler();
        WritableImage image = SwingFXUtils.toFXImage(imageScaler.getScaledImageNeirestNeighbour(DataConverter.pixelDataToImage(this.calcImageData, this.benchmarkModel.getMinData(), this.benchmarkModel.isRgb()), visWidth, visHeight), null);

        return image;
    }

    public void daydream() {
    	RBMTrainer trainer = new  RBMTrainer();
    	
    	// Create visible daydream data, which is used for the next calculation step 
        float[] visibleDataForCalc = trainer.daydreamAllRBMs(this.benchmarkModel, this.calcImageData, this.useHiddenStates, this.useVisibleStates);
        
        // Create hidden and visible daydream data, which is used for visualization
        float[] hiddenData = trainer.getHiddenAllRBMs1D(this.benchmarkModel, this.calcImageData, this.useHiddenStates);
        float[] visibleDataForVis = trainer.getVisibleAllRBMs1D(this.benchmarkModel, hiddenData, this.useVisibleStates);
        
        // Convert hiddenData to pixels
        int hiddenImageEdgeLength = (int)Math.sqrt(hiddenData.length);
        int[] hiddenImagePixels = new int[hiddenImageEdgeLength * (hiddenImageEdgeLength + 1)];
        
        int counter = 0;
        for(int y = 0; y < hiddenImageEdgeLength + 1; y++) {
        	for(int x = 0; x < hiddenImageEdgeLength; x++) {
        		int pos = y*hiddenImageEdgeLength+x;
        		if(counter < hiddenData.length) {
            		int hiddenValue = (int) Math.round(hiddenData[pos] * 255);
            		hiddenImagePixels[pos] = (0xFF << 24) | (hiddenValue << 16) | (hiddenValue << 8) | hiddenValue;
        		} else {
            		hiddenImagePixels[pos] = (0xFF << 24) | (255 << 16) | (0 << 8) | 0;        			
        		}
        		counter++;
        	}
        }
        
        this.calcImageData = visibleDataForCalc;
        this.visibleImage = DataConverter.pixelDataToImage(visibleDataForVis, 0.0f, this.benchmarkModel.isRgb());
        BufferedImage hiddenImage = new BufferedImage(hiddenImageEdgeLength, hiddenImageEdgeLength + 1, BufferedImage.TYPE_INT_RGB);
        hiddenImage.setRGB(0, 0, hiddenImageEdgeLength, hiddenImageEdgeLength + 1, hiddenImagePixels, 0, hiddenImageEdgeLength);
        this.hiddenImage = hiddenImage;
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

    public void setUseHiddenStates(boolean useHiddenStates) {
        this.useHiddenStates = useHiddenStates;
    }

    public void setUseVisibleStates(boolean useVisibleStates) {
        this.useVisibleStates = useVisibleStates;
    }

	public void setBenchmarkModel(BenchmarkModel benchmarkModel) {
		this.benchmarkModel = benchmarkModel;
	}
        
    public BenchmarkModel getBenchmarkModel(){
        return benchmarkModel;
    }

}
