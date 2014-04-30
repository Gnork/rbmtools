package berlin.iconn.rbm.views;

import berlin.iconn.rbm.enhancement.IVisualizeObserver;
import berlin.iconn.rbm.enhancement.RBMInfoPackage;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public final class WeightsVisualizationModel implements IVisualizeObserver {

    private final WeightsVisualizationController controller;

    private int weightsWidth = 10;
    private int weightsHeight = 10;
    private int viewWidth = 400;
    private int viewHeight = 300;

    private int[] picWeights;

    private WritableImage image;

    public WeightsVisualizationModel(WeightsVisualizationController controller,
            int width, int height) {

        this.viewWidth = width;
        this.viewHeight = height;
        clear();
        this.controller = controller;
    }

    public int[] resizePixels(int[] pixels, int w1, int h1, int w2, int h2) {

        int[] temp = new int[w2 * h2];
        double x_ratio = w1 / (double) w2;
        double y_ratio = h1 / (double) h2;
        double px, py;

        for (int i = 0; i < h2; i++) {
            for (int j = 0; j < w2; j++) {
                px = Math.floor(j * x_ratio);
                py = Math.floor(i * y_ratio);
                temp[(i * w2) + j] = pixels[(int) ((py * w1) + px)];
            }
        }

        return temp;
    }

    public int getIntFromColor(int Red, int Green, int Blue) {

        Red = (Red << 16) & 0x00FF0000; 
        Green = (Green << 8) & 0x0000FF00; 
        Blue = Blue & 0x000000FF; 

        return 0xFF000000 | Red | Green | Blue;
    }

    public int[] generateImage(float[][] weights) {
        int[] pixels;
        int input;
        int output;
        input = weights.length;
        output = weights[0].length;

        pixels = new int[input * output];

        float start = -10;
        float end = 10;


        //float[][] rWeights = relativateWeights(weights);

        float rc, gc, bc;

        for (int y = 0; y < output; y++) {
            for (int x = 0; x < input; x++) {

                int pos = y * input + x;

                float current = weights[x][y];
                if (current > 0) {
                    rc = 1.0f - current;
                    gc = 1.0f;
                    bc = 1.0f - current;
                } else {
                    current = 1 + current;
                    rc = 1.0f;
                    gc = current;
                    bc = current;
                }
                rc *= 255;
                gc *= 255;
                bc *= 255;
                pixels[pos] = getIntFromColor((int) rc, (int) gc, (int) bc);

            }
        }
        return pixels;
    }

    private float[][] relativateWeights(float[][] weights) {
//        final float[][] result = new float[weights.length][weights[0].length];
//        double max = 0;
//        for (int i = 0; i < result.length; i++) {
//            for (int j = 0; j < result[0].length; j++) {
//                final double currentAbs = Math.abs(weights[i][j]);
//                if (currentAbs > max) {
//                    max = currentAbs;
//                }
//            }
//        }
//        
//        for (int i = 0; i < result.length; i++) {
//            for (int j = 0; j < result[0].length; j++) {
//                result[i][j] = Math.max(-1, Math.min(1, (weights[i][j] / 5.0f)));
//            }
//        }
//        return result;
        final float[][] result = new float[weights.length][weights[0].length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                    result[i][j] = weights[i][j] * 0.2f;
                    if(result[i][j] > 1) 
                        result[i][j] = 1;
                    if(result[i][j] < -1) 
                        result[i][j] = -1;
                }
            }
        return result;
    }

    @Override
    public void update(RBMInfoPackage pack) {
        if (pack != null) {
            float[][] weights = pack.getWeights();
            this.weightsWidth = weights.length;
            this.weightsHeight = weights[0].length;

            picWeights = generateImage(relativateWeights(pack.getWeights()));

        }
        WritableImage newImage = new WritableImage(this.viewWidth, this.viewHeight);
        int[] resizedWeights = resizePixels(
                picWeights,
                weightsWidth, weightsHeight,
                viewWidth, viewHeight);
        PixelWriter writer = newImage.getPixelWriter();
        for (int y = 0; y < this.viewHeight; y++) {
            for (int x = 0; x < this.viewWidth; x++) {
                writer.setArgb(x, y, resizedWeights[y * this.viewWidth + x]);
            }
        }
        this.image = newImage;

        controller.update();
    }

    public final void clear() {
        this.image = new WritableImage(viewWidth, viewHeight);
        this.picWeights = new int[getWeightsWidth() * getWeightsWidth()];
    }

    /**
     * @return the image
     */
    public WritableImage getImage() {
        return image;
    }

    /**
     * @param viewHeight the viewHeight to set
     */
    public void setViewHeight(int viewHeight) {
        this.viewHeight = viewHeight;
        update(null);
    }

    /**
     * @param viewWidth the viewWidth to set
     */
    public void setViewWidth(int viewWidth) {
        this.viewWidth = viewWidth;
        update(null);
    }

    /**
     * @return the weightsHeight
     */
    public int getWeightsHeight() {
        return weightsHeight;
    }

    /**
     * @return the weightsWidth
     */
    public int getWeightsWidth() {
        return weightsWidth;
    }

}
