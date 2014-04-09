package berlin.iconn.rbm.sorter;

import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;
import berlin.iconn.rbm.image.Pic;

public class SorterRBMFeatures extends ASorter {

    public SorterRBMFeatures(Pic[] images, ForkJoinPool pool) {
        super(images, pool);
    }


	///////////////////////////////////////////
    // visualize the feature data as image
    //
    @Override
    public BufferedImage getFeatureImage(Pic image) {

        float[] fv = image.getFeatureVector();
        int height = 256, width = fv.length;
        int[] pixels = new int[width * height];
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = y * width + x;
                float val = fv[x] * height;
                pixels[i] = (val > height - 1 - y) ? 0xFFFFFFFF : 0xFF777777;
            }
        }

        bi.setRGB(0, 0, width, height, pixels, 0, width);
        return bi;
    }

    @Override
    public float getDistance(float[] fv1, float[] fv2) {
        return getL1Dist(fv1, fv2);
    }

    @Override
    public String getName() {
        return "SorterRBMWrapper";
    }
}
