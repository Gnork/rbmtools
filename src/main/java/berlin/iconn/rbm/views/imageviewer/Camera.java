package berlin.iconn.rbm.views.imageviewer;

import com.badlogic.gdx.math.Vector2;

public class Camera {
  private final Vector2  pos        = new Vector2(0, 0);

  private float zoomFactor = 1.0f;

  void move(Vector2 offset) {
    setPos(getPos().sub(offset));
  }

  void setPos(Vector2 offset) {
    pos.set(offset);
  }

  void setRelPos(Vector2 offset) {
    getPos().set(offset.mul((float) (1.0 / getZoomFactor())));
  }

  Vector2 getRelPos() {
    return getPos().mul((float) getZoomFactor());
  }

  public float getZoomFactor() {
    return zoomFactor;
  }

  public void setZoomFactor(float zoomFactor) {
    this.zoomFactor = zoomFactor;
  }

  public Vector2 getPos() {
    return pos;
  }
}
