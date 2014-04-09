package berlin.iconn.rbm.rbm;

import berlin.iconn.rbm.logistic.ILogistic;
import java.text.DecimalFormat;
import java.util.Random;


public class RBMOriginal implements IRBM {
	
   
    private int numHidden;
    private int numVisible;
    private int numHiddenWithBias;
    private int numVisibleWithBias;
    private float learningRate;
    
    private float error;
    
    private Random randomGenerator = new Random();
    private float[][] weights;
    
    
    public RBMOriginal(int numVisbible, int numHidden, float learningRate, float[][] weights) {
    	
		this.numHidden = numHidden;
		this.numVisible = numVisbible;
		this.numHiddenWithBias = numHidden + 1;
		this.numVisibleWithBias = numVisbible + 1;
		this.learningRate = learningRate;
		
		this.weights = new float[numVisibleWithBias][numHiddenWithBias];
	    for(int v = 1; v < numVisibleWithBias; v++) {
	    	for(int h = 1; h < numHiddenWithBias; h++) {
	    		this.weights[v][h] = weights[v -1][h - 1];
	    	}
	    }
            setWeights(weights);
    }
    
	public RBMOriginal(int numVisbible, int numHidden, float learningRate) {
		this.numHidden = numHidden;
		this.numVisible = numVisbible;
		this.numHiddenWithBias = numHidden + 1;
		this.numVisibleWithBias = numVisbible + 1;
		this.learningRate = learningRate;
		
		
		this.weights = new float[numVisibleWithBias][numHiddenWithBias];
	    for(int v = 1; v < numVisibleWithBias; v++) {
	    	for(int h = 1; h < numHiddenWithBias; h++) {
	    		weights[v][h] = (float)(this.learningRate * randomGenerator.nextGaussian());
	    	}
	    }
	}
	
