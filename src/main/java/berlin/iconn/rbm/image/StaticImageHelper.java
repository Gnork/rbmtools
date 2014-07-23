/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.image;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 *
 * @author christoph
 */
public class StaticImageHelper {
    
    /** image scale function to replace ImageScaler class
     * 
     * @param image
     * @param width
     * @param height
     * @return 
     */
    public static BufferedImage getScaledImage(BufferedImage image, int width, int height){
        
        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage scaledBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = scaledBufferedImage.getGraphics();
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();
        return scaledBufferedImage;
        
    }
    
}
