package berlin.iconn.rbm.imageviewer.drawables;

import java.util.ArrayList;

import berlin.iconn.rbm.image.V2;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;


public class FlowGroup extends ADrawable {
    Canvas canvas;

    public FlowGroup(ArrayList<ADrawable> e, Canvas p) {
        canvas = p;
        drawables = e;
        pos.set(0, 0);
    }

    @Override
    public void draw(GraphicsContext gc, V2 offset, double zoom) {

        V2 border = new V2(3, 3);
        boolean drawFeatures = false;
        int nThumbs = drawables.size();

        int hCanvas = (int) canvas.getHeight();
        int wCanvas = (int) canvas.getWidth();

        // ubuntu hack to avoid devide by zero exception
        // the frame gets sized up very fast but starts with height and width 0
        if (hCanvas < 10 || wCanvas < 10 || nThumbs < 10) {
            return;
        }

        // Groesse eines thumbnail-Bereichs
        int thumbSize = (int) Math.sqrt((double) wCanvas * hCanvas / nThumbs);
        while (thumbSize > 0
                && (wCanvas / thumbSize) * (hCanvas / thumbSize) < nThumbs) {
            --thumbSize;
        }

        int mapPlacesX = wCanvas / thumbSize;
        int mapPlacesY = hCanvas / thumbSize;

        // avoid empty lines at the bottom
        while (mapPlacesX * (mapPlacesY - 1) >= nThumbs) {
            mapPlacesY--;
        }

        double thumbSizeX = (double) wCanvas / mapPlacesX;
        double thumbSizeY = (double) hCanvas / mapPlacesY;

        double size = Math.min(thumbSizeX, thumbSizeY);

        int i = 0;
        for (ADrawable image : drawables) {

            int w = (int) ((drawFeatures) ? 64 : image.getSize().x);
            int h = (int) ((drawFeatures) ? 64 : image.getSize().y);

            // skalierung, keep aspect ratio
            double s = Math.max(w, h);
            double scale = size / s;

            int xLen = (int) (scale * w);
            int yLen = (int) (scale * h);

            // is sortet by image.getRank()
            int pos = i;

            int xStart = (int) ((pos % mapPlacesX) * thumbSizeX);
            int yStart = (int) ((pos / mapPlacesX) * thumbSizeY);

            drawables.get(i).setPos(new V2(xStart, yStart));
            drawables.get(i).draw(
                    gc,
                    offset,
                    zoom,
                    new V2((float) xLen - border.x, (float) yLen
                            - border.y));
            i++;
        }
    }

    public V2 getPos() {
        return new V2(0, 0);
    }

    public V2 getSize() {
        return new V2((float) canvas.getWidth(), (float) canvas.getHeight());
    }

    @Override
    public void draw(GraphicsContext gc, V2 offset, double zoom,
                     V2 newSize) {

    }
}
