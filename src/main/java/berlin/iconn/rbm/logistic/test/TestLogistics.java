package berlin.iconn.rbm.logistic.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import berlin.iconn.rbm.logistic.HardClipMatrixFunction;
import berlin.iconn.rbm.logistic.GaussMatrixFunction;
import berlin.iconn.rbm.logistic.RectifierMatrixFunction;
import berlin.iconn.rbm.logistic.DefaultLogisticMatrixFunction;
import berlin.iconn.rbm.logistic.TanHMatrixFunction;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;
import org.jblas.FloatMatrix;

/**
 *
 * @author Cristea
 */
public class TestLogistics extends TestCase {

    public TestLogistics(String testName) {
        super(testName);
    }

    public TestLogistics() {
    }

    float[][] testArray = new float[][]{{0, 0, 0}, {0.5f, 0.5f, 0.5f}, {1, 1, 1}};

    float[][] testArrayHardClipped = new float[][]{{2, 2, 2}, {-1, -1, -1}, {1, 1, 1}};

    float delta = 0.000001f;

    FloatMatrix testFloatMatrix = new FloatMatrix(testArray);
    FloatMatrix testFloatMatrixHard = new FloatMatrix(testArrayHardClipped);

    public void testDefaultLogistic() {

        float[][] expectMatrix = new float[][]{
            {0.5f, 0.5f, 0.5f},
            {0.622459f, 0.622459f, 0.622459f},
            {0.731059f, 0.731059f, 0.731059f}};

        System.out.println("* testDefaultLogistic()");
        DefaultLogisticMatrixFunction x = new DefaultLogisticMatrixFunction();
        FloatMatrix result = x.function(testFloatMatrix);

        float[][] resultArray = result.toArray2();

        //System.out.println(result);
        for (int i = 0; i < expectMatrix.length; i++) {
            for (int j = 0; j < expectMatrix.length; j++) {
                assertEquals(expectMatrix[i][j], resultArray[i][j], delta);
            }
        }
    }

    public void testTanhLogistic() {

        float[][] expectMatrix = new float[][]{
            {0.5f, 0.5f, 0.5f},
            {0.731059f, 0.731059f, 0.731059f},
            {0.880797f, 0.880797f, 0.880797f}};

        System.out.println("* testTanhLogistic");
        TanHMatrixFunction x = new TanHMatrixFunction();
        FloatMatrix result = x.function(testFloatMatrix);

        float[][] resultArray = result.toArray2();

        //System.out.println(result);
        for (int i = 0; i < expectMatrix.length; i++) {
            for (int j = 0; j < expectMatrix.length; j++) {
                assertEquals(expectMatrix[i][j], resultArray[i][j], delta);
            }
        }
    }

    public void testGaussLogistic() {

        float[][] expectMatrix = new float[][]{
            {1.0f, 1.0f, 1.0f},
            {0.778801f, 0.778801f, 0.778801f},
            {0.367879f, 0.367879f, 0.367879f}};

        System.out.println("* testGaussLogistic");
        GaussMatrixFunction x = new GaussMatrixFunction();
        FloatMatrix result = x.function(testFloatMatrix);

        float[][] resultArray = result.toArray2();

        //System.out.println(result);
        for (int i = 0; i < expectMatrix.length; i++) {
            for (int j = 0; j < expectMatrix.length; j++) {
                assertEquals(expectMatrix[i][j], resultArray[i][j], delta);
            }
        }
    }

    public void testHardClippedLogistic() {

        float[][] expectMatrix = new float[][]{
            {1, 1, 1},
            {0, 0, 0},
            {1, 1, 1}};

        System.out.println("* testHardClippedLogistic");
        HardClipMatrixFunction x = new HardClipMatrixFunction();
        FloatMatrix result = x.function(testFloatMatrixHard);

        float[][] resultArray = result.toArray2();

        //System.out.println(result);
        for (int i = 0; i < expectMatrix.length; i++) {
            for (int j = 0; j < expectMatrix.length; j++) {
                assertEquals(expectMatrix[i][j], resultArray[i][j], delta);
            }
        }
    }

    public void testRectifierLogistic() {

        float[][] expectMatrix = new float[][]{
            {0.693147f, 0.693147f, 0.693147f},
            {0.974077f, 0.974077f, 0.974077f},
            {1.313262f, 1.313262f, 1.313262f}};

        System.out.println("* testRectifierLogistic");
        RectifierMatrixFunction x = new RectifierMatrixFunction();
        FloatMatrix result = x.function(testFloatMatrix);

        float[][] resultArray = result.toArray2();

        //System.out.println(result);
        for (int i = 0; i < expectMatrix.length; i++) {
            for (int j = 0; j < expectMatrix.length; j++) {
                assertEquals(expectMatrix[i][j], resultArray[i][j], delta);
            }
        }
    }

}
