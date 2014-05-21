package berlin.iconn.rbm.settings;


import berlin.iconn.rbm.logistic.DefaultLogisticMatrixFunction;
import berlin.iconn.rbm.logistic.GaussMatrixFunction;
import berlin.iconn.rbm.logistic.HardClipMatrixFunction;
import berlin.iconn.rbm.logistic.ILogistic;
import berlin.iconn.rbm.logistic.LinearClippedMatrixFunction;
import berlin.iconn.rbm.logistic.LinearInterpolatedMatrixFunction;
import berlin.iconn.rbm.logistic.LinearUnclippedMatrixFunction;
import berlin.iconn.rbm.logistic.RectifierMatrixFunction;
import berlin.iconn.rbm.logistic.SquareRootLogistic;
import berlin.iconn.rbm.logistic.TanHMatrixFunction;
import berlin.iconn.rbm.persistence.Conserve;
import berlin.iconn.rbm.rbm.RBMJBlas;
import berlin.iconn.rbm.rbm.RBMJBlasAVG;
import berlin.iconn.rbm.rbm.RBMJBlasOpti;

/**
 *
 * @author Moritz
 */
public class RBMSettingsMainModel{
	
    private final RBMSettingsMainController controller;
    
    //combobox select options
    private final String[] rbmImplementations = {
        "RBMJBlasAVG",
        "RBMJBlasOpti",
        "RBMJBlas"
    };

    public RBMSettingsMainModel(RBMSettingsMainController controller) {
        this.controller = controller;
    }

    private final String[] logisticFunctions = {
        "Standard",
        "Gaussian",
        "Hard Clip",
        "Linear Clipped",
        "Linear Interpolated",
        "Linear Unclipped (Absolute Value)",
        "Rectifier",
        "TanH",
        "SqareRoot"
    };
    
    private final ILogistic[] logisticFunctionImplementation = {
        new DefaultLogisticMatrixFunction(),
        new GaussMatrixFunction(),
        new HardClipMatrixFunction(),
        new LinearClippedMatrixFunction(),
        new LinearInterpolatedMatrixFunction(),
        new LinearUnclippedMatrixFunction(),
        new RectifierMatrixFunction(),
        new TanHMatrixFunction(),
        new SquareRootLogistic()
    };
    
    @Conserve
    private int selectedRbmImplementation = 0;
    @Conserve
    private int selectedLogisticFunction = 0;
    @Conserve
    private int inputSize = 0;
    @Conserve
    private int outputSize = 150;

    public int getSelectedRbmImplementation() {
        return selectedRbmImplementation;
    }
    
    public Class getSelectedRbmImplementationClass(){
        switch(this.getSelectedRbmImplementation()){
        case 0:
            return RBMJBlasAVG.class;
        case 1:
            return RBMJBlasOpti.class;
        case 2:
            return RBMJBlas.class;
        default:
            return RBMJBlasOpti.class;
        }       
    }

    public void setSelectedRbmImplementation(int selectedRbmImplementation) {
        this.selectedRbmImplementation = selectedRbmImplementation;
    }

    public int getSelectedLogisticFunction() {
        return selectedLogisticFunction;
    }
    
    public ILogistic getSelectedLogisticFunctionImplementation() {
        return this.logisticFunctionImplementation[selectedLogisticFunction];
    }

    public void setSelectedLogisticFunction(int selectedLogisticFunction) {
        this.selectedLogisticFunction = selectedLogisticFunction;
    }

    public void setInputSize(int inputSize) {
        this.inputSize = inputSize;
        this.controller.update();
    }

    public int getOutputSize() {
        return outputSize;
    }

    public void setOutputSize(int outputSize) {
        this.outputSize = outputSize;
    }

    public String[] getRbmImplementations() {
        return rbmImplementations;
    }

    public String[] getLogisticFunctions() {
        return logisticFunctions;
    }

    public int getInputSize() {
        return this.inputSize;
    }
    
}
