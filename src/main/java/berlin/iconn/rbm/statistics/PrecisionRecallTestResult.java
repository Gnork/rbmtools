/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.statistics;

/**
 *
 * @author Moritz
 */
public class PrecisionRecallTestResult {
    private final double mAP;
    private final String name;
    private final String testGroup;
    private final float[][] prTable;

    PrecisionRecallTestResult(double mAP, String name, String testGroup, float[][] prTable) {
        this.mAP = mAP;
        this.name = name;
        this.testGroup = testGroup;
        this.prTable = prTable;
    }

    /**
     * @return the mAP
     */
    public double getmAP() {
        return mAP;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the testGroup
     */
    public String getTestGroup() {
        return testGroup;
    }

    /**
     * @return the prTable
     */
    public float[][] getPrTable() {
        return prTable;
    }
    
}