	public void printMatrix(String title, float[][] m){
	    try{
	    	DecimalFormat f = new DecimalFormat("#0.000");
	    	
	        int rows = m.length;
	        int cols = m[0].length;
	        
	        String str = title + "\n" + "|\t";

	        for(int r=0;r<rows;r++){
	            for(int c=0;c<cols;c++){
	                str += f.format(m[r][c]) + "\t";
	            }

	            //System.out.println(str + "|");
	            str = "|\t";
	        }

	    }catch(Exception e){System.out.println("Matrix is empty!!");}
	}
	

	
	public float[][] logistic(float[][] matrix) {
		
		int rows = matrix.length;
		int cols = matrix[0].length;
		
		float[][] result = new float[rows][cols];
		
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < cols; c++) {
				result[r][c] = 1.0f / (float)(1.0 + Math.exp(-matrix[r][c]));
			}
		}
		
		return result;
	}

	public static float[][] transposeMatrix(float [][] matrix){
		
		int rows = matrix.length;
		int cols = matrix[0].length;
		
        float[][] result = new float[cols][rows];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result[j][i] = matrix[i][j];
            	
        return result;
    }

	public float[][] multiplicar(float[][] matrixA, float[][] matrixB) {

        int aRows = matrixA.length;
        int aCols = matrixA[0].length;
        int bRows = matrixB.length;
        int bCols = matrixB[0].length;

        if (aCols != bRows) {
            throw new IllegalArgumentException("A:Rows: " + aCols + " did not match B:Columns " + bRows + ".");
        }

        float[][] result = new float[aRows][bCols];

        for (int r = 0; r < aRows; r++) { // aRow
            for (int c = 0; c < bCols; c++) { // bColumn
                for (int k = 0; k < aCols; k++) { // aColumn
                    result[r][c] += matrixA[r][k] * matrixB[k][c];
                }
            }
        }

        return result;
    }
	
	@Override
	public float error(float[][] trainingData, boolean useHiddenStates, boolean useVisibleStates) {
		return error;
	}
	
	@Override
	public void train(float[][] trainingData, StoppingCondition stop, boolean useHiddenStates, boolean useVisibleStates) {
		
		//Printer.printMatrix("weights", weights);
		/*
	    Train the machine.

	    Parameters
	    ----------
	    data: A matrix where each row is a training example consisting of the states of visible units.    
	    */

		int numberOfExamples = trainingData.length;
		int numberOfChoicesPerExample = trainingData[0].length;

	    // Insert bias units of 1 into the first column.
	    float[][] dataWithBias = new float[numberOfExamples][numberOfChoicesPerExample + 1];
	    for(int r = 0; r < numberOfExamples; r++) {
	    	for(int c = 0; c < numberOfChoicesPerExample + 1; c++) {
	    		if(c == 0) {
	    			dataWithBias[r][c] = 1;
	    		} else {
	    			dataWithBias[r][c] = trainingData[r][c-1];
	    		}
	    	}
	    }
	    
	    //Printer.printMatrix("DataWithBias", dataWithBias);
	    
	    while(stop.isNotDone()) {
	    	
	    	// Clamp to the data and sample from the hidden units. 
		    // (This is the "positive CD phase", aka the reality phase.)
	    	float[][] posHiddenActivations = multiplicar(dataWithBias, this.weights);
	    	//Printer.printMatrix("posHiddenActivations", posHiddenActivations);
	    	
	    	float[][] posHiddenProbs = logistic(posHiddenActivations);
	    	//Printer.printMatrix("posHiddenProbs", posHiddenProbs);
	    	/*
	    	float[][] posHiddenStates = new float[rLength][cLength];
		    for(int r = 0; r < rLength; r++) {
		    	for(int c = 0; c < cLength; c++) {
		    		posHiddenStates[r][c] = (posHiddenProbs[r][c] > randomMatrix[r][c]) ? 1 : 0; 
		    	}
		    }
		    */
                /*for(int r = 0; r < posHiddenProbs.length; r++) {
		    	for(int c = 0; c < posHiddenProbs[0].length; c++) {
		    		if(c==0) posHiddenProbs[r][c] = 1;
		    	}
		    }
		    */
		    float[][] dataWithBiasT = transposeMatrix(dataWithBias);
		    
		    float[][] posAssociations = multiplicar(dataWithBiasT, posHiddenProbs);
	    	//Printer.printMatrix("posAssociations", posAssociations);
		    
		    
		    float[][] weightsT = transposeMatrix(this.weights);
		    
		    float[][] negVisibleActivations = multiplicar(posHiddenProbs, weightsT);
	    	//Printer.printMatrix("negVisibleActivations", negVisibleActivations);
		    
		    float[][] negVisibleProbs = logistic(negVisibleActivations);
		    
		    for(int r = 0; r < negVisibleProbs.length; r++) {
		    	for(int c = 0; c < negVisibleProbs[0].length; c++) {
		    		if(c==0) negVisibleProbs[r][c] = 1;
		    	}
		    }
	    	//Printer.printMatrix("negVisibleProbs", negVisibleProbs);
		    
		    float[][] negHiddenActivations = multiplicar(negVisibleProbs, this.weights);
	    	//Printer.printMatrix("negHiddenActivations", negHiddenActivations);
		    
		    float[][] negHiddenProbs = logistic(negHiddenActivations);
	    	//Printer.printMatrix("negHiddenProbs", negHiddenProbs);
		    
		    float[][] negVisibleProbsT = transposeMatrix(negVisibleProbs);
		    
		    float[][] negAssociations = multiplicar(negVisibleProbsT, negHiddenProbs);
	     //	Printer.printMatrix("negAssociations", negAssociations);
		    
		    // Update weights
		    for(int r = 0; r < weights.length; r++) {
		    	for(int c = 0; c < weights[0].length; c++) {
		    		weights[r][c] += this.learningRate * ((posAssociations[r][c] - negAssociations[r][c]) / numberOfExamples);
//		    		weights[r][c] += (posAssociations[r][c] - negAssociations[r][c]);
		    	}
		    }
	    	//Printer.printMatrix("weights", weights);
	    	//System.out.println(numberOfExamples);
		    
		    error = 0;
		    for(int r = 0; r < negVisibleProbs.length; r++) {
		    	for(int c = 0; c < negVisibleProbs[0].length; c++) {
		    		error += (dataWithBias[r][c] - negVisibleProbs[r][c]) * (dataWithBias[r][c] - negVisibleProbs[r][c]);
		    	}
		    }
                    stop.update(error);
		    //System.out.println(error);

	    }

	}
	
	// boolean useHiddenStates not implemented
	
	public float[][] run_visible(float[][] userData, boolean useHiddenStates) {
		/*
	    Assuming the RBM has been trained (so that weights for the network have been learned),
	    run the network on a set of visible units, to get a sample of the hidden units.

	    Parameters
	    ----------
	    data: A matrix where each row consists of the states of the visible units.

	    Returns
	    -------
	    hidden_states: A matrix where each row consists of the hidden units activated from the visible
	    units in the data matrix passed in.
		*/
		
		int numberOfExamples = userData.length;
		int numberOfChoicesPerExample = userData[0].length;
		
		/*
		float[][] hiddenStates = new float[numberOfExamples][this.numHidden + 1];
	    for(int r = 0; r < numberOfExamples; r++) {
	    	for(int c = 0; c < this.numHidden + 1; c++) {
	    		hiddenStates[r][c] = 1;
	    	}
	    }
	    */
		// printMatrix("hiddenStates:", hiddenStates);
		
	    // Insert bias units of 1 into the first column.
	    float[][] dataWithBias = new float[numberOfExamples][numberOfChoicesPerExample + 1];
	    for(int r = 0; r < numberOfExamples; r++) {
	    	for(int c = 0; c < numberOfChoicesPerExample + 1; c++) {
	    		if(c == 0) {
	    			dataWithBias[r][c] = 1;
	    		} else {
	    			dataWithBias[r][c] = userData[r][c-1];
	    		}
	    	}
	    }
	   // printMatrix("dataWithBias:", dataWithBias);
	    
	    // Calculate the activations of the hidden units.
	    float[][] hiddenActivations = multiplicar(dataWithBias, this.weights);
	   // printMatrix("hiddenActivations:", hiddenActivations);
		
	    // Calculate the probabilities of turning the hidden units on.
	    float[][] hiddenProbs = logistic(hiddenActivations);
	    // printMatrix("hiddenProbs:", hiddenProbs);
	    
	    // Turn the hidden units on with their specified probabilities.
	    /*
	    for(int r = 0; r < numberOfExamples; r++) {
	    	for(int c = 0; c < this.numHidden + 1; c++) {
	    		hiddenStates[r][c] = (hiddenProbs[r][c] > randomGenerator.nextDouble()) ? 1 : 0; 
	    	}
	    }
	    */
	    // printMatrix("hiddenStates:", hiddenProbs);

	    
	    float[][] hiddenStatesWithoutBias = new float[numberOfExamples][this.numHidden];
	    for(int r = 0; r < numberOfExamples; r++) {
	    	for(int c = 1; c < this.numHidden + 1; c++) {
	    		hiddenStatesWithoutBias[r][c - 1] = hiddenProbs[r][c];
	    	}
	    }

	    // Ignore the bias units.
	    return hiddenStatesWithoutBias;
	}
	
	// boolean useVisibleStates not implemented
	
        public float[][] run_hidden(float[][] hiddenData, boolean useVisibleStates) {

		int numberOfExamples = hiddenData.length;
		int numberOfChoicesPerExample = hiddenData[0].length;
	    
	    // Create a matrix, where each row is to be the visible units (plus a bias unit)
		float[][] visibleStates = new float[numberOfExamples][this.numVisible + 1];
		
	    for(int r = 0; r < numberOfExamples; r++) {
	    	for(int c = 0; c < this.numVisible + 1; c++) {
	    		visibleStates[r][c] = 1;
	    	}
	    }
	    
	    // Insert bias units of 1 into the first column of data.
	    float[][] dataWithBias = new float[numberOfExamples][numberOfChoicesPerExample + 1];
	    for(int r = 0; r < numberOfExamples; r++) {
	    	for(int c = 0; c < numberOfChoicesPerExample + 1; c++) {
	    		if(c == 0) {
	    			dataWithBias[r][c] = 1;
	    		} else {
	    			dataWithBias[r][c] = hiddenData[r][c-1];
	    		}
	    	}
	    }
	    
	    float[][] weightsT = transposeMatrix(weights);
	    // Calculate the activations of the visible units.
	    float[][] visibleActivations = multiplicar(dataWithBias, weightsT);
	  
	    // Calculate the probabilities of turning the visible units on.
	    float[][] visibleProbs = this.logistic(visibleActivations);
	    
	    // Turn the visible units on with their specified probabilities.
	    /*
	    for(int r = 0; r < visibleStates.length; r++) {
	    	for(int c = 0; c < visibleStates[0].length; c++) {
	    		visibleStates[r][c] = visibleProbs[r][c] > randomGenerator.nextDouble() ? 1 : 0;
	    	}
	    }
	    */
	    
	    printMatrix("visible states", visibleStates);
	    
	    // Ignore bias
	    float[][] visibleStatesWithoutBias = new float[numberOfExamples][this.numVisible];
	    for(int r = 0; r < numberOfExamples; r++) {
	    	for(int c = 1; c < this.numVisible + 1; c++) {
	    		visibleStatesWithoutBias[r][c - 1] = visibleStates[r][c];
	    	}
	    }
	    
	    return visibleProbs;
	    
	}
	
	
	public void setWeights(float[][] weights) {
		this.weights = weights;
	} 
	
	@Override
	public float[][] getWeights() {
		return this.weights;
	}



	public static void main(String[] args) {
		RBMOriginal rbm = new RBMOriginal(6, 2, 0.1f);

		float data[][] = {
						// Alice: (Harry Potter = 1, Avatar = 1, LOTR 3 = 1, Gladiator = 0, Titanic = 0, Glitter = 0). Big SF/fantasy fan.
						{ 1, 1, 1, 0, 0, 0 },
						// Bob: (Harry Potter = 1, Avatar = 0, LOTR 3 = 1, Gladiator = 0, Titanic = 0, Glitter = 0). SF/fantasy fan, but doesn't like Avatar.
						{ 1, 0, 1, 0, 0, 0 },
						// Carol: (Harry Potter = 1, Avatar = 1, LOTR 3 = 1, Gladiator = 0, Titanic = 0, Glitter = 0). Big SF/fantasy fan.
						{ 1, 1, 1, 0, 0, 0 },
						// David: (Harry Potter = 0, Avatar = 0, LOTR 3 = 1, Gladiator = 1, Titanic = 1, Glitter = 0). Big Oscar winners fan.
						{ 0, 0, 1, 1, 1, 0 },
						// Eric: (Harry Potter = 0, Avatar = 0, LOTR 3 = 1, Gladiator = 1, Titanic = 0, Glitter = 0). Oscar winners fan, except for Titanic.
						{ 0, 0, 1, 1, 0, 0 },
						// Fred: (Harry Potter = 0, Avatar = 0, LOTR 3 = 1, Gladiator = 1, Titanic = 1, Glitter = 0). Big Oscar winners fan.
						{ 0, 0, 1, 1, 1, 0 },
	    			   };

		rbm.train(data, new StoppingCondition(10000), false, false);
		rbm.printMatrix("Weights", rbm.weights);
		
		float user[][] = {
				// Gregory: (Harry Potter = 1, Avatar = 1, LOTR 3 = 1, Gladiator = 0, Titanic = 0, Glitter = 0). Big SF/fantasy fan.
				{ 0, 0, 1, 1, 1, 0 },
				{ 1, 1, 1, 0, 0, 0 }
		};
		
		for(int i = 0; i < 1; i++) {
			rbm.printMatrix("User", user);
			float[][] result1 = rbm.run_visible(user, true);
			rbm.printMatrix("Result", result1);
			float[][] result2 = rbm.run_hidden(result1, true);
			rbm.printMatrix("Check", result2);
			//System.out.println("");
		}
		
	}

		public int getInputSize() {
		return numVisible;
	}

		public int getOutputSize() {
		return numHidden;
	}

		public float getLearnRate() {
		// TODO Auto-generated method stub
		return 0;
	}

		public ILogistic getLogisticFunction() {
		// TODO Auto-generated method stub
		return null;
	}

		public boolean hasBias() {
		return true;
	}

    @Override
    public float[][] getHidden(float[][] data, boolean binarizeHidden) {
        return null;
    }

    @Override
    public float[][] getVisible(float[][] data, boolean binarizeVisible) {
        return null;
        
    }

}
