package berlin.iconn.rbm.views.imageviewer;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

import berlin.iconn.rbm.imageviewer.drawables.ADrawable;

public class Paper {

	private final Vector2 size = new Vector2();
	private final ArrayList<ADrawable> drawables = new ArrayList<ADrawable>();

	Paper(Vector2 s) {
		size.set(s);
	}

	public Paper() {
	  autoSize();
	}

	public void autoSize() {
		Vector2 maxSize = new Vector2();

		for (ADrawable e : drawables) {
			if (e.getSize() != null) {
				Vector2 tmpSize = (e.getSize().add(e.getPos()));
				if (maxSize.x < tmpSize.x)
					maxSize.x = tmpSize.x;

				if (maxSize.y < tmpSize.y)
					maxSize.y = tmpSize.y;
			}
		}

		setSize(maxSize);
	}

	public void setSize(Vector2 s) {
		size.set(s);
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
}
