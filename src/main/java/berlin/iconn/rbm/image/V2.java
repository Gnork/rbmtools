package berlin.iconn.rbm.image;

/**
 *  simple Vector class two store x, y value pairs
 */
public class V2 {
    public float x, y;

    public V2() {
        x = 0.0f;
        y = 0.0f;
    }

    public V2(float xx, float yy) {
        x = xx;
        y = yy;
    }

    public V2(V2 tmp) {
        x = tmp.x;
        y = tmp.y;
    }
    public void set(V2 tmp) {
        x = tmp.x;
        y = tmp.y;
    }
    public void set(float xx, float yy) {
        x = xx;
        y = yy;
    }
    public V2 mul(float tmp) {
        return new V2(x * tmp, y * tmp);
    }
    public V2 sub(V2 tmp) {
        return new V2(x - tmp.x, y - tmp.y);
    }
    public V2 add(float tmp) {
        return new V2(x + tmp, y + tmp);
    }
    public V2 add(V2 tmp) {
        return new V2(x + tmp.x, y + tmp.y);
    }

    public V2 copy() {
        return new V2(x,y);
    }
}