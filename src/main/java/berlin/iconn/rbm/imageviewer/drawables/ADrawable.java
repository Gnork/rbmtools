package berlin.iconn.rbm.imageviewer.drawables;

import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;

import com.badlogic.gdx.math.Vector2;

public abstract class ADrawable {

	protected ArrayList<ADrawable> drawables = new ArrayList<>();
	protected final Vector2 pos = new Vector2();
	protected final Vector2 size = new Vector2();

	public abstract void draw(GraphicsContext gc, Vector2 offset, double zoom);

	public abstract void draw(GraphicsContext gc, Vector2 offset, double zoom,
			Vector2 newSize);

	public Vector2 getPos() {
		return pos.cpy();
	}

	public void addDrawable(ADrawable d) {
		drawables.add(d);
	}

	public ArrayList<ADrawable> getDrawables() {
		return drawables;
	}

	public Vector2 getSize() {
		return size.cpy();
	}

	public void setPos(Vector2 v) {
		this.pos.set(v);
	}
}
