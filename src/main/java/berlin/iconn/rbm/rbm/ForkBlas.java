/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.rbm;

import org.jblas.FloatMatrix;
import org.jblas.ranges.IntervalRange;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by Moritz on 1/14/14.
 */
public class ForkBlas {
    private final static ForkJoinPool pool = new ForkJoinPool();

    public void pmmuli(FloatMatrix self, FloatMatrix other, FloatMatrix result) {
        pool.invoke(new MulitplyPartly(self, other, result, 0, self.getRows(), 0, other.getColumns()));
    }

    private final class MulitplyPartly extends RecursiveAction {
        final static int minSize = 40;
        final FloatMatrix a;
        final FloatMatrix b;
        final FloatMatrix result;
        final int aStart;
        final int aEnd;
        final int bStart;
        final int bEnd;

        public MulitplyPartly(FloatMatrix a, FloatMatrix b, FloatMatrix result, int aStart, int aEnd, int bStart, int bEnd) {
            this.a = a;
            this.b = b;
            this.result = result;
            this.aStart = aStart;
            this.aEnd = aEnd;
            this.bStart = bStart;
            this.bEnd = bEnd;

        }

        @Override
        protected void compute() {

            if(aEnd - aStart > minSize && bEnd - bStart > minSize) {
                final int aMiddle = aStart + (aEnd - aStart) / 2;
                final int bMiddle = bStart + (bEnd - bStart) / 2;

                invokeAll(
                        new MulitplyPartly(a, b, result, aStart, aMiddle, bStart, bMiddle),
                        new MulitplyPartly(a, b, result, aMiddle, aEnd, bMiddle, bEnd),
                        new MulitplyPartly(a, b, result, aStart, aMiddle, bMiddle, bEnd),
                        new MulitplyPartly(a, b, result, aMiddle, aEnd, bStart, bMiddle));
            } else {
                    FloatMatrix x = a.get(new IntervalRange(aStart, aEnd), new IntervalRange(0, a.columns)).mmul(
                            b.get(new IntervalRange(0, b.rows), new IntervalRange(bStart, bEnd)));
                    result.put(x, aStart, bStart);
            }

        }

    }
}

