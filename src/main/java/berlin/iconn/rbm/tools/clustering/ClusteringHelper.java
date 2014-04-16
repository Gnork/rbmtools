package berlin.iconn.rbm.tools.clustering;

import java.util.LinkedList;
import java.util.List;

import berlin.iconn.rbm.image.ImageManager;

public class ClusteringHelper {
	
    /**
     * Generates clusters from the hidden layer result of the last rbm
     * It uses training data labels to put all data from one category into one cluster
     * The cluster center is the mean value of the data in the cluster
     * @param data
     * @return
     */
    public static List<Cluster> generateClusters(float[][] resultData, ImageManager imageManager) {
    	
    	LabeledData[] dataSet = new LabeledData[resultData.length];
    	
    	for(int i = 0; i < resultData.length; i++) {
    		dataSet[i] = new LabeledData(resultData[i], imageManager.get(i).getCategory());
    	}
    	
    	LabeledData[] data = arrayToDataSet(resultData, dataSet);
    	
        List<Cluster> clusters = new LinkedList<Cluster>();

        for (LabeledData ds : data) {
            boolean found = false;
            String label = ds.getLabel();
            for (Cluster c : clusters) {
                if (c.getLabel().equals(label)) {
                    c.addVector(ds.getData());
                    found = true;
                    break;
                }
            }
            if (!found) {
                Cluster c = new Cluster(label);
                c.addVector(ds.getData());
                clusters.add(c);
            }
        }
        for (Cluster c : clusters) {
            c.init();
        }

        return clusters;
    }
    
    public static Cluster getCluster(List<Cluster> clusters, float[] resultData) {
    	
    	float minDistance = Float.MAX_VALUE;
    	Cluster result = null;
    	
    	for(Cluster cluster : clusters) {
    		float distance = cluster.getDistanceToCenter(resultData);
    		if(distance < minDistance) {
    			minDistance = distance;
    			result = cluster;
    		}
    	}
    	
    	return result;
    	
    }
	
    private static LabeledData[] arrayToDataSet(float[][] resultData, LabeledData[] originalData) {
        //Length of result data must be equal to length of original data, eg. number of pics
        if (resultData.length != originalData.length) {
            return null;
        }
        
        LabeledData[] result = new LabeledData[resultData.length];
        for (int i = 0; i < resultData.length; ++i) {
            result[i] = new LabeledData(resultData[i], originalData[i].getLabel());
        }
        
        return result;
    }
    
}
