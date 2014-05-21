package berlin.iconn.rbm.rbm;

import berlin.iconn.rbm.cl.CLFloatMatrix;
import berlin.iconn.rbm.cl.OCL;
import berlin.iconn.rbm.logistic.ILogistic;
import java.util.Random;
import org.jblas.FloatMatrix;

public class RBMCL implements IRBM {

    private final float learnRate;
    private final ILogistic logisticFunction;

    private float error;

    private CLFloatMatrix weights;
    
    int numExamples;
    final int numVisible;
    final int numHidden;

    public RBMCL(int inputSize, int outputSize, float learningRate, ILogistic logisticFunction, boolean useSeed, int seed, float[][] weights) {
        
        System.out.println("Create RBMJBlasOpti");
        
        this.numVisible = inputSize;
        this.numHidden = outputSize;
        
        this.learnRate = learningRate;
        this.logisticFunction = logisticFunction;
        
        FloatMatrix w;

        if (weights == null) {
            if (useSeed) {
                Random random = new Random(seed);
                float[][] weightsTemp = new float[inputSize][outputSize];
                for (int v = 0; v < inputSize; v++) {
                    for (int h = 0; h < outputSize; h++) {
                        weightsTemp[v][h] = (float)(0.01f * random.nextGaussian());
                    }
                }
                w = new FloatMatrix(weightsTemp);
            } else {
                w = FloatMatrix.randn(inputSize, outputSize).mmul(0.01f);
            }
            final FloatMatrix oneVectorCol = FloatMatrix.zeros(w.getRows(), 1);
            final FloatMatrix oneVectorRow = FloatMatrix.zeros(1, w.getColumns() + 1);

            w = FloatMatrix.concatHorizontally(oneVectorCol, w);
            w = FloatMatrix.concatVertically(oneVectorRow, w);
        } else {
            w = new FloatMatrix(weights);
        } 

	this.weights = new CLFloatMatrix(w.toArray2());
    }  

    @Override
    public void train(float[][] trainingData, StoppingCondition stop, boolean binarizeHidden, boolean binarizeVisible) {
        
	final int numExamples = trainingData.length;

	final CLFloatMatrix posHiddenProbs = new CLFloatMatrix(numExamples, numHidden);
	final CLFloatMatrix posAssociations = new CLFloatMatrix(numVisible, numHidden);
	final CLFloatMatrix reconstructedDataProbs = new CLFloatMatrix(numExamples, numVisible);
	final CLFloatMatrix negHiddenProbs = new CLFloatMatrix(numExamples, numHidden);
	final CLFloatMatrix negAssociations = new CLFloatMatrix(numVisible, numHidden);
	final CLFloatMatrix gradient = new CLFloatMatrix(numVisible, numHidden);

        final CLFloatMatrix data = new CLFloatMatrix(trainingData);
        final CLFloatMatrix biasedData = data.putColVecOnes();

        while(stop.isNotDone()) {

            // positive phase
            getHidden(posHiddenProbs, data);

            // https://github.com/echen/restricted-boltzmann-machines/blob/master/rbm.py
            // kann auch nach den posAssociations berechnet werden -> starke bias filterbilder
            posHiddenProbs.randomQuantisation();
            posHiddenProbs.putColVecOnes();

            posAssociations.mmullt(data, posHiddenProbs);

            // reconstruction
            getVisible(reconstructedDataProbs, posHiddenProbs);
            reconstructedDataProbs.putColVecOnes();

            // negative phase
            getHidden(negHiddenProbs, reconstructedDataProbs);

            // associations for both phases
            negAssociations.mmullt(reconstructedDataProbs, negHiddenProbs);

            // update weights
            gradient.sub(posAssociations, negAssociations).mul(this.learnRate);
            weights.add(weights, gradient);

            // calculate error only once in an updateInterval
            error = calcError(data, reconstructedDataProbs);

            stop.update(error);
        }
        System.out.println(error);
    }
    
    public double calcError(CLFloatMatrix data) {
        
        final CLFloatMatrix posHiddenProbs = new CLFloatMatrix(numExamples, numHidden);
        final CLFloatMatrix reconstructedDataProbs = new CLFloatMatrix(numExamples, numVisible);

        // positive phase
        getHidden(posHiddenProbs, data);

        // reconstruction
        getVisible(reconstructedDataProbs, posHiddenProbs);

        // calculate error
        return calcError(data, reconstructedDataProbs);
    }

    public float calcError(final CLFloatMatrix data, final CLFloatMatrix reconstruction) {     
        return (float)Math.sqrt(reconstruction.sub(data, reconstruction).sqr().sum() / (numExamples * numVisible)) * 255;
    }
    
    public float error(float[][] trainingData, boolean binarizeHidden, boolean binarizeVisible) {
        
        CLFloatMatrix data = new CLFloatMatrix(trainingData);
        
	final CLFloatMatrix posHiddenProbs = new CLFloatMatrix(numExamples, numHidden);
	final CLFloatMatrix reconstructedDataProbs = new CLFloatMatrix(numExamples, numVisible);
		
        // positive phase
        getHidden(posHiddenProbs, data);

	// reconstruction
	getVisible(reconstructedDataProbs, posHiddenProbs);

	// calculate error
	return calcError(data, reconstructedDataProbs);
    }
    
    public void getHidden(final CLFloatMatrix posHiddenProbs, final CLFloatMatrix data) {
        
	posHiddenProbs.mmul(data, weights).sigfunc();
    }

    public void getVisible(final CLFloatMatrix reconstructedDataProbs, final CLFloatMatrix hiddenProbs) {
        
	reconstructedDataProbs.mmulrt(hiddenProbs, weights).sigfunc();
    }

    @Override
    public float[][] getHidden(float[][] data, boolean binarizeHidden) {

        
        CLFloatMatrix hidden = new CLFloatMatrix(numExamples, numHidden);
        CLFloatMatrix visibleData = new CLFloatMatrix(data);
        
        getHidden(hidden, visibleData);
        
        return hidden.getFloatMatrix().toArray2();

    }

    @Override
    public float[][] getVisible(float[][] data, boolean binarizeVisible) {
        
        CLFloatMatrix visible = new CLFloatMatrix(numExamples, numVisible);
        CLFloatMatrix hiddenData = new CLFloatMatrix(data);
        
        getVisible(visible, hiddenData);
        
        return visible.getFloatMatrix().toArray2();
    }

    @Override
    public float[][] getWeights() {
        return this.weights.getFloatMatrix().toArray2();
    }

}
