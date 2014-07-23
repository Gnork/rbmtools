package berlin.iconn.number_reconstructor;

/**
 * Created by G on 12.07.14.
 */
/**
 * Basic concept by
 *
 * @author John M (http://sourceforge.net/users/nextdesign)
 *
 */
import berlin.iconn.rbm.image.DataConverter;
import berlin.iconn.rbm.image.ImageScaler;
import berlin.iconn.rbm.main.BenchmarkModel;
import berlin.iconn.rbm.rbm.RBMTrainer;
import berlin.iconn.rbm.tools.ColorConverter;
import jpen.PKind;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.PenManager;
import jpen.event.PenAdapter;
import jpen.owner.multiAwt.AwtPenToolkit;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public class TabletCanvas extends PenAdapter {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        new TabletCanvas(new JPanel());

        /*
         frame.setSize(new Dimension(300, 330));
         frame.add(jp);
         // show the window and setup the panelg
         frame.setVisible(true);
         */
    }

    private boolean useHiddenStates;
    private boolean useVisibleStates;

    BufferedImage image;
    Graphics2D imageg;

    JPanel panel;
    Graphics2D panelg;

    JFrame frame = new JFrame("Drawing Surface");

    Point2D.Float prevLoc = new Point2D.Float();// previous location of cursor
    Point2D.Float loc = new Point2D.Float();// current location of cursor

    /* brush dynamics */
    float brushSize;
    float opacity;
    BasicStroke stroke;
    private BenchmarkModel benchmarkModel;

    float[] calcImageData;
    
    public TabletCanvas(JPanel a) {
        this.useHiddenStates = false;
        this.useVisibleStates = false;
        panel = a;

        // Use the AwtPenToolkit to register a PenListener on the panel:
        AwtPenToolkit.addPenListener(panel, this);
        // setup the mouse to cause a pressure level event when the left button is pressed:
        PenManager pm = AwtPenToolkit.getPenManager();
        pm.pen.levelEmulator.setPressureTriggerForLeftCursorButton(0.5f);

        panelg = (Graphics2D) panel.getGraphics();
        // make the lines smooth
        panelg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        panelg.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        panelg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        imageg = image.createGraphics();
        panelg.drawImage(image, null, 0, 0);

        // make the lines smooth
        imageg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        imageg.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        imageg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        clearImage();
    }

    @Override
    public void penLevelEvent(PLevelEvent ev) {
        // if this event was not a movement, do nothing
        if (ev.isMovement()) {

            // set the brush's size, and opacity relative to the pressure
            float pressure = ev.pen.getLevelValue(PLevel.Type.PRESSURE);
            brushSize = pressure * 30;

            // get the current cursor location
            loc.x = ev.pen.getLevelValue(PLevel.Type.X);
            loc.y = ev.pen.getLevelValue(PLevel.Type.Y);

            if (brushSize > 0) {
                if (ev.pen.getKind() == PKind.valueOf(PKind.Type.ERASER)) // using the eraser, create a white line, effectively "erasing" the black line
                {
                    // set the color to white, and create the stroke
                    imageg.setColor(Color.white);
                    stroke = new BasicStroke(brushSize * 2); // make it a bit more sensitive
                } else// default, we want to draw a black line onto the screen.
                {
                    // set the opacity, and create the stroke
                    //panelg.setColor(new Color((int)opacity, (int)opacity, 255, 255));
                    stroke = new BasicStroke(brushSize,
                            BasicStroke.CAP_ROUND, // round line endings
                            BasicStroke.JOIN_MITER
                    );
                }

                // draw a line between the current and previous locations
                imageg.setStroke(stroke);
                imageg.draw(new Line2D.Float(prevLoc, loc));
            }

            // set the current position to the previous position
            prevLoc.setLocation(loc);

            // paint pane to draw brush info on
            imageg.setColor(Color.black);

            panelg.drawImage(image, null, 0, 0);
            /*
             panelg.setColor(Color.white);
             panelg.fillRect(0, 5, 155, 35);
             panelg.setColor(Color.black);
             panelg.drawString(("Brush size: " + brushSize), 5, 20);
             panelg.drawString(("Opacity: " + opacity), 5, 35);
             */

        } else {
            float t = ev.pen.getLevelValue(PLevel.Type.PRESSURE);
            if (t == 0.0f) {
                onRelease();
            }
            System.out.println("ev: " + ev);
        }
    }

    public void onRelease() {
        /* prepare image */
        calcImageData = getFloatImage();
        saveCurrentImage("bla3.jpeg", DataConverter.pixelDataToImage(calcImageData, 0.0f, false));
        saveCurrentImage("bla2.jpeg", image);

        /* send image to rbm  , run hidden, run visible    return image  */
        runRBM();

        // show result in the right panel
    }

    public float[] getFloatImage() {
        BufferedImage clipped;

        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int bottom = 0;
        int right = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != new Color(255, 255, 255).getRGB()) {

                    if (x < left) {
                        left = x;
                    }
                    if (y < top) {
                        top = y;
                    }

                    if (x > right) {
                        right = x;
                    }
                    if (y > bottom) {
                        bottom = y;
                    }

                }
            }
        }
        System.out.println("l: " + left + " ,r: " + right + " ,t: " + top + " ,b: " + bottom);

        int maxSize = (int) (Math.max(right - left, bottom - top) * 1.4);
        System.out.println("max " + maxSize);
        clipped = new BufferedImage(maxSize, maxSize, BufferedImage.TYPE_INT_ARGB);
        mergeImages(clipped, image.getSubimage(left, top, right - left, bottom - top));
        saveCurrentImage("bla1.jpeg", clipped);
        int edgeLength = 28;
        boolean binarize = true;
        boolean invert = true;
        float minData = 0.0f;
        float maxData = 1.0f;
        boolean isRgb = false;
        return DataConverter.processPixelData(clipped, edgeLength, binarize, invert, minData, maxData, isRgb);
    }

    private void clearImage() {
        imageg.setColor(Color.white);
        imageg.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        panelg.setColor(Color.white);
        panelg.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        panel.repaint();
    }

    public void saveCurrentImage(String name, BufferedImage i) {
        try {
            String path = "E:\\";
            ImageIO.write(i, "JPEG", new File(path + name));
        } catch (IOException e) {
        }
    }

    public void setVisible(boolean tmp) {
        frame.setVisible(tmp);
    }

    void setSize(MainTabletCanvas aThis) {

    }

    public void setBenchmarkModel(BenchmarkModel b) {
        benchmarkModel = b;
    }

    private void runRBM() {

        if (benchmarkModel.getRbmSettingsList().isEmpty()) {
            return;
        }

        int delay = 0; // delay for 3 sec. 
        int period = 50;
        java.util.Timer timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    System.out.println("Dream");
                    daydream();
                    System.out.println("daydream over");
                    /*
                    javafx.scene.image.Image visibleImage = model.getVisibleImage((int) imgv_Result.getFitWidth(), (int) imgv_Result.getFitHeight());
                    imgv_Result.setImage(visibleImage);
                    javafx.scene.image.Image hiddenImage = model.getHiddenImage(10);
                    imgv_ResultHidden.setFitWidth(hiddenImage.getWidth());
                    imgv_ResultHidden.setFitHeight(hiddenImage.getHeight());
                    imgv_ResultHidden.setImage(hiddenImage);
                    */
                });

            }
        }, delay, period);
    }

    public void daydream() {
        RBMTrainer trainer = new RBMTrainer();

        // Create visible daydream data, which is used for the next calculation step
        float[] visibleDataForCalc = trainer.daydreamAllRBMs(this.benchmarkModel, this.calcImageData, this.useHiddenStates, this.useVisibleStates);

        // Create hidden and visible daydream data, which is used for visualization
        float[] hiddenData = trainer.getHiddenAllRBMs1D(this.benchmarkModel, this.calcImageData, this.useHiddenStates);
        float[] visibleDataForVis = trainer.getVisibleAllRBMs1D(this.benchmarkModel, hiddenData, this.useVisibleStates);

        // Convert hiddenData to pixels
        int hiddenImageEdgeLength = (int) Math.sqrt(hiddenData.length);
        int[] hiddenImagePixels = new int[hiddenImageEdgeLength * (hiddenImageEdgeLength + 1)];

        int counter = 0;
        for (int y = 0; y < hiddenImageEdgeLength + 1; y++) {
            for (int x = 0; x < hiddenImageEdgeLength; x++) {
                int pos = y * hiddenImageEdgeLength + x;
                if (counter < hiddenData.length) {
                    int hiddenValue = (int) Math.round(hiddenData[pos] * 255);
                    hiddenImagePixels[pos] = (0xFF << 24) | (hiddenValue << 16) | (hiddenValue << 8) | hiddenValue;
                } else {
                    hiddenImagePixels[pos] = (0xFF << 24) | (255 << 16) | (0 << 8) | 0;
                }
                counter++;
            }
        }

        this.calcImageData = visibleDataForCalc;
        BufferedImage visibleImage = DataConverter.pixelDataToImage(visibleDataForVis, 0.0f, this.benchmarkModel.isRgb());
        
        BufferedImage hiddenImage = new BufferedImage(hiddenImageEdgeLength, hiddenImageEdgeLength + 1, BufferedImage.TYPE_INT_RGB);
        hiddenImage.setRGB(0, 0, hiddenImageEdgeLength, hiddenImageEdgeLength + 1, hiddenImagePixels, 0, hiddenImageEdgeLength);
        
        
    }

    private void mergeImages(BufferedImage img1, BufferedImage img2) {
        // http://stackoverflow.com/questions/20826216/copy-two-buffered-image-into-one-image-side-by-side

        //do some calculate first
        int offsetw = (int) ((img1.getWidth() - img2.getWidth()) / 2.0f);
        int offseth = (int) ((img1.getHeight() - img2.getHeight()) / 2.0f);

        //create a new buffer and draw two image into the new image
        Graphics2D g2 = img1.createGraphics();

        //fill background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, img1.getWidth(), img1.getHeight());

        //draw image
        g2.drawImage(img2, null, offsetw, offseth);
        g2.dispose();
    }
}
