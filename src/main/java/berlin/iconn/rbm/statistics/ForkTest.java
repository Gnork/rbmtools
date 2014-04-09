package berlin.iconn.rbm.statistics;

import gnu.trove.map.hash.TIntDoubleHashMap;

import java.util.concurrent.RecursiveAction;

import berlin.iconn.rbm.image.Pic;

public class ForkTest extends RecursiveAction {

    private static final long serialVersionUID = 7409860587315610289L;
    protected static int sThreshold = 100;

    private final Pic[] images;
    private final int start;
    private final int length;
    private final float[] MAPs;
    private final PrecisionRecallTester mAPTester;
    private final TIntDoubleHashMap lookup;

    public ForkTest(PrecisionRecallTester mAPTester, TIntDoubleHashMap lookup, Pic[] images, int start, int length, float[] MAPs) {
        this.images = images;
        this.start = start;
        this.length = length;
        this.MAPs = MAPs;
        this.mAPTester = mAPTester;
        this.lookup = lookup;
    }

    protected void computeDirectly() {
        for (int index = start; index < start + length; index++) {
            Pic image = images[index];
            float map = mAPTester.test(image, index, lookup);
            MAPs[index] = map;
        }
    }

    @Override
    protected void compute() {
        if (length < sThreshold) {
            computeDirectly();
            return;
        }

        int split = length / 2;

        invokeAll(new ForkTest(mAPTester, lookup, images, start, split, MAPs),
                new ForkTest(mAPTester, lookup, images, start + split, length - split, MAPs));
    }
}
