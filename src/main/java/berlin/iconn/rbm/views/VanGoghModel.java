/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.views;

import berlin.iconn.rbm.image.DataConverter;
import berlin.iconn.rbm.image.StaticImageHelper;
import berlin.iconn.rbm.main.BenchmarkModel;
import berlin.iconn.rbm.rbm.RBMTrainer;
import berlin.iconn.rbm.tools.Chooser;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/**
 *
 * @author christoph
 */
public class VanGoghModel {

    private final VanGoghController controller;
    private final String exportDirectory = "export";

    private int minEdgeSize = 600;
    private boolean binarize = false;

    private BufferedImage image;

    public VanGoghModel(VanGoghController controller) {
        this.controller = controller;
    }

    public int getMinEdgeSize() {
        return minEdgeSize;
    }

    public void setMinEdgeSize(int minEdgeSize) {
        this.minEdgeSize = minEdgeSize;
    }

    public boolean isBinarize() {
        return binarize;
    }

    public void setBinarize(boolean binarize) {
        this.binarize = binarize;
    }

    public void loadImageFile() {
        File imageFile = Chooser.openFileChooser("images");
        if (imageFile == null) {
            return;
        }
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(imageFile);
        } catch (IOException ex) {
            Logger.getLogger(VanGoghModel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        int scaleWidth = this.minEdgeSize;
        int scaleHeight = this.minEdgeSize;

        if (originalImage.getHeight() < originalImage.getWidth()) {
            scaleWidth = (int) (scaleHeight * ((float) originalWidth / originalHeight));
        } else {
            scaleHeight = (int) (scaleWidth * ((float) originalHeight / originalWidth));
        }

        BufferedImage scaledImage = StaticImageHelper.getScaledImage(originalImage, scaleWidth, scaleHeight);
        scaledImage = scaleToFitPatchSize(scaledImage);

        this.image = scaledImage;

        drawImage();
    }

    private BufferedImage scaleToFitPatchSize(BufferedImage originalImage) {
        int imageEdgeSize = controller.getBenchmarkModel().getImageEdgeSize();
        if (originalImage.getWidth() % imageEdgeSize == 0 && originalImage.getHeight() % imageEdgeSize == 0) {
            return originalImage;
        }

        int scaleWidth = originalImage.getWidth() - (originalImage.getWidth() % imageEdgeSize);
        int scaleHeight = originalImage.getHeight() - (originalImage.getHeight() % imageEdgeSize);

        System.out.println("Image: " + scaleWidth + ", " + scaleHeight);

        return StaticImageHelper.getScaledImage(originalImage, scaleWidth, scaleHeight);
    }

    private void drawImage() {
        if (this.image == null) {
            return;
        }

        WritableImage fxImage = SwingFXUtils.toFXImage(this.image, null);

        ImageView imgView = this.controller.getImageView();
        imgView.setImage(fxImage);
    }

    public void generateImage() {
        if (this.image == null) {
            return;
        }

        BenchmarkModel benchmarkModel = controller.getBenchmarkModel();
        int imageEdgeSize = benchmarkModel.getImageEdgeSize();

        int xShifts = this.image.getWidth() / imageEdgeSize;
        int yShifts = this.image.getHeight() / imageEdgeSize;
        
        ForkJoinPool pool = ForkJoinPool.commonPool();

        int numOfCores= Runtime.getRuntime().availableProcessors();
        System.out.println("Available Processors: " + numOfCores);
        pool.invoke(new VanGoghReconstruct(this.image, imageEdgeSize, 0, xShifts-1, 0, yShifts-1, 2, benchmarkModel, this.binarize));

        drawImage();
    }

    public void exportImage() {
        if (this.image == null) {
            return;
        }
        String date = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        File outputfile = new File(this.exportDirectory + "/vangogh_" + date + ".png");
        try {
            ImageIO.write(this.image, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(VanGoghModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class VanGoghReconstruct extends RecursiveAction {

        final BufferedImage image;
        final int patchSize;
        final int startX;
        final int endX;
        final int startY;
        final int endY;
        final int penalty;
        final BenchmarkModel benchmarkModel;
        final boolean binarize;

        public VanGoghReconstruct(BufferedImage image, int patchSize, int startX, int endX, int startY, int endY, int penalty, BenchmarkModel benchmarkModel, boolean binarize) {
            this.image = image;
            this.patchSize = patchSize;
            this.startX = startX;
            this.endX = endX;
            this.startY = startY;
            this.endY = endY;
            this.penalty = penalty;
            this.benchmarkModel = benchmarkModel;
            this.binarize = binarize;
            System.out.println("Fork: " + startX + ", " + endX + ", " + startY + ", " + endY + ", " + penalty);
        }

        @Override
        protected void compute() {
            if (penalty <= 1 || (startX == endX && startY == endY)) {
                this.make();
                System.out.println("Fork: finished");
            } else {
                int startX1 = startX;
                int endX1 = endX;
                int startY1 = startY;
                int endY1 = endY;
                int startX2 = startX;
                int endX2 = endX;
                int startY2 = startY;
                int endY2 = endY;
                int nextPenalty = penalty / 2;
                int diffX = endX - startX;
                int diffY = endY - startY;
                if (diffX < diffY) {
                    int halfDiffY = diffY / 2;
                    endY1 = startY + halfDiffY;
                    startY2 = endY1 + 1;
                } else {
                    int halfDiffX = diffX / 2;
                    endX1 = startX + halfDiffX;
                    startX2 = endX1 + 1;
                }
                invokeAll(
                        new VanGoghReconstruct(this.image, this.patchSize, startX1, endX1, startY1, endY1, nextPenalty, this.benchmarkModel, this.binarize),
                        new VanGoghReconstruct(this.image, this.patchSize, startX2, endX2, startY2, endY2, nextPenalty, this.benchmarkModel, this.binarize)
                );
            }
        }

        private void make() {
            RBMTrainer trainer = new RBMTrainer();
            for(int y = this.startY; y <= this.endY; ++y){
                for(int x = this.startX; x <= this.endX; ++x){
                    BufferedImage subImage = this.image.getSubimage(x * this.patchSize, y * this.patchSize, this.patchSize, this.patchSize);
                    float[] processedData = DataConverter.processPixelData(subImage, this.patchSize, this.patchSize, benchmarkModel.isBinarizeImages(), benchmarkModel.isInvertImages(), benchmarkModel.getMinData(), benchmarkModel.getMaxData(), benchmarkModel.isRgb());
                    float[] hidden = trainer.getHiddenAllRBMs1D(benchmarkModel, processedData, this.binarize);
                    float[] visible = trainer.getVisibleAllRBMs1D(benchmarkModel, hidden, false);
                    BufferedImage reconSubImage = DataConverter.pixelDataToImage(visible, benchmarkModel.getMinData(), benchmarkModel.isRgb(), this.patchSize, this.patchSize);
                    int[] reconSubRGB = reconSubImage.getRGB(0, 0, this.patchSize, this.patchSize, null, 0, this.patchSize);
                    this.image.setRGB(x * this.patchSize, y * this.patchSize, this.patchSize, this.patchSize, reconSubRGB, 0, this.patchSize);
                }
            }
        }
    }
}
