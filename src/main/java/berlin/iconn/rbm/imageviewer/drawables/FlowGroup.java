package berlin.iconn.rbm.imageviewer.drawables;

import java.util.ArrayList;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import com.badlogic.gdx.math.Vector2;

public class FlowGroup extends ADrawable {
	Canvas canvas;

	public FlowGroup(ArrayList<ADrawable> e, Canvas p) {
		canvas = p;
		drawables = e;
		pos.set(0, 0);
	}

	@Override
	public void draw(GraphicsContext gc, Vector2 offset, double zoom) {

		Vector2 border = new Vector2(3, 3);
		boolean drawFeatures = false;
		int nThumbs = drawables.size();

		int hCanvas = (int) canvas.getHeight();
		int wCanvas = (int) canvas.getWidth();

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

			drawables.get(i).setPos(new Vector2(xStart, yStart));
			drawables.get(i).draw(
					gc,
					offset,
					zoom,
					new Vector2((float) xLen - border.x, (float) yLen
							- border.y));
			i++;
		}
	}

	public Vector2 getPos() {
		return new Vector2(0, 0);
	}

	public Vector2 getSize() {
		return new Vector2((float) canvas.getWidth(),
				(float) canvas.getHeight());
	}

	@Override
	public void draw(GraphicsContext gc, Vector2 offset, double zoom,
			Vector2 newSize) {

	}
}
