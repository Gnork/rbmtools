package berlin.iconn.rbm.views.imageviewer;

import java.util.ArrayList;


import berlin.iconn.rbm.image.V2;
import berlin.iconn.rbm.imageviewer.drawables.ADrawable;

public class Paper {

	private final V2 size = new V2();
	private final ArrayList<ADrawable> drawables = new ArrayList<ADrawable>();

	Paper(V2 s) {
		size.set(s);
	}

	public Paper() {
	  autoSize();
	}

	public void autoSize() {
		V2 maxSize = new V2();

		for (ADrawable e : drawables) {
			if (e.getSize() != null) {
				V2 tmpSize = (e.getSize().add(e.getPos()));
				if (maxSize.x < tmpSize.x)
					maxSize.x = tmpSize.x;

				if (maxSize.y < tmpSize.y)
					maxSize.y = tmpSize.y;
			}
		}

		setSize(maxSize);
	}

	public void setSize(V2 s) {
		size.set(s);
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
}
