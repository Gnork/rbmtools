package berlin.iconn.rbm.rbm;

import berlin.iconn.rbm.image.DataStatistics;
import berlin.iconn.rbm.logistic.ILogistic;

import java.util.Random;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

public class RBMJBlasAVG implements IRBM {

    private float learnRate;
    private float learnRatePrev;
    
    private final ILogistic logisticFunction;

    private float error;

    private FloatMatrix weights;
    private FloatMatrix avgVector;

    public RBMJBlasAVG(int inputSize, int outputSize, float learningRate, ILogistic logisticFunction, boolean useSeed, int seed, float[][] weights) {
        this.learnRate = learningRate;
        this.logisticFunction = logisticFunction;

        if (weights == null) {
            if (useSeed) {
                Random random = new Random(seed);
                float[][] weightsTemp = new float[inputSize][outputSize];
                for (int v = 0; v < inputSize; v++) {
                    for (int h = 0; h < outputSize; h++) {
                        weightsTemp[v][h] = (float)(0.01 * random.nextGaussian());
                    }
                }
                this.weights = new FloatMatrix(weightsTemp);
            } else {
                this.weights = FloatMatrix.randn(inputSize, outputSize).mmul(0.01f);
            }
            final FloatMatrix oneVectorCol = FloatMatrix.zeros(this.weights.getRows(), 1);
            final FloatMatrix oneVectorRow = FloatMatrix.zeros(1, this.weights.getColumns() + 1);

            this.weights = FloatMatrix.concatHorizontally(oneVectorCol, this.weights);
            this.weights = FloatMatrix.concatVertically(oneVectorRow, this.weights);
        } else {
            this.weights = new FloatMatrix(weights);
        }        
    }  
    
    @Override
    public float error(float[][] trainingData, boolean binarizeHidden, boolean binarizeVisible) {
        FloatMatrix data = new FloatMatrix(trainingData);
        
        avgVector = DataStatistics.getMean(data);
        data.subiColumnVector(avgVector);

        final FloatMatrix dataWithBias = FloatMatrix.concatHorizontally(FloatMatrix.ones(data.getRows(), 1), data);
        final FloatMatrix dataWithBiasTrans = dataWithBias.transpose();
        final FloatMatrix localWeights = this.weights;
        final FloatMatrix hidden = new FloatMatrix(dataWithBias.rows, localWeights.columns);
        FloatMatrix hiddenStates = new FloatMatrix(dataWithBias.rows, localWeights.columns);
        final FloatMatrix visible = new FloatMatrix(hidden.rows, localWeights.rows);
        final FloatMatrix posAssociations = new FloatMatrix(dataWithBiasTrans.rows, hidden.columns);
        final FloatMatrix negAssociations = new FloatMatrix(dataWithBiasTrans.rows, hidden.columns);
        final FloatMatrix resetBiasHidden = FloatMatrix.ones(hidden.getRows(), 1);
        final FloatMatrix resetBiasVisible = FloatMatrix.ones(visible.getRows(), 1);
        final ForkBlas forkBlas = new ForkBlas();

        // pos_hidden_activations
        forkBlas.pmmuli(dataWithBias, localWeights, hidden);
        
        // pos_hidden_probs
        logisticFunction.function(hidden);

        // pos_hidden_states
        if (binarizeHidden) {
        	hiddenStates = hidden.dup();
        	hiddenStates.gti(FloatMatrix.rand(hidden.getRows(), hidden.getColumns()));
        } else {
        	hiddenStates = hidden;
        }

        // pos_associations
        forkBlas.pmmuli(dataWithBiasTrans, hidden, posAssociations);
       
        // neg_visible_activations
        forkBlas.pmmuli(hiddenStates, localWeights.transpose(), visible);
        
        // neg_visible_probs
        logisticFunction.function(visible);
        
        visible.subiColumnVector(avgVector);

        // Fix Bias
        visible.putColumn(0, resetBiasVisible);
        
        // neg_hidden_activations
        forkBlas.pmmuli(visible, localWeights, hidden);

        // neg_hidden_probs
        logisticFunction.function(hidden);

        // neg_associations
        forkBlas.pmmuli(visible.transpose(), hidden, negAssociations);

        return (float)Math.sqrt(MatrixFunctions.pow(dataWithBias.sub(visible), 2.0f).sum() / trainingData.length / localWeights.getRows());
    }

