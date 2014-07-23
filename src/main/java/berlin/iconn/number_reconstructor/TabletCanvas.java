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

    public TabletCanvas(JPanel a) {
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
                float[] preparedImage = getFloatImage();
                
                saveCurrentImage("D:\\bla3.jpeg", DataConverter.pixelDataToImage(preparedImage, 0.0f, false));
                saveCurrentImage("D:\\bla2.jpeg", image);
            }
            System.out.println("ev: " + ev);
        }
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
        clipped = image.getSubimage(left, top, right - left, bottom - top);

        saveCurrentImage("D:\\bla1.jpeg", clipped);
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

    public void saveCurrentImage(String path, BufferedImage i) {
        try {
            ImageIO.write(i, "JPEG", new File(path));
        } catch (IOException e) {
        }
    }

    public void setVisible(boolean tmp) {
        frame.setVisible(tmp);
    }

    void setSize(MainTabletCanvas aThis) {

    }

}
