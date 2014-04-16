/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.tools.labeling;

import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.tools.clustering.LabeledData;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Radek
 */
public class DataLabelingHelper {
    
    public static LabeledData[] getLabeledData(float[][] resultData, ImageManager imageManager) {
    
        LabeledData[] labeledData = new LabeledData[resultData.length];
        
        for(int i = 0; i < resultData.length; i++) {
            labeledData[i] = new LabeledData(resultData[i], imageManager.get(i).getCategory());
    	}
        
        return labeledData;
    }   
    
    public static LabeledData[] getLabeledDataUnique(float[][] resultData, ImageManager imageManager) {
        HashSet<LabeledData> labeledDataUniqeSet = new HashSet<>();
        
        for(int i = 0; i < resultData.length; i++) {
            LabeledData labeledData = new LabeledData(resultData[i], imageManager.get(i).getCategory());
            labeledDataUniqeSet.add(labeledData);
    	}
        
        LabeledData[] labeledDataArray = new LabeledData[labeledDataUniqeSet.size()];
        
        int i = 0;
        for(LabeledData labeledData : labeledDataUniqeSet) {
            labeledDataArray[i] = labeledData;
            i++;
        }
        
        return labeledDataArray;
    }

    public static SortedMap<Double, String> getDistanceMap(LabeledData[] labeledDataSet, float[] hiddenWindowData) {
        SortedMap<Double, String> distanceMap = new TreeMap<>();
        
        double maxDistance = 0;
        for(LabeledData labeledData : labeledDataSet) {
            
            String label = labeledData.getLabel();
            float[] data = labeledData.getData();
            
            double distance = getL1Distance(hiddenWindowData, data);
            
            if(distance > maxDistance) {
                maxDistance = distance;
            }
            
            distanceMap.put(distance, label);   
        }
        
        return distanceMap;
    }
    
    public static double getL2Distance(float[] data1, float[] data2){
        double sum = 0f;
        for(int i = 0; i < data1.length; ++i){
            sum += (data1[i] - data2[i]) * (data1[i] - data2[i]);
        }
        return Math.sqrt(sum);
    }
    
    public static double getL1Distance(float[] data1, float[] data2){
        double sum = 0f;
        for(int i = 0; i < data1.length; ++i){
            sum += Math.abs(data1[i] - data2[i]);
        }
        return sum / data1.length;
    }
    
}