    @Override
    public void train(float[][] trainingData, StoppingCondition stop, boolean binarizeHidden, boolean binarizeVisible) {
        FloatMatrix data = new FloatMatrix(trainingData);

        avgVector = DataStatistics.getMean(data);
        data.subiColumnVector(avgVector);
        
        final FloatMatrix dataWithBias = FloatMatrix.concatHorizontally(FloatMatrix.ones(data.getRows(), 1), data);
        final FloatMatrix dataWithBiasTrans = dataWithBias.transpose();
        final FloatMatrix localWeights = this.weights;
        final FloatMatrix hidden = new FloatMatrix(dataWithBias.rows, localWeights.columns);
        FloatMatrix hiddenStates = new FloatMatrix(dataWithBias.rows, localWeights.columns);
        FloatMatrix visible = new FloatMatrix(hidden.rows, localWeights.rows);
        final FloatMatrix posAssociations = new FloatMatrix(dataWithBiasTrans.rows, hidden.columns);
        final FloatMatrix negAssociations = new FloatMatrix(dataWithBiasTrans.rows, hidden.columns);
        final FloatMatrix resetBiasHidden = FloatMatrix.ones(hidden.getRows(), 1);
        final FloatMatrix resetBiasVisible = FloatMatrix.ones(visible.getRows(), 1);
        final ForkBlas forkBlas = new ForkBlas();
        while(stop.isNotDone()) {

            // pos_hidden_activations
            forkBlas.pmmuli(dataWithBias, localWeights, hidden);
            
            // pos_hidden_probs
            logisticFunction.function(hidden);

            // pos_hidden_states
            if (binarizeHidden) {
            	hiddenStates = hidden.dup();
            	hiddenStates.gti(FloatMatrix.rand(hidden.getRows(), hidden.getColumns()));
            } else {
            	hiddenStates = hidden;
            }

            // pos_associations
            forkBlas.pmmuli(dataWithBiasTrans, hidden, posAssociations);
           
            // neg_visible_activations
            forkBlas.pmmuli(hiddenStates, localWeights.transpose(), visible);
            
            // neg_visible_probs
            logisticFunction.function(visible);
            
            visible.subiColumnVector(avgVector);

            // Fix Bias
            visible.putColumn(0, resetBiasVisible);
            
            // neg_hidden_activations
            forkBlas.pmmuli(visible, localWeights, hidden);

            // neg_hidden_probs
            logisticFunction.function(hidden);

            // neg_associations
            forkBlas.pmmuli(visible.transpose(), hidden, negAssociations);

            // Adaptive learn rate
           // this.learnRatePrev = learnRate;
           // this.learnRate = (1.0f - 0.001f) * (1.0f - 0.001f) * learnRatePrev;
            
            // Update weights
            localWeights.addi((posAssociations.sub(negAssociations)).div((float)data.getRows()).mul(this.learnRate));
            error = (float)Math.sqrt(MatrixFunctions.pow(dataWithBias.sub(visible), 2.0f).sum() / trainingData.length / localWeights.getRows());
            
            stop.update(error);
        }

        System.out.println(error);
    }

    @Override
    public float[][] getHidden(float[][] data, boolean binarizeHidden) {

        FloatMatrix dataMatrix = new FloatMatrix(data);
        avgVector = DataStatistics.getMean(dataMatrix);
        dataMatrix.subiColumnVector(avgVector);

        // Insert bias units of 1 into the first column of data.
        final FloatMatrix oneVector = FloatMatrix.ones(dataMatrix.getRows(), 1);
        final FloatMatrix dataWithBias = FloatMatrix.concatHorizontally(oneVector, dataMatrix);
        
        // Calculate the activations of the hidden units.
        final FloatMatrix hiddenActivations = dataWithBias.mmul(this.weights);

        // Calculate the probabilities of turning the hidden units on.
        FloatMatrix hiddenNodes = logisticFunction.function(hiddenActivations);
	    //final FloatMatrix hiddenProbs = hiddenActivations;

        if (binarizeHidden) {
            float[][] randomMatrix = FloatMatrix.rand(hiddenNodes.getRows(), hiddenNodes.getColumns()).toArray2();

            float[][] tmpHiddenStates = hiddenNodes.dup().toArray2();
            for (int y = 0; y < tmpHiddenStates.length; y++) {
                for (int x = 0; x < tmpHiddenStates[y].length; x++) {
                    tmpHiddenStates[y][x] = (tmpHiddenStates[y][x] > randomMatrix[y][x]) ? 1 : 0;
                }
            }
            hiddenNodes = new FloatMatrix(tmpHiddenStates);
        }

        final FloatMatrix hiddenNodesWithoutBias = hiddenNodes.getRange(0, hiddenNodes.getRows(), 1, hiddenNodes.getColumns());

        // Ignore the bias units.
        return hiddenNodesWithoutBias.toArray2();
    }

    @Override
    public float[][] getVisible(float[][] data, boolean binarizeVisible) {

        FloatMatrix dataMatrix = new FloatMatrix(data);
        
        // Insert bias units of 1 into the first column of data.
        final FloatMatrix oneVector = FloatMatrix.ones(dataMatrix.getRows(), 1);
        final FloatMatrix dataWithBias = FloatMatrix.concatHorizontally(oneVector, dataMatrix);
        
        // Calculate the activations of the visible units.
        final FloatMatrix visibleActivations = dataWithBias.mmul(this.weights.transpose());

        // Calculate the probabilities of turning the visible units on.
        FloatMatrix visibleNodes = logisticFunction.function(visibleActivations);

        if (binarizeVisible) {
            float[][] randomMatrix = FloatMatrix.rand(visibleNodes.getRows(), visibleNodes.getColumns()).toArray2();

            float[][] tmpVisibleStates = visibleNodes.dup().toArray2();
            for (int y = 0; y < tmpVisibleStates.length; y++) {
                for (int x = 0; x < tmpVisibleStates[0].length; x++) {
                    tmpVisibleStates[y][x] = (tmpVisibleStates[y][x] > randomMatrix[y][x]) ? 1 : 0;
                }
            }

            visibleNodes = new FloatMatrix(tmpVisibleStates);
        }

        // Ignore bias
        final FloatMatrix visibleNodesWithoutBias = visibleNodes.getRange(0, visibleNodes.getRows(), 1, visibleNodes.getColumns());

        return visibleNodesWithoutBias.toArray2();

    }

    @Override
    public float[][] getWeights() {
        return this.weights.toArray2();
    }

}
