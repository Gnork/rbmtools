package berlin.iconn.rbm.rbm;

import berlin.iconn.rbm.logistic.ILogistic;
import java.util.Random;

/**
 * 
 * RBM implementation in Java
 * multithreaded and optimized for fast calculations
 */

public class RBMOpti implements IRBM {

    private final float learnRate;
    private final ILogistic logisticFunction;

    private float error;

    private FloatMatrix weights;

    public RBMOpti(int inputSize, int outputSize, float learningRate, ILogistic logisticFunction, boolean useSeed, int seed, float[][] weights) {
        this.learnRate = learningRate;
        this.logisticFunction = logisticFunction;

        if (weights == null) {
            this.weights = new FloatMatrix(inputSize, outputSize, useSeed, seed);
            this.weights = new FloatMatrix(this.weights.getData(), FloatMatrix.Bias.BOTH);
        } else {
            this.weights = new FloatMatrix(weights);
        }        
    }  
    
    @Override
    public float error(float[][] trainingData, boolean binarizeHidden, boolean binarizeVisible) {
        return 0;
    }

    @Override
    public void train(float[][] trainingData, StoppingCondition stop, boolean binarizeHidden, boolean binarizeVisible) {

        final FloatMatrix dataWithBias = new FloatMatrix(trainingData, FloatMatrix.Bias.COLUMN_ONLY);
        final FloatMatrix dataWithBiasTrans = new FloatMatrix(dataWithBias.getColumns(), dataWithBias.getRows());
        dataWithBias.transposei(dataWithBiasTrans);
        final FloatMatrix localWeights = this.weights;
        final FloatMatrix localWeightsTrans = new FloatMatrix(localWeights.getColumns(), localWeights.getRows());
        localWeights.transposei(localWeightsTrans);
        final FloatMatrix hidden = new FloatMatrix(dataWithBias.getRows(), localWeights.getColumns());
        final FloatMatrix visible = new FloatMatrix(hidden.getRows(), localWeights.getRows());
        final FloatMatrix visibleTrans = new FloatMatrix(visible.getColumns(), visible.getRows());
        final FloatMatrix posAssociations = new FloatMatrix(dataWithBiasTrans.getRows(), hidden.getColumns());
        final FloatMatrix negAssociations = new FloatMatrix(dataWithBiasTrans.getRows(), hidden.getColumns());

        while(stop.isNotDone()) {

            //final FloatMatrix posHiddenActivations = dataWithBias.mmul(this.weights);
            dataWithBias.mmuli(localWeights, hidden);

            //FloatMatrix posHiddenProbs = logisticFunction.function(posHiddenActivations);
            logistic(hidden);

            if (binarizeHidden) {            
                hidden.gti(new FloatMatrix(hidden.getRows(), hidden.getColumns(), false, 0));
            }

            //posHiddenNodes.putColumn(0, FloatMatrix.ones(posHiddenNodes.getRows(), 1));
            hidden.setFirstColumnOne();

            //final FloatMatrix posAssociations = dataWithBias.transpose().mmul(posHiddenProbs);
            dataWithBiasTrans.mmuli(hidden, posAssociations);
           
            //final FloatMatrix negVisibleActivations = posHiddenStates.mmul(this.weights.transpose());
            localWeights.transposei(localWeightsTrans);
            hidden.mmuli(localWeightsTrans, visible);

            //final FloatMatrix negVisibleProbs = logisticFunction.function(negVisibleActivations);
            logistic(visible);

            //negVisibleProbs.putColumn(0, FloatMatrix.ones(negVisibleProbs.getRows(), 1));
            visible.setFirstColumnOne();

            //final FloatMatrix negHiddenActivations = negVisibleProbs.mmul(this.weights);
            visible.mmuli(localWeights, hidden);

            //final FloatMatrix negHiddenProbs = logisticFunction.function(negHiddenActivations);
            logistic(hidden);

            //final FloatMatrix negAssociations = negVisibleProbs.transpose().mmul(negHiddenProbs);
            visible.transposei(visibleTrans);
            visibleTrans.mmuli(hidden, negAssociations);

            // Update weights
            posAssociations.subi(negAssociations);
            posAssociations.muli(this.learnRate / trainingData.length);
            localWeights.addi(posAssociations);
            
            stop.update(error);
        }
        System.out.println(error);
    }
    
    private void logistic(FloatMatrix m){
        float[][] data = m.getData();
        for (int i = 0; i < m.getRows(); i++){
            for (int j = 0; j < m.getColumns(); j++) {
                data[i][j] = 1.f / (float)( 1. + Math.exp(-data[i][j]) );
            }        
        }
    }

    @Override
    public float[][] getHidden(float[][] data, boolean binarizeHidden) {
        return null;
    }

    @Override
    public float[][] getVisible(float[][] data, boolean binarizeVisible) {
        return null;
    }

    @Override
    public float[][] getWeights() {
        return null;
    }

}
