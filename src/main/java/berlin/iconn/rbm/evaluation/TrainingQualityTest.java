package berlin.iconn.rbm.evaluation;

import java.io.File;

import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.main.BenchmarkModel;
import berlin.iconn.rbm.rbm.RBMTrainer;
import berlin.iconn.rbm.tools.Chooser;

public class TrainingQualityTest {
	
	public static float getMSE(BenchmarkModel benchmarkModel) {
		File folder = Chooser.openDirectoryChooser("images");
                if(folder == null){
                    return 0.0f;
                }
		ImageManager imageManager = new ImageManager(folder, benchmarkModel);
		
		float[][] originalData = imageManager.getImageData();
		float[][] synthesizedData;
		
		RBMTrainer trainer = new RBMTrainer();
		
        float[][] hiddenDataForVis = trainer.getHiddenAllRBMs(benchmarkModel, originalData, false);
        synthesizedData = trainer.getVisibleAllRBMs(benchmarkModel, hiddenDataForVis, false);
        
        float error = 0.0f;
        for(int i = 0; i < originalData.length; i++) {
        	error += calcMSE(originalData[i], synthesizedData[i]);
        }
        
        float norm = benchmarkModel.getImageEdgeSize() * benchmarkModel.getImageEdgeSize() * originalData.length;
        error = (float) (255.0f * Math.sqrt( (1.0f / norm)  * error));
        
        return error;
	}
	
    private static float calcMSE(float[] data1, float[] data2) {
    	float mse = 0;
    	int n = data1.length;
    	for(int i = 0; i < n; i++) {
    		float error = data1[i] - data2[i];
    		mse += error * error;
    	}

    	return mse;
    }
	
}
