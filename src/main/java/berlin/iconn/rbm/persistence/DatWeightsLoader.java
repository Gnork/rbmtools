/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.persistence;

import java.io.File;
import java.io.ObjectInputStream;
import java.nio.file.Files;

/**
 *
 * @author christoph
 */
public class DatWeightsLoader {

    public static float[][] loadWeights(File file) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
            return (float[][]) ois.readObject();
        }
    }
    
    public static float[][] changeRGBcoding(float[][] weights){
        float[][] changedWeights = new float[weights.length][];
        
        int dimensionSize = (weights.length - 1) / 3;
        
        changedWeights[0] = weights[0];
        
        for(int i = 0; i < dimensionSize; ++i){
            float[] r = weights[1 + i*3];
            float[] g = weights[2 + i*3];
            float[] b = weights[3 + i*3];
            changedWeights[1 + i] = r;
            changedWeights[1 + dimensionSize + i] = g;
            changedWeights[1 + dimensionSize * 2 + i] = b;
        }
        
        return changedWeights;
    }
}
