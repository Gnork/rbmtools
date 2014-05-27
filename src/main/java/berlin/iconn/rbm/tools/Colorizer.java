/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.tools;

import berlin.iconn.rbm.image.StaticImageHelper;
import ij.process.FloatProcessor;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * @author Radek
 */
public class Colorizer {

    private static final int NUMBER_OF_IMAGES_TO_LOAD = 2000000;
    private static final int NUMBER_OF_BEST_IMAGES = 8;
    private static final int EDGE_LENGTH = 32;
    private static final int PLANE_SIZE = EDGE_LENGTH * EDGE_LENGTH;

    private static final String INPUT_IMAGES_PATH = "/Users/Radek/git/rbmtools/images/tiny_images.bin";

    private static final String INPUT_IMAGE_PATH = "/Users/Radek/git/rbmtools/images/ColorizerTestImages/test2.jpg";
    private static final String OUTPUT_IMAGE_PATH = "/Users/Radek/git/rbmtools/images/tiny_images/";

    //private static final double[][] imagesLuminance = new double[NUMBER_OF_IMAGES_TO_LOAD][];
    private static final int maxAllowedColors = 64;

    public static void main(String[] args) {
        Colorizer colorizer = new Colorizer();
        //colorizer.loadData();
        colorizer.colorize(INPUT_IMAGE_PATH);
    }

