package berlin.iconn.rbm.rbm.cuda;


import org.jblas.FloatMatrix;

/**
 * DataConverter
 *
 * @author Radek
 */
public class RBMSettings {

    private int maxepoch;

    private float epsilonw;
    private float epsilonvb;
    private float epsilonhb;
    private float weightcost;
    private float initialmomentum;
    private float finalmomentum;

    private int numhid;

    private int numcases;
    private int numdims;
    private int numbatches;
    
    private FloatMatrix vishid;
    private FloatMatrix hidbiases;
    private FloatMatrix visbiases;

    public RBMSettings() {
    }

    public int getMaxepoch() {
        return maxepoch;
    }

    public void setMaxepoch(int maxepoch) {
        this.maxepoch = maxepoch;
    }

    public float getEpsilonw() {
        return epsilonw;
    }

    public void setEpsilonw(float epsilonw) {
        this.epsilonw = epsilonw;
    }

    public float getEpsilonvb() {
        return epsilonvb;
    }

    public void setEpsilonvb(float epsilonvb) {
        this.epsilonvb = epsilonvb;
    }

    public float getEpsilonhb() {
        return epsilonhb;
    }

    public void setEpsilonhb(float epsilonhb) {
        this.epsilonhb = epsilonhb;
    }

    public float getWeightcost() {
        return weightcost;
    }

    public void setWeightcost(float weightcost) {
        this.weightcost = weightcost;
    }

    public float getInitialmomentum() {
        return initialmomentum;
    }

    public void setInitialmomentum(float initialmomentum) {
        this.initialmomentum = initialmomentum;
    }

    public float getFinalmomentum() {
        return finalmomentum;
    }

    public void setFinalmomentum(float finalmomentum) {
        this.finalmomentum = finalmomentum;
    }

    public int getNumhid() {
        return numhid;
    }

    public void setNumhid(int numhid) {
        this.numhid = numhid;
    }

    public int getNumcases() {
        return numcases;
    }

    public void setNumcases(int numcases) {
        this.numcases = numcases;
    }

    public int getNumdims() {
        return numdims;
    }

    public void setNumdims(int numdims) {
        this.numdims = numdims;
    }

    public int getNumbatches() {
        return numbatches;
    }

    public void setNumbatches(int numbatches) {
        this.numbatches = numbatches;
    }

    public FloatMatrix getVishid() {
        return vishid;
    }

    public void setVishid(FloatMatrix vishid) {
        this.vishid = vishid;
    }

    public FloatMatrix getHidbiases() {
        return hidbiases;
    }

    public void setHidbiases(FloatMatrix hidbiases) {
        this.hidbiases = hidbiases;
    }

    public FloatMatrix getVisbiases() {
        return visbiases;
    }

    public void setVisbiases(FloatMatrix visbiases) {
        this.visbiases = visbiases;
    }
    
    

}
