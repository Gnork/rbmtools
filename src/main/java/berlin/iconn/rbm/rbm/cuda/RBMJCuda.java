package berlin.iconn.rbm.rbm.cuda;

import berlin.iconn.rbm.logistic.ILogistic;
import berlin.iconn.rbm.rbm.IRBM;
import berlin.iconn.rbm.rbm.StoppingCondition;
import java.util.Random;
import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

/**
 * 
 * RBM implementation with native NVIDIA CUDA support for Matrix multiplications
 */
public class RBMJCuda implements IRBM {

    private final float learnRate;
    private final ILogistic logisticFunction;

    private float error;

    private FloatMatrix weights;

    public RBMJCuda(int inputSize, int outputSize, float learningRate, ILogistic logisticFunction, boolean useSeed, int seed, float[][] weights) {
        
        this.learnRate = learningRate;
        this.logisticFunction = logisticFunction;

        if (weights == null) {
            if (useSeed) {
                Random random = new Random(seed);
                float[][] weightsTemp = new float[inputSize][outputSize];
                for (int v = 0; v < inputSize; v++) {
                    for (int h = 0; h < outputSize; h++) {
                        weightsTemp[v][h] = (float)(0.01f * random.nextGaussian());
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

        final FloatMatrix oneVector = FloatMatrix.ones(data.getRows(), 1);
        final FloatMatrix dataWithBias = FloatMatrix.concatHorizontally(oneVector, data);

        //final FloatMatrix posHiddenActivations = dataWithBias.mmul(this.weights);
        final FloatMatrix posHiddenActivations = JCUDAMatrixUtils.multiply(dataWithBias, this.weights, false, false);

        FloatMatrix posHiddenNodes = logisticFunction.function(posHiddenActivations);

        if (binarizeHidden) {
            float[][] randomMatrix = FloatMatrix.rand(posHiddenNodes.getRows(), posHiddenNodes.getColumns()).toArray2();

            float[][] tmpHiddenStates = posHiddenNodes.dup().toArray2();
            for (int y = 0; y < tmpHiddenStates.length; y++) {
                for (int x = 0; x < tmpHiddenStates[y].length; x++) {
                    tmpHiddenStates[y][x] = (tmpHiddenStates[y][x] > randomMatrix[y][x]) ? 1 : 0;
                }
            }
            posHiddenNodes = new FloatMatrix(tmpHiddenStates);
        }

        posHiddenNodes.putColumn(0, FloatMatrix.ones(posHiddenNodes.getRows(), 1));

        //final FloatMatrix negVisibleActivations = posHiddenNodes.mmul(this.weights.transpose());
        final FloatMatrix negVisibleActivations = JCUDAMatrixUtils.multiply(posHiddenNodes, this.weights, false, true);

        FloatMatrix negVisibleNodes = logisticFunction.function(negVisibleActivations);

        if (binarizeVisible) {
            float[][] randomMatrix = FloatMatrix.rand(negVisibleNodes.getRows(), negVisibleNodes.getColumns()).toArray2();

            float[][] tmpVisibleStates = negVisibleNodes.dup().toArray2();
            for (int y = 0; y < tmpVisibleStates.length; y++) {
                for (int x = 0; x < tmpVisibleStates[y].length; x++) {
                    tmpVisibleStates[y][x] = (tmpVisibleStates[y][x] > randomMatrix[y][x]) ? 1 : 0;
                }
            }
            negVisibleNodes = new FloatMatrix(tmpVisibleStates);
        }

        negVisibleNodes.putColumn(0, FloatMatrix.ones(negVisibleNodes.getRows(), 1));

        return (float) Math.sqrt(MatrixFunctions.pow(dataWithBias.sub(negVisibleNodes), 2.0f).sum() / trainingData.length / weights.getRows());
    }

    @Override
    public void train(float[][] trainingData, StoppingCondition stop, boolean binarizeHidden, boolean binarizeVisible) {
        FloatMatrix data = new FloatMatrix(trainingData);

        final FloatMatrix oneVector = FloatMatrix.ones(data.getRows(), 1);
        final FloatMatrix dataWithBias = FloatMatrix.concatHorizontally(oneVector, data);

        while(stop.isNotDone()) {

            //final FloatMatrix posHiddenActivations = dataWithBias.mmul(this.weights);
            final FloatMatrix posHiddenActivations = JCUDAMatrixUtils.multiply(dataWithBias, this.weights, false, false);

            FloatMatrix posHiddenProbs = logisticFunction.function(posHiddenActivations);
            FloatMatrix posHiddenStates;

            if (binarizeHidden) {
                float[][] randomMatrix = FloatMatrix.rand(posHiddenProbs.getRows(), posHiddenProbs.getColumns()).toArray2();

                float[][] tmpHiddenStates = posHiddenProbs.dup().toArray2();
                for (int y = 0; y < tmpHiddenStates.length; y++) {
                    for (int x = 0; x < tmpHiddenStates[y].length; x++) {
                        tmpHiddenStates[y][x] = (tmpHiddenStates[y][x] > randomMatrix[y][x]) ? 1 : 0;
                    }
                }
                posHiddenStates = new FloatMatrix(tmpHiddenStates);
            } else {
            	posHiddenStates = posHiddenProbs;
            }

            //posHiddenNodes.putColumn(0, FloatMatrix.ones(posHiddenNodes.getRows(), 1));

            //final FloatMatrix posAssociations = dataWithBias.transpose().mmul(posHiddenProbs);
            final FloatMatrix posAssociations = JCUDAMatrixUtils.multiply(dataWithBias, posHiddenProbs, true, false);

            //final FloatMatrix negVisibleActivations = posHiddenStates.mmul(this.weights.transpose());
            final FloatMatrix negVisibleActivations = JCUDAMatrixUtils.multiply(posHiddenStates, this.weights, false, true);

            FloatMatrix negVisibleNodes = logisticFunction.function(negVisibleActivations);

            if (binarizeVisible) {
                float[][] randomMatrix = FloatMatrix.rand(negVisibleNodes.getRows(), negVisibleNodes.getColumns()).toArray2();

                float[][] tmpVisibleStates = negVisibleNodes.dup().toArray2();
                for (int y = 0; y < tmpVisibleStates.length; y++) {
                    for (int x = 0; x < tmpVisibleStates[y].length; x++) {
                        tmpVisibleStates[y][x] = (tmpVisibleStates[y][x] > randomMatrix[y][x]) ? 1 : 0;
                    }
                }
                negVisibleNodes = new FloatMatrix(tmpVisibleStates);
            }

            negVisibleNodes.putColumn(0, FloatMatrix.ones(negVisibleNodes.getRows(), 1));

            //final FloatMatrix negHiddenActivations = negVisibleNodes.mmul(this.weights);
            final FloatMatrix negHiddenActivations = JCUDAMatrixUtils.multiply(negVisibleNodes, this.weights, false, false);

            final FloatMatrix negHiddenProbs = logisticFunction.function(negHiddenActivations);

            //final FloatMatrix negAssociations = negVisibleNodes.transpose().mmul(negHiddenProbs);
            final FloatMatrix negAssociations = JCUDAMatrixUtils.multiply(negVisibleNodes, negHiddenProbs, true, false);

            // Update weights
            this.weights.addi((posAssociations.sub(negAssociations)).mul(this.learnRate / data.getRows()));
            error = (float)Math.sqrt(MatrixFunctions.pow(dataWithBias.sub(negVisibleNodes), 2.0f).sum() / trainingData.length / weights.getRows());

            stop.update(error);
            System.out.println(error);
        }
        //System.out.println(error);
    }

    @Override
    public float[][] getHidden(float[][] data, boolean binarizeHidden) {

        FloatMatrix dataMatrix = new FloatMatrix(data);

        // Insert bias units of 1 into the first column of data.
        final FloatMatrix oneVector = FloatMatrix.ones(dataMatrix.getRows(), 1);
        final FloatMatrix dataWithBias = FloatMatrix.concatHorizontally(oneVector, dataMatrix);

        // Calculate the activations of the hidden units.
        //final FloatMatrix hiddenActivations = dataWithBias.mmul(this.weights);
        final FloatMatrix hiddenActivations = JCUDAMatrixUtils.multiply(dataWithBias, this.weights, false, false);

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
        //final FloatMatrix visibleActivations = dataWithBias.mmul(weights.transpose());
        final FloatMatrix visibleActivations = JCUDAMatrixUtils.multiply(dataWithBias, this.weights, false, true);

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
