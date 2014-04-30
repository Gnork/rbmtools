/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.rbm;

import berlin.iconn.rbm.enhancement.RBMEnhancer;
import berlin.iconn.rbm.enhancement.TrainingVisualizer;
import berlin.iconn.rbm.logistic.ILogistic;
import berlin.iconn.rbm.main.BenchmarkModel;
import berlin.iconn.rbm.persistence.XMLEndTrainingLogger;
import berlin.iconn.rbm.settings.RBMSettingsController;
import berlin.iconn.rbm.settings.RBMSettingsLearningRateController;
import berlin.iconn.rbm.settings.RBMSettingsLearningRateModel;
import berlin.iconn.rbm.settings.RBMSettingsLoggerController;
import berlin.iconn.rbm.settings.RBMSettingsLoggerModel;
import berlin.iconn.rbm.settings.RBMSettingsMainController;
import berlin.iconn.rbm.settings.RBMSettingsMainModel;
import berlin.iconn.rbm.settings.RBMSettingsModel;
import berlin.iconn.rbm.settings.RBMSettingsStoppingConditionController;
import berlin.iconn.rbm.settings.RBMSettingsStoppingConditionModel;
import berlin.iconn.rbm.settings.RBMSettingsVisualizationsController;
import berlin.iconn.rbm.settings.RBMSettingsVisualizationsModel;
import berlin.iconn.rbm.settings.RBMSettingsWeightsController;
import berlin.iconn.rbm.settings.RBMSettingsWeightsModel;
import berlin.iconn.rbm.views.ErrorViewModel;
import berlin.iconn.rbm.views.imageviewer.ImageViewerModel;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author radek, christoph
 */
public class RBMTrainer {
    
    private Task<Void> task = null;

    public void trainAllRBMs(BenchmarkModel benchmarkModel) {
            if (task == null || !task.isRunning()) {
                this.updateRBMs(benchmarkModel);
                final LinkedList<RBMSettingsController> rbmSettingsList = benchmarkModel.getRbmSettingsList();

                final LinkedList<RBMEnhancer> rbms = new LinkedList<>();

                for (RBMSettingsController c : rbmSettingsList) {
                    rbms.add(new RBMEnhancer(this.createRBMForTemporaryUse(c)));
                }

                task = new Task<Void>() {

                    LinkedList<RBMEnhancer> rbmList = rbms;
                    int counter = 0;

                    @Override
                    protected Void call() throws Exception {
                        RBMEnhancer rbm;
                        RBMSettingsController lastController = null;
                        for (RBMSettingsController c : rbmSettingsList) {
                            rbm = rbmList.removeFirst();
                            System.out.println("RBM " + counter++);
                            if (lastController != null) {
                                float[][] data = getHiddenSingleRBM(lastController, lastController.getModel().getData());
                                c.getModel().setData(data);
                            }
                            RBMTrainer.this.trainSingleRBM(c, rbm);
                            lastController = c;
                        }
                        return null;
                    }

                    @Override
                    protected void cancelled() {
                        super.cancelled();
                        updateMessage("RBM Training cancelled!");
                    }

                    @Override
                    protected void succeeded() {
                        super.succeeded();
                        updateMessage("Training for all RBMs finished");
                    }
                };
                
                for (RBMEnhancer rbm : rbms) {
                    rbm.setTask(task);
                    
                }
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
        }
    }

