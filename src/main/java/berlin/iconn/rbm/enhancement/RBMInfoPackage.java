/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.enhancement;

import java.util.LinkedList;

/**
 *
 * @author moritz
 */
public class RBMInfoPackage {
    
    private float error;
    private float[][] weights;
    private int epochs;
    private final LinkedList<float[][]> collectedWeights;

    public RBMInfoPackage(float error, float[][] weights, int epochs) {
        this.error = error;
        this.weights = weights;
        this.epochs = epochs;
        this.collectedWeights = new LinkedList<>();
    }

    public float getError() {
        return error;
    }

    public float[][] getWeights() {
        return weights;
    }

    public int getEpochs() {
        return epochs;
    }

    public LinkedList<float[][]> getCollectedWeights() {
        return collectedWeights;
    }

    /**
     * @param error the error to set
     */
    public void setError(float error) {
        this.error = error;
    }

    /**
     * @param weights the weights to set
     */
    public void setWeights(float[][] weights) {
        this.collectedWeights.add(weights);
        this.weights = weights;
    }

    /**
     * @param epochs the epochs to set
     */
    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }
    
    
    
}
