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
public class FaceRepairModel {

    private final FaceRepairController controller;
    private final String exportDirectory = "export";

    private int minEdgeSize = 600;
    private boolean binarize = false;

    private BufferedImage image;

    public FaceRepairModel(FaceRepairController controller) {
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
            Logger.getLogger(FaceRepairModel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        int[] scaledImageSize = getScaledImageSize(originalImage);

        BufferedImage scaledImage = StaticImageHelper.getScaledImage(originalImage, scaledImageSize[0], scaledImageSize[1]);

        this.image = scaledImage;

        drawImage();
    }
    
    private int[] getScaledImageSize(BufferedImage image){
        
        int scaleWidth = this.minEdgeSize;
        int scaleHeight = this.minEdgeSize;
        
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        if (image.getHeight() < image.getWidth()) {
            scaleWidth = (int) (scaleHeight * ((float) originalWidth / originalHeight));
        } else {
            scaleHeight = (int) (scaleWidth * ((float) originalHeight / originalWidth));
        }
        
        return new int[]{scaleWidth, scaleHeight};
    }

    private void drawImage() {
        if (this.image == null) {
            return;
        }

        WritableImage fxImage = SwingFXUtils.toFXImage(this.image, null);

        ImageView imgView = this.controller.getImageView();
        imgView.setImage(fxImage);
        
        int width = this.image.getWidth();
        int height = this.image.getHeight();
        
        System.out.println(imgView.getViewport().getWidth());
        System.out.println(imgView.getViewport().getHeight());
    }

    public void repairImage() {
        if (this.image == null) {
            return;
        }

        BenchmarkModel benchmarkModel = controller.getBenchmarkModel();
        int imageEdgeSize = benchmarkModel.getImageEdgeSize();

        int xShifts = this.image.getWidth() / imageEdgeSize;
        int yShifts = this.image.getHeight() / imageEdgeSize;
        
        

        drawImage();
    }

    public void exportImage() {
        if (this.image == null) {
            return;
        }
        String date = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        File outputfile = new File(this.exportDirectory + "/facerepair_" + date + ".png");
        try {
            ImageIO.write(this.image, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(FaceRepairModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