    public IRBM createRBMForTemporaryUse(RBMSettingsController controller) {
        RBMSettingsModel model = controller.getModel();
        RBMSettingsMainModel mainModel = model.getController(RBMSettingsMainController.class).getModel();
        RBMSettingsWeightsModel weightsModel = model.getController(RBMSettingsWeightsController.class).getModel();
        RBMSettingsLearningRateModel learningRateModel = model.getController(RBMSettingsLearningRateController.class).getModel();

        int inputSize = mainModel.getInputSize();
        int outputSize = mainModel.getOutputSize();
        ILogistic logisticFunction = mainModel.getSelectedLogisticFunctionImplementation();
        float learningRate = learningRateModel.getConstantLearningRate();
        int seed = weightsModel.getSeed();
        boolean useSeed = weightsModel.isUseSeed();
        float[][] weights = weightsModel.getWeights();

        Class rbmImplementation = mainModel.getSelectedRbmImplementationClass();
        try {
            Constructor rbmConstructor = rbmImplementation.getConstructor(int.class, int.class, float.class, ILogistic.class, boolean.class, int.class, float[][].class);
            return (IRBM)rbmConstructor.newInstance(inputSize, outputSize, learningRate, logisticFunction, useSeed, seed, weights);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(RBMTrainer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    public void trainSingleRBM(RBMSettingsController controller, RBMEnhancer rbmEnhancer) {
        System.out.println("Training started...");
        RBMSettingsModel model = controller.getModel();
        RBMSettingsWeightsModel weightsModel = model.getController(RBMSettingsWeightsController.class).getModel();
        RBMSettingsLoggerModel loggerModel = model.getController(RBMSettingsLoggerController.class).getModel();
        RBMSettingsStoppingConditionModel stoppingConditionModel = model.getController(RBMSettingsStoppingConditionController.class).getModel();
        RBMSettingsVisualizationsModel visualizationsModel = model.getController(RBMSettingsVisualizationsController.class).getModel();

        if (loggerModel.isFinalLoggerOn()) {
            rbmEnhancer.addEnhancement(new XMLEndTrainingLogger());
        }

        int weightsInterval = visualizationsModel.getWeightsInterval();
        int errorInterval = visualizationsModel.getErrorInterval();
        int featuresInterval = visualizationsModel.getFeaturesInterval();
        //int featuresInterval = visualizationsModel.getFeaturesInterval();

        if (visualizationsModel.isShowWeights()) {
            rbmEnhancer.addEnhancement(new TrainingVisualizer(weightsInterval,
                    visualizationsModel.getWeightsVisualizationController().getModel()));
        }

        if (visualizationsModel.isShowErrorGraph()) {
            ErrorViewModel errorViewModel = visualizationsModel.getErrorViewController().getModel();
            errorViewModel.clear();
            rbmEnhancer.addEnhancement(new TrainingVisualizer(errorInterval, errorViewModel));
        }

        
        ImageViewerModel featuresViewer = visualizationsModel.getImageViewController().getModel();
        rbmEnhancer.addEnhancement(new TrainingVisualizer(featuresInterval, featuresViewer));
        
        
        StoppingCondition stop;
        if (stoppingConditionModel.isEpochsOn() && stoppingConditionModel.isErrorOn()) {
            stop = new StoppingCondition(stoppingConditionModel.getEpochs(), stoppingConditionModel.getError());
        } else if (!stoppingConditionModel.isEpochsOn() && stoppingConditionModel.isErrorOn()) {
            stop = new StoppingCondition(stoppingConditionModel.getError());
        } else if (stoppingConditionModel.isEpochsOn() && !stoppingConditionModel.isErrorOn()) {
            stop = new StoppingCondition(stoppingConditionModel.getEpochs());
        } else {
            stop = new StoppingCondition();
        }

        long startTime = System.currentTimeMillis();
        rbmEnhancer.train(model.getData(), stop, weightsModel.isBinarizeHidden(), weightsModel.isBinarizeVisible());
        System.out.println("Training finished in " + (System.currentTimeMillis() - startTime) + "ms");

        weightsModel.setWeights(rbmEnhancer.getWeights());
    }

    // GET HIDDEN
    public float[][] getHiddenAllRBMs(BenchmarkModel benchmarkModel, float[][] data) {
        return getHiddenAllRBMs(benchmarkModel, data, false, false);
    }

    public float[] getHiddenAllRBMs1D(BenchmarkModel benchmarkModel, float[] data, boolean binarizeHidden) {
        float[][] data2Dimensions = vectorToMatrix(data);

        float[][] hiddenData2Dimensions = getHiddenAllRBMs(benchmarkModel, data2Dimensions, binarizeHidden, true);

        float hiddenData[] = matrixToVector(hiddenData2Dimensions);

        return hiddenData;
    }

    public float[][] getHiddenAllRBMs(BenchmarkModel benchmarkModel, float[][] data, boolean binarizeHidden) {
        return getHiddenAllRBMs(benchmarkModel, data, binarizeHidden, true);
    }

    private float[][] getHiddenAllRBMs(BenchmarkModel benchmarkModel, float[][] data, boolean binarizeHidden, boolean useBinarizeOptionGiven) {
        LinkedList<RBMSettingsController> rbmSettingsList = benchmarkModel.getRbmSettingsList();

        float[][] visibleData = data;

        for (RBMSettingsController rbmSettingsController : rbmSettingsList) {
            float[][] hiddenData;
            if (useBinarizeOptionGiven) {
                hiddenData = this.getHiddenSingleRBM(rbmSettingsController, visibleData, binarizeHidden);
            } else {
                hiddenData = this.getVisibleSingleRBM(rbmSettingsController, visibleData);
            }
            visibleData = hiddenData;
        }

        float[][] hiddenDataFinal = visibleData;
        return hiddenDataFinal;
    }

    public float[][] getHiddenSingleRBM(RBMSettingsController controller, float[][] data) {
        boolean binarizeHidden = controller.getModel().getController(RBMSettingsWeightsController.class).getModel().isBinarizeHidden();
        return this.getHiddenSingleRBM(controller, data, binarizeHidden);
    }

    public float[][] getHiddenSingleRBM(RBMSettingsController controller, float[][] data, boolean binarizeHidden) {
        float[][] hiddenData = null;

        IRBM rbm = this.createRBMForTemporaryUse(controller);

        if (data != null) {
            hiddenData = rbm.getHidden(data, binarizeHidden);
        } else if (controller.getModel().getData() != null) {
            data = controller.getModel().getData();
            hiddenData = rbm.getHidden(data, binarizeHidden);
        } else {
            throw new IllegalArgumentException(
                    "The data in the model was never set. "
                    + "Not inside of the first RBMSettingsController "
                    + "and is also not given inside of the methods "
                    + "parameter list (float[][] data)"
            );
        }

        return hiddenData;
    }

    public float[] getHiddenSingleRBM(RBMSettingsController controller, float[] data, boolean binarizeHidden) {
        float[][] data2Dimensions = vectorToMatrix(data);

        float[][] hiddenData2Dimensions = getHiddenSingleRBM(controller, data2Dimensions, binarizeHidden);

        float hiddenData[] = matrixToVector(hiddenData2Dimensions);

        return hiddenData;
    }

    // GET VISIBLE
    public float[][] getVisibleAllRBMs(BenchmarkModel benchmarkModel, float[][] data) {
        return getVisibleAllRBMs(benchmarkModel, data, false, false);
    }

    public float[] getVisibleAllRBMs1D(BenchmarkModel benchmarkModel, float[] data, boolean binarizeVisible) {
        float[][] data2Dimensions = vectorToMatrix(data);
        // TODO
        float[][] visibleData2Dimensions = getVisibleAllRBMs(benchmarkModel, data2Dimensions, binarizeVisible, true);

        float visibleData[] = matrixToVector(visibleData2Dimensions);

        return visibleData;
    }

    public float[][] getVisibleAllRBMs(BenchmarkModel benchmarkModel, float[][] data, boolean binarizeVisible) {
        return getVisibleAllRBMs(benchmarkModel, data, binarizeVisible, true);
    }

    private float[][] getVisibleAllRBMs(BenchmarkModel benchmarkModel, float[][] data, boolean binarizeVisible, boolean useBinarizeOptionGiven) {
        LinkedList<RBMSettingsController> rbmSettingsList = benchmarkModel.getRbmSettingsList();

        float[][] hiddenData = data;
        ListIterator<RBMSettingsController> rbmSettingsListIterator = rbmSettingsList.listIterator(rbmSettingsList.size());

        while (rbmSettingsListIterator.hasPrevious()) {
            RBMSettingsController rbmSettingsController = rbmSettingsListIterator.previous();
            float[][] visibleData;
            if (useBinarizeOptionGiven) {
                visibleData = this.getVisibleSingleRBM(rbmSettingsController, hiddenData, binarizeVisible);
            } else {
                visibleData = this.getVisibleSingleRBM(rbmSettingsController, hiddenData);
            }

            hiddenData = visibleData;
        }

        float[][] visibleDataFinal = hiddenData;
        return visibleDataFinal;
    }

    public float[][] getVisibleSingleRBM(RBMSettingsController controller, float[][] data, boolean binarizeVisible) {
        IRBM rbm = this.createRBMForTemporaryUse(controller);
        return rbm.getVisible(data, binarizeVisible);
    }

    public float[][] getVisibleSingleRBM(RBMSettingsController controller, float[][] data) {
        boolean binarizeVisible = controller.getModel().getController(RBMSettingsWeightsController.class).getModel().isBinarizeVisible();
        return this.getVisibleSingleRBM(controller, data, binarizeVisible);
    }

    public float[] getVisibleSingleRBM(RBMSettingsController controller, float[] data) {
        boolean binarizeVisible = controller.getModel().getController(RBMSettingsWeightsController.class).getModel().isBinarizeVisible();
        return this.getVisibleSingleRBM(controller, data, binarizeVisible);
    }

    public float[] getVisibleSingleRBM(RBMSettingsController controller, float[] data, boolean binarizeVisible) {
        float[][] data2Dimensions = vectorToMatrix(data);

        float[][] visibleData2Dimensions = getVisibleSingleRBM(controller, data2Dimensions, binarizeVisible);

        float visibleData[] = matrixToVector(visibleData2Dimensions);

        return visibleData;
    }

    // DAYDREAM
    public float[] daydreamAllRBMs(BenchmarkModel benchmarkModel, float[] data, boolean binarizeHidden, boolean binarizeVisible) {
        float[][] data2Dimensions = vectorToMatrix(data);

        float[][] hiddenData2Dimensions = this.getHiddenAllRBMs(benchmarkModel, data2Dimensions, binarizeHidden);
        float[][] visibleData2Dimensions = this.getVisibleAllRBMs(benchmarkModel, hiddenData2Dimensions, binarizeVisible);

        float visibleData[] = matrixToVector(visibleData2Dimensions);

        return visibleData;
    }

    public float[] daydreamSingleRBM(RBMSettingsController controller, float[] data, boolean binarizeHidden, boolean binarizeVisible) {

        float[] hiddenData = this.getHiddenSingleRBM(controller, data, binarizeHidden);
        float[] visibleData = this.getVisibleSingleRBM(controller, hiddenData, binarizeVisible);

        return visibleData;
    }

    private float[] matrixToVector(float[][] matrix) {
        float vector[] = new float[matrix[0].length];
        for (int i = 0; i < matrix[0].length; i++) {
            vector[i] = matrix[0][i];
        }
        return vector;
    }

    private float[][] vectorToMatrix(float[] vector) {
        float[][] matrix = new float[1][vector.length];
        for (int i = 0; i < vector.length; i++) {
            matrix[0][i] = vector[i];
        }
        return matrix;
    }

    public void updateRBMs(BenchmarkModel benchmarkModel) {

        float[][] data = benchmarkModel.getInputData();
        int inputSize = benchmarkModel.getOutputSize();
        
        LinkedList<RBMSettingsController> rbmSettingsList = benchmarkModel.getRbmSettingsList();
        for (RBMSettingsController settingsController : rbmSettingsList) {
            RBMSettingsModel settingsModel = settingsController.getModel();
            RBMSettingsMainModel mainModel = settingsModel.getController(RBMSettingsMainController.class).getModel();
            RBMSettingsWeightsModel weightsModel = settingsModel.getController(RBMSettingsWeightsController.class).getModel();
            mainModel.setInputSize(inputSize);
            settingsModel.setData(data);
            float[][] weights = weightsModel.getWeights();
            if(weights != null){
                if(weights.length != mainModel.getInputSize() + 1 || weights[0].length != mainModel.getOutputSize() + 1){
                    weightsModel.setWeights(null);
                    System.out.println("ATTENTION: Weights have been reset to null, because array dimensions do not fit input or output size!");
                }
            }
            data = getHiddenSingleRBM(settingsController, data);
            inputSize = mainModel.getOutputSize();
        }
    }

    public void cancelTraining() {
        if(task != null) {
            task.cancel();
        }
    }

}
