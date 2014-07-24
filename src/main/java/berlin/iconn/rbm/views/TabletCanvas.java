package berlin.iconn.rbm.views;

/**
 * Created by G on 12.07.14.
 */
/**
 *
 * @author John M (http://sourceforge.net/users/nextdesign)
 *
 * <b>Changelog:</b>
 * <ul>
 * <li>2010/01/16 by Nicol�s Carranza: - changed to make it work with the mouse
 * too - draw always when pressure &gt; 0</li>
 * <li>2011/06/23 by Nicol�s Carranza: - changed to use the AwtPenToolkit -
 * added note about technique to get better performance</li>
 * </ul>
 */
import berlin.iconn.rbm.image.DataConverter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

import jpen.PKind;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.PenManager;
import jpen.event.PenAdapter;
import jpen.owner.multiAwt.AwtPenToolkit;

public class TabletCanvas extends PenAdapter {

    private static final long serialVersionUID = 1L;

    BufferedImage image;
    Graphics2D imageg;

    JPanel panel = new JPanel();
    Graphics2D panelg;

    JFrame frame = new JFrame("Drawing Surface");
    JPanel jp = new JPanel();

    Point2D.Float prevLoc = new Point2D.Float();// previous location of cursor
    Point2D.Float loc = new Point2D.Float();// current location of cursor

    /* brush dynamics */
    float brushSize;
    float opacity;
    BasicStroke stroke;
    DaydreamController daydreamController;

    public TabletCanvas(DaydreamController dm) {
        daydreamController = dm;
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener((ActionEvent ae) -> {
            clearImage();
        });
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener((ActionEvent ae) -> {
            daydreamController.sendCanvasImage();
        });
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        frame.setSize(new Dimension(300, 330));
        jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));
        frame.add(jp);

        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jp.add(panel);
        jp.add(clearButton);
        jp.add(sendButton);

        // Use the AwtPenToolkit to register a PenListener on the panel:
        AwtPenToolkit.addPenListener(panel, this);
        // setup the mouse to cause a pressure level event when the left button is pressed:
        PenManager pm = AwtPenToolkit.getPenManager();
        pm.pen.levelEmulator.setPressureTriggerForLeftCursorButton(0.5f);

        // show the window and setup the panelg
        frame.setVisible(true);
        panelg = (Graphics2D) panel.getGraphics();
        // make the lines smooth
        panelg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        panelg.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        panelg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        imageg = image.createGraphics();
        clearImage();

        // make the lines smooth
        imageg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        imageg.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        imageg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    @Override
    public void penLevelEvent(PLevelEvent ev) {
        
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

        } else {
            float t = ev.pen.getLevelValue(PLevel.Type.PRESSURE);
            if (t == 0.0f) {
                onRelease();
            }
        }
    }

    public void onRelease() {
        //daydreamModel.sendTabletImage();
    }
    
    private void clearImage() {
        imageg.setColor(Color.white);
        imageg.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        panelg.setColor(Color.white);
        panelg.fillRect(0, 0, panel.getWidth(), panel.getHeight());
    }

    public BufferedImage getCurrentImage() {
        return image;
    }

    public void saveCurrentImage(String path) {
        try {
            ImageIO.write(image, "JPEG", new File(path));
        } catch (IOException e) {
        }
    }

    public void setVisible(boolean tmp) {
        frame.setVisible(tmp);
    }
}
