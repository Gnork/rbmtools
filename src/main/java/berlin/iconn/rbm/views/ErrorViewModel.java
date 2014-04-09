package berlin.iconn.rbm.views;

import berlin.iconn.rbm.enhancement.IVisualizeObserver;
import berlin.iconn.rbm.enhancement.RBMInfoPackage;
import java.util.LinkedList;
public class ErrorViewModel implements IVisualizeObserver {

    private final ErrorViewController controller;

    private final LinkedList<Float> errors;
    private final LinkedList<Integer> epochs;
    private int lastEpoch = 0;
    private float lastError = 0;

    public ErrorViewModel(ErrorViewController controller) {
        this.controller = controller;
        this.errors = new LinkedList<>();
        this.epochs = new LinkedList<>();
    }

    @Override
    public void update(RBMInfoPackage pack) {
        lastEpoch = pack.getEpochs();
        lastError = pack.getError();
        getErrors().add(getLastError());
        getEpochs().add(getLastEpoch());
        controller.update();
    }

    /**
     * @return the errors
     */
    public LinkedList<Float> getErrors() {
        return errors;
    }

    public void clear() {
        getEpochs().clear();
        errors.clear();
        controller.clear();
    }

    /**
     * @return the epochs
     */
    public LinkedList<Integer> getEpochs() {
        return epochs;
    }

    /**
     * @return the lastEpoch
     */
    public int getLastEpoch() {
        return lastEpoch;
    }

    /**
     * @return the lastError
     */
    public float getLastError() {
        return lastError;
    }

}
