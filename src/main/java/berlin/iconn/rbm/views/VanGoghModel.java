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
    
    private final int minEdgeSize = 600;
    private BufferedImage image;
    
    public VanGoghModel(VanGoghController controller){
        this.controller = controller;
    }
    
    public void loadImageFile(){
        File imageFile = Chooser.openFileChooser("images");
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
        
        if(originalImage.getHeight() < originalImage.getWidth()){
            scaleWidth = (int)(scaleHeight * ((float)originalWidth / originalHeight));
        }else{
            scaleHeight = (int)(scaleWidth * ((float)originalHeight / originalWidth)); 
        }
        
        BufferedImage scaledImage = StaticImageHelper.getScaledImage(originalImage, scaleWidth, scaleHeight);
        scaledImage = scaleToFitPatchSize(scaledImage);
    
        this.image = scaledImage;
        
        drawImage();      
    }
    
    private BufferedImage scaleToFitPatchSize(BufferedImage originalImage){
        int imageEdgeSize = controller.getBenchmarkModel().getImageEdgeSize();
        System.out.println(originalImage.getWidth() + ", " + originalImage.getHeight());
        if(originalImage.getWidth() % imageEdgeSize == 0 && originalImage.getHeight() % imageEdgeSize == 0){
            return originalImage;
        }
        
        int scaleWidth = originalImage.getWidth() - (originalImage.getWidth() % imageEdgeSize);
        int scaleHeight = originalImage.getHeight() - (originalImage.getHeight() % imageEdgeSize);
        
        System.out.println(scaleWidth + ", " + scaleHeight);
        
        return StaticImageHelper.getScaledImage(originalImage, scaleWidth, scaleHeight);
    }
    
    private void drawImage(){
        if(this.image == null){
            return;
        }
        
        WritableImage fxImage = SwingFXUtils.toFXImage(this.image, null);
        
        ImageView imgView = this.controller.getImageView();
        imgView.setImage(fxImage);
    }
    
    public void generateImage(){
        if(this.image == null){
            return;
        }
        
        BenchmarkModel benchmarkModel = controller.getBenchmarkModel();
        int imageEdgeSize = benchmarkModel.getImageEdgeSize();       
        
        float xShifts = this.image.getWidth() / (float)imageEdgeSize;
        float yShifts = this.image.getHeight() / (float)imageEdgeSize;
        
        RBMTrainer trainer = new RBMTrainer();
        
        for(int j = 0; j < yShifts; ++j){
            for(int i = 0; i < xShifts; ++i){
                int startX = i * imageEdgeSize;
                int startY = j * imageEdgeSize;
                
                BufferedImage subImage = this.image.getSubimage(startX, startY, imageEdgeSize, imageEdgeSize);
                float[] processedData = DataConverter.processPixelData(subImage, imageEdgeSize, imageEdgeSize, benchmarkModel.isBinarizeImages(), benchmarkModel.isInvertImages(), benchmarkModel.getMinData(), benchmarkModel.getMaxData(), benchmarkModel.isRgb());
                float[] hidden = trainer.getHiddenAllRBMs1D(benchmarkModel, processedData, true);
                float[] visible = trainer.getVisibleAllRBMs1D(benchmarkModel, hidden, false);
                BufferedImage reconSubImage = DataConverter.pixelDataToImage(visible, benchmarkModel.getMinData(), benchmarkModel.isRgb(), imageEdgeSize, imageEdgeSize);
                int[] reconSubRGB = reconSubImage.getRGB(0, 0, imageEdgeSize, imageEdgeSize, null, 0, imageEdgeSize);
                this.image.setRGB(startX, startY, imageEdgeSize, imageEdgeSize, reconSubRGB, 0, imageEdgeSize);
            }
        }
        
        drawImage();
    }
}
