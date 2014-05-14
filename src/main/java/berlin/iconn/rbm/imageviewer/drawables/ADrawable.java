package berlin.iconn.rbm.imageviewer.drawables;

import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;

import berlin.iconn.rbm.image.V2;

public abstract class ADrawable {

	protected ArrayList<ADrawable> drawables = new ArrayList<>();
	protected final V2 pos = new V2();
	protected final V2 size = new V2();

	public abstract void draw(GraphicsContext gc, V2 offset, double zoom);

	public abstract void draw(GraphicsContext gc, V2 offset, double zoom,
			V2 newSize);

	public V2 getPos() {
		return pos.copy();
	}

	public void addDrawable(ADrawable d) {
		drawables.add(d);
	}

	public ArrayList<ADrawable> getDrawables() {
		return drawables;
	}

	public V2 getSize() {
		return size.copy();
	}

	public void setPos(V2 v) {
		this.pos.set(v);
	}
}
