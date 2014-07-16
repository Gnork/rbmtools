package berlin.iconn.rbm.views;

import berlin.iconn.rbm.image.DataConverter;
import berlin.iconn.rbm.image.ImageHelper;
import berlin.iconn.rbm.image.ImageScaler;
import berlin.iconn.rbm.main.BenchmarkModel;
import berlin.iconn.rbm.rbm.RBMTrainer;
import berlin.iconn.rbm.tools.clustering.LabeledData;
import berlin.iconn.rbm.tools.labeling.DataLabelingHelper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class InImageDetectorModel {

    private final InImageDetectorController controller;
    
    float[] imageData;
    int width;
    int height;
    
    LabeledData[] labeledData;    
    String[] codeImage;

    private BenchmarkModel benchmarkModel;
    
    public InImageDetectorModel(InImageDetectorController controller) {
        this.controller = controller;
    }


    
    public BenchmarkModel getBenchmarkModel() {
		return benchmarkModel;
	}



	public Image loadImage(int visWidth, int visHeight) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("images"));
        Stage fileChooserStage = new Stage();

        File file = fileChooser.showOpenDialog(fileChooserStage);
        if (file != null) {
            BufferedImage sourceImage = ImageHelper.loadImage(file);
        	
            this.width = sourceImage.getWidth();
            this.height = sourceImage.getHeight();
        	
            this.imageData = DataConverter.processPixelData(sourceImage, this.width, this.height, this.benchmarkModel.isBinarizeImages(), this.benchmarkModel.isInvertImages(), this.benchmarkModel.getMinData(), this.benchmarkModel.getMaxData(), this.benchmarkModel.isRgb());
            BufferedImage processedImage = DataConverter.pixelDataToImage(imageData, this.benchmarkModel.getMinData(), this.benchmarkModel.isRgb(), this.width, this.height);
            
            WritableImage image = SwingFXUtils.toFXImage(processedImage, null);

            return image;
        } else {
            return null;
        }
    }

	public void setBenchmarkModel(BenchmarkModel benchmarkModel) {
		this.benchmarkModel = benchmarkModel;
	}

	public void detection() {
            RBMTrainer trainer = new  RBMTrainer();
            float[][] hiddenData = trainer.getHiddenAllRBMs(this.benchmarkModel, this.benchmarkModel.getImageManager().getImageData(), false);
    	
            this.labeledData = DataLabelingHelper.getLabeledDataUnique(hiddenData, this.benchmarkModel.getImageManager());
            
            
            /*
            codeImage = new String[imageData.length];
		
            int windowEdgeLength = this.benchmarkModel.getImageEdgeSize();
		
            
            for(int y = 0, pos = 0; y < this.height - windowEdgeLength; y++) {
            	for(int x = 0; x < this.width - windowEdgeLength; x++, pos++) {
            		codeImage[pos] = getLabel(x, y);
            	}
            }
            */
		
	}
	
	public HashMap<String, Double> getProbabilityMap(double xPos, double yPos) {
            
            int windowEdgeLength = this.benchmarkModel.getImageEdgeSize();
            
            if(xPos > this.width - windowEdgeLength || yPos > this.height - windowEdgeLength) {
                return null;
            }
            
            TreeMap<Double, String> probabilityMap = new TreeMap<>();	
            
            RBMTrainer trainer = new RBMTrainer();
		
            int x = (int)xPos;
            int y = (int)yPos;
		
            float[] window = new float[windowEdgeLength * windowEdgeLength];
            for(int yW = 0, iW = 0; yW < windowEdgeLength; yW++) {
            	for(int xW = 0; xW < windowEdgeLength; xW++, iW++) {
            		int pos = (y + yW) * this.width + x + xW;
            		window[iW] = imageData[pos];
            	}
            }
		
            float[] hiddenWindowData = trainer.getHiddenAllRBMs1D(this.benchmarkModel, window, false);
		
            SortedMap<Double, String> distanceMap = DataLabelingHelper.getDistanceMap(this.labeledData, hiddenWindowData);
                
            double maxDistance = 0;
            for(double distance : distanceMap.keySet()) {
                if(distance > maxDistance) maxDistance = distance;
            }
            
            Iterator<Double> distanceIterator = distanceMap.keySet().iterator();
            HashSet<String> labelsInside = new HashSet<>();
            while((probabilityMap.size() < this.benchmarkModel.getImageManager().getGroupNames().size()) && distanceIterator.hasNext()) {
                Double distance = distanceIterator.next();
                //probabilityMap.put((maxDistance - distance) / maxDistance, distanceMap.get(distance));
                String label = distanceMap.get(distance);
                if(!labelsInside.contains(label)) {
                    labelsInside.add(label);
                    probabilityMap.put(1 - distance / maxDistance, label);
                }
            }
		
		/*
		if(distance< 3.0f) {
			return "posX: " + xPos + " posX: " + yPos + " label: " + cluster.getLabel() + " distance: " + distance;
		} else return "";
		*/
            
        HashMap<String, Double> toReturn = new HashMap<String, Double>();
        
        for(String name : this.benchmarkModel.getImageManager().getGroupNames()) {
        	
        	Double distance = 0.0;
        	for(Double key : probabilityMap.keySet()) {
        		if(probabilityMap.get(key).equals(name)) {
        			distance = key;
        		}
        	}
        	
        	toReturn.put(name, distance);
        }
		
        return toReturn;
	}
	
	public float[] getImageData() {
            return imageData;
        }
        
        public float getWidth() {
            return this.width;
        }
        
        public float getHeight() {
            return this.height;
        }
	
}
