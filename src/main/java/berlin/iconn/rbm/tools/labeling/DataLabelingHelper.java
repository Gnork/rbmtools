/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.tools.labeling;

import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.tools.clustering.Cluster;
import berlin.iconn.rbm.tools.clustering.DataSet;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Radek
 */
public class DataLabelingHelper {
    
    public static DataSet[] labelData(float[][] resultData, ImageManager imageManager) {
    
        DataSet[] dataSet = new DataSet[resultData.length];
        
        for(int i = 0; i < resultData.length; i++) {
            dataSet[i] = new DataSet(resultData[i], imageManager.get(i).getCategory());
    	}
    	
    	DataSet[] data = arrayToDataSet(resultData, dataSet);
        
        return data;
    }   
    
    private static DataSet[] arrayToDataSet(float[][] resultData, DataSet[] originalData) {
        //Length of result data must be equal to length of original data, eg. number of pics
        if (resultData.length != originalData.length) {
            return null;
        }
        
        DataSet[] result = new DataSet[resultData.length];
        for (int i = 0; i < resultData.length; ++i) {
            result[i] = new DataSet(resultData[i], originalData[i].getLabel());
        }
        
        return result;
    }

    public static SortedMap<Double, String> getLabelProbabilityMap(DataSet[] dataSet, float[] hiddenWindowData) {
        SortedMap<Double, String> labelsSortedByDistance = new TreeMap<>();
        
        HashMap<Double, String> distanceLabelMap = new HashMap<>();
        
        double maxDistance = 0;
        for(DataSet currentDataSet : dataSet) {
            float[] currentData = currentDataSet.getData();
            double distance = getDistance(hiddenWindowData, currentData);
            maxDistance = (distance > maxDistance) ? distance : maxDistance;
            labelsSortedByDistance.put(distance, currentDataSet.getLabel());
        }
        
        for(Double distance : distanceLabelMap.keySet()) {
            String label = distanceLabelMap.get(distance);
            Double probability = maxDistance / distance;
            labelsSortedByDistance.put(probability, label);
        }
        
        return labelsSortedByDistance;
    }
    
    public static double getDistance(float[] data1, float[] data2){
        double sum = 0f;
        for(int i = 0; i < data1.length; ++i){
            sum += (data1[i] - data2[i]) * (data1[i] - data2[i]);
        }
        return Math.sqrt(sum);
    }
    
}