    private void loadData() {
        // TODO Auto-generated method stub

        try (FileInputStream inputStream = new FileInputStream(INPUT_IMAGES_PATH)) {

            byte[] buffer = new byte[PLANE_SIZE];

            for (int i = 0; i < NUMBER_OF_IMAGES_TO_LOAD; i++) {

                int[] pixels = new int[PLANE_SIZE];

                inputStream.read(buffer);
                for (int c = 0; c < PLANE_SIZE; c++) {
                    int r = buffer[c] & 0xff;
                    pixels[c] = 0xff000000 | (r << 16);
                }

                inputStream.read(buffer);
                for (int c = 0; c < PLANE_SIZE; c++) {
                    int g = buffer[c] & 0xff;
                    pixels[c] |= g << 8;
                }

                inputStream.read(buffer);
                for (int c = 0; c < PLANE_SIZE; c++) {
                    int b = buffer[c] & 0xff;
                    pixels[c] |= b;
                }

                HashSet<Integer> colors = new HashSet<>();
                int colorCount = 0;

                int[] pixelsRotated = new int[pixels.length];
                for (int y = 0; y < EDGE_LENGTH; y++) {
                    for (int x = 0; x < EDGE_LENGTH; x++) {
                        int color = pixels[x * EDGE_LENGTH + y];

                        if (!colors.contains(color)) {
                            colors.add(color);
                            colorCount++;
                        }

                        pixelsRotated[y * EDGE_LENGTH + x] = color;
                    }
                }

                if (colorCount < maxAllowedColors) {
                    System.out.println("Image has NOT enough colors");
                } else {
                    System.out.println("Image has enough colors");
                }

                //BufferedImage image = new BufferedImage(EDGE_LENGTH, EDGE_LENGTH, BufferedImage.TYPE_INT_RGB);
                //image.setRGB(0, 0, EDGE_LENGTH, EDGE_LENGTH, pixelsRotated, 0, EDGE_LENGTH);

                //File outputfile = new File(OUTPUT_IMAGE_PATH + "image_" + i + ".png");
                //ImageIO.write(image, "png", outputfile);

                //imagesLuminance[i] = extractLuminance(pixelsRotated);

            }
        } catch (IOException ex) {
            Logger.getLogger(Colorizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BufferedImage loadTinyImage(int index) {
        BufferedImage image = new BufferedImage(EDGE_LENGTH, EDGE_LENGTH, BufferedImage.TYPE_INT_RGB);

        try (FileInputStream inputStream = new FileInputStream(INPUT_IMAGES_PATH)) {
            int[] pixels = new int[PLANE_SIZE];

            long idx = (long) index;
            long offset = 3L * idx * PLANE_SIZE;
            byte[] buffer = new byte[PLANE_SIZE];

            inputStream.skip(offset);

            inputStream.read(buffer);
            for (int c = 0; c < PLANE_SIZE; c++) {
                int r = buffer[c] & 0xff;
                pixels[c] = 0xff000000 | (r << 16);
            }

            inputStream.read(buffer);
            for (int c = 0; c < PLANE_SIZE; c++) {
                int g = buffer[c] & 0xff;
                pixels[c] |= g << 8;
            }

            inputStream.read(buffer);
            for (int c = 0; c < PLANE_SIZE; c++) {
                int b = buffer[c] & 0xff;
                pixels[c] |= b;
            }

            int[] pixelsRotated = new int[pixels.length];
            for (int y = 0; y < EDGE_LENGTH; y++) {
                for (int x = 0; x < EDGE_LENGTH; x++) {
                    pixelsRotated[y * EDGE_LENGTH + x] = pixels[x * EDGE_LENGTH + y];
                }
            }

            image.setRGB(0, 0, EDGE_LENGTH, EDGE_LENGTH, pixelsRotated, 0, EDGE_LENGTH);

        } catch (IOException ex) {
            Logger.getLogger(Colorizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return image;
    }

    private void colorize(String path) {

        BufferedImage inputImage = null;

        double[] luminanceInputImage = null;
        double[] luminanceInputImageTiny = null;

        // load image
        try {
            File imageFile = new File(path);
            inputImage = ImageIO.read(imageFile);
        } catch (IOException ex) {
            Logger.getLogger(Colorizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // extract luminance of image
        int[] pixels = inputImage.getRGB(0, 0, inputImage.getWidth(), inputImage.getHeight(), null, 0, inputImage.getWidth());
        luminanceInputImage = extractLuminance(pixels);

        // scale luminance channel to the edge length of the tiny images
        BufferedImage inputImageTiny = StaticImageHelper.getScaledImage(inputImage, EDGE_LENGTH, EDGE_LENGTH);
        int[] inputPixelsTiny = inputImageTiny.getRGB(0, 0, inputImageTiny.getWidth(), inputImageTiny.getHeight(), null, 0, inputImageTiny.getWidth());
        luminanceInputImageTiny = extractLuminance(inputPixelsTiny);

        // compare luminance with luminance of the tiny images, get the x nearest
        TreeMap<Double, Integer> sortedLuminance = new TreeMap<>();
        for (int i = 0; i < NUMBER_OF_IMAGES_TO_LOAD; i++) {
            double[] luminanceCurrentImageTiny = extractLuminance(loadTinyImage(i).getRGB(0, 0, EDGE_LENGTH, EDGE_LENGTH, null, 0, EDGE_LENGTH));

            double distance = getDistance(luminanceInputImageTiny, luminanceCurrentImageTiny);
            sortedLuminance.put(distance, i);

            if (sortedLuminance.size() > NUMBER_OF_BEST_IMAGES) {
                sortedLuminance.remove(sortedLuminance.lastEntry().getKey());
            }

            System.out.print("\r" + i);
        }

        List<Integer> sortedLuminanceIndicesList = new ArrayList(sortedLuminance.values());
        sortedLuminance = null;

        BufferedImage[] bestTinyImages = new BufferedImage[NUMBER_OF_BEST_IMAGES];
        for (int i = 0; i < NUMBER_OF_BEST_IMAGES; i++) {
            int index = sortedLuminanceIndicesList.get(i);
            BufferedImage tinyImage = loadTinyImage(index);
            bestTinyImages[i] = tinyImage;
        }
        sortedLuminanceIndicesList = null;

        for (int i = 0; i < NUMBER_OF_BEST_IMAGES; i++) {
            File outputfile = new File(OUTPUT_IMAGE_PATH + "image_" + i + ".png");
            try {
                ImageIO.write(bestTinyImages[i], "png", outputfile);
            } catch (IOException ex) {
                Logger.getLogger(Colorizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        // compute lab of this 16 tiny images
        // create average color (ab) (rgb optional) of this 16 tiny images
        double[] aSum = new double[PLANE_SIZE];
        double[] bSum = new double[PLANE_SIZE];

        for (int i = 0; i < NUMBER_OF_BEST_IMAGES; i++) {
            BufferedImage image = bestTinyImages[i];
            int[] pixelsImage = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

            for (int p = 0; p < pixelsImage.length; p++) {
                int pixel = pixelsImage[p];

                int r = ((pixel >> 16) & 0xFF);
                int g = ((pixel >> 8) & 0xFF);
                int b = ((pixel) & 0xFF);

                LAB lab = LAB.fromRGBr(r, g, b, 0);

                aSum[p] += lab.a;
                bSum[p] += lab.b;
            }
        }

        for (int p = 0; p < PLANE_SIZE; p++) {
            aSum[p] /= NUMBER_OF_BEST_IMAGES;
            bSum[p] /= NUMBER_OF_BEST_IMAGES;
        }


        // scale ab of avg to the dimensions of the input image
        FloatProcessor a = new FloatProcessor(EDGE_LENGTH, EDGE_LENGTH, aSum);
        float[][] aScaled = a.resize(inputImage.getWidth(), inputImage.getHeight()).getFloatArray();

        FloatProcessor b = new FloatProcessor(EDGE_LENGTH, EDGE_LENGTH, bSum);
        float[][] bScaled = b.resize(inputImage.getWidth(), inputImage.getHeight()).getFloatArray();

        // Combine L of input image with ab of avg
        int[] finalColor = new int[pixels.length];
        for (int y = 0; y < inputImage.getHeight(); y++) {
            for (int x = 0; x < inputImage.getWidth(); x++) {
                int pos = y * inputImage.getWidth() + x;

                double L = luminanceInputImage[pos];
                double A = aScaled[x][y];
                double B = bScaled[x][y];

                finalColor[pos] = new LAB(L, A, B).rgb();
            }
        }

        BufferedImage finalImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        finalImage.setRGB(0, 0, inputImage.getWidth(), inputImage.getHeight(), finalColor, 0, inputImage.getWidth());

        File finaloutputfile = new File(OUTPUT_IMAGE_PATH + "image_Final" + ".png");
        try {
            ImageIO.write(finalImage, "png", finaloutputfile);
        } catch (IOException ex) {
            Logger.getLogger(Colorizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private double[] extractLuminance(int[] pixels) {
        double[] luminance = new double[pixels.length];

        for (int p = 0; p < pixels.length; p++) {
            int argb = pixels[p];

            int r = ((argb >> 16) & 0xFF);
            int g = ((argb >> 8) & 0xFF);
            int b = ((argb) & 0xFF);

            LAB lab = LAB.fromRGBr(r, g, b, 0);

            luminance[p] = lab.L;
        }

        return luminance;
    }

    private double getDistance(double[] a, double[] b) {
        float distance = 0;

        for (int i = 0; i < a.length; i++) {
            distance += Math.abs(a[i] - b[i]);
        }

        return distance;
    }


}
