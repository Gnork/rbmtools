package berlin.iconn.rbm.views.imageviewer;


import berlin.iconn.rbm.image.V2;

public class Camera {
  private final V2  pos        = new V2(0, 0);

  private float zoomFactor = 1.0f;

  void move(V2 offset) {
    setPos(getPos().sub(offset));
  }

  void setPos(V2 offset) {
    pos.set(offset);
  }

  void setRelPos(V2 offset) {
    getPos().set(offset.mul((float) (1.0 / getZoomFactor())));
  }

  V2 getRelPos() {
    return getPos().mul((float) getZoomFactor());
  }

  public float getZoomFactor() {
    return zoomFactor;
  }

  public void setZoomFactor(float zoomFactor) {
    this.zoomFactor = zoomFactor;
  }

  public V2 getPos() {
    return pos;
  }
}
