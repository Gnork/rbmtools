/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.rbm;

import java.util.Random;

/**
 *
 * deprecated class implementing FloatMatrix functionality of OpenBlas in Java
 */
public final class FloatMatrix {

    private float[][] m;

    public enum Bias {

        NONE, COLUMN_ONLY, BOTH, ROW_ONLY
    }
    
    public FloatMatrix(int rows, int columns, boolean useSeed, int seed){
        Random random;
        if(useSeed) random = new Random(seed);
        else random = new Random();
        this.m = new float[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                m[i][j] = (float) random.nextGaussian() * 0.01f;
            }
        }
    }
    
    public FloatMatrix(int rows, int columns){
        this.m = new float[rows][columns];
    }

    public FloatMatrix(float[][] m) {
        this(m, Bias.NONE);
    }

    public FloatMatrix(float[][] m, Bias bias) {
        if (bias == Bias.COLUMN_ONLY) {
            this.m = new float[m.length][m[0].length + 1];
            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m[0].length; j++) {
                    this.m[i][j + 1] = m[i][j];
                }
            }
            
            setFirstColumnOne();
            
        } else if(bias == Bias.ROW_ONLY) {
            this.m = new float[m.length + 1][m[0].length];
            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m[0].length; j++) {
                    this.m[i + 1][j] = m[i][j];
                }
            }
            
            setFirstRowOne();
            
        } else if(bias == Bias.BOTH) {
        this.m = new float[m.length + 1][m[0].length + 1];
            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m[0].length; j++) {
                    this.m[i + 1][j + 1] = m[i][j];
                }
            }
            
            setFirstColumnOne();
            setFirstRowOne();
            
        } else {
            this.m = m;
        }
    }

    public FloatMatrix(float[][] m, boolean biasColumn, boolean biasRow) {
        this.m = new float[m.length][m[0].length + 1];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                this.m[i][j + 1] = m[i][j];
            }
        }
        setFirstColumnOne();
    }

    public void mmuli(FloatMatrix other, FloatMatrix result) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < other.m[0].length; ++j) {
                result.m[i][j] = 0;
                for (int k = 0; k < m[0].length; ++k) {
                    result.m[i][j] += m[i][k] * other.m[k][j];
                }
            }
        }
    }

    public void transposei(FloatMatrix result) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; j++) {
                result.m[j][i] = m[i][j];
            }
        }
    }
    
    public void gti(FloatMatrix other){
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                if(m[i][j] > other.m[i][j]) m[i][j] = 1.f;
                else m[i][j] = 0.f;
            }
        }
    }
    
    public void subi(FloatMatrix other){
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] -= other.m[i][j];
            }
        }
    }
    
    public void addi(FloatMatrix other){
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] -= other.m[i][j];
            }
        }
    }
    
    public void muli(float f){
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] *= f;
            }
        }
    }

    public void setFirstColumnOne() {
        for (int i = 0; i < m.length; i++) {
            m[i][0] = 1.0f;
        }
    }
    public void setFirstRowOne() {
        for (int i = 0; i < m[0].length; i++) {
            m[0][i] = 1.0f;
        }
    }
    
    public int getRows(){
        return m.length;
    }
    
    public int getColumns(){
        return m[0].length;
    }

    public float[][] getData() {
        return m;
    }

}
