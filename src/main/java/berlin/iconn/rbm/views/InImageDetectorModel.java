package berlin.iconn.rbm.views;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import berlin.iconn.rbm.image.DataConverter;
import berlin.iconn.rbm.image.ImageHelper;
import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.main.BenchmarkModel;
import berlin.iconn.rbm.rbm.RBMTrainer;
import berlin.iconn.rbm.tools.clustering.Cluster;
import berlin.iconn.rbm.tools.clustering.ClusteringHelper;

public class InImageDetectorModel {

    private final InImageDetectorController controller;
    
    float[] imageData;
    int width;
    int height;
    
    List<Cluster> clusters;
    String[] codeImage;

	private BenchmarkModel benchmarkModel;
    
    public InImageDetectorModel(InImageDetectorController controller) {
        this.controller = controller;
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
    	
		clusters = ClusteringHelper.generateClusters(hiddenData, this.benchmarkModel.getImageManager());
		
		codeImage = new String[imageData.length];
		
		int windowEdgeLength = this.benchmarkModel.getImageEdgeSize();
		
		/*
		for(int y = 0, pos = 0; y < this.height - windowEdgeLength; y++) {
			for(int x = 0; x < this.width - windowEdgeLength; x++, pos++) {
				codeImage[pos] = getLabel(x, y);
			}
		}
		*/
		
	}
	
	public String getLabel(double xPos, double yPos) {
		
    	RBMTrainer trainer = new RBMTrainer();
		
		int x = (int)xPos;
		int y = (int)yPos;
		
		int windowEdgeLength = this.benchmarkModel.getImageEdgeSize();
		
		float[] window = new float[windowEdgeLength * windowEdgeLength];
		for(int yW = 0, iW = 0; yW < windowEdgeLength; yW++) {
			for(int xW = 0; xW < windowEdgeLength; xW++, iW++) {
				int pos = (y + yW) * this.width + x + xW;
				window[iW] = imageData[pos];
			}
		}
		
		float[] hiddenWindowData = trainer.getHiddenAllRBMs1D(this.benchmarkModel, window, false);
		
		Cluster cluster = ClusteringHelper.getCluster(clusters, hiddenWindowData);
		float distance = cluster.getDistanceToCenter(hiddenWindowData);
		
		/*
		if(distance< 3.0f) {
			return "posX: " + xPos + " posX: " + yPos + " label: " + cluster.getLabel() + " distance: " + distance;
		} else return "";
		*/
		
		return (true) ? cluster.getLabel() : "";
		
	}
	
	
	
}
