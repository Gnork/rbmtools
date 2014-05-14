package berlin.iconn.rbm.imageviewer.drawables;


import berlin.iconn.rbm.image.V2;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import berlin.iconn.rbm.image.Pic;

public class Image extends ADrawable {
    javafx.scene.image.Image image;

    public Image(Pic i) {

        // from Pic
        image = SwingFXUtils.toFXImage(i.getDisplayImage(), null);

        // from internet
        // image = new
        // javafx.scene.image.Image("http://i.imgur.com/n0yfX0B.png");

        // local
        // image = new
        // javafx.scene.image.Image(getClass().getResourceAsStream("./logo.png"));

        size.set(new V2((float) image.getWidth(), (float) image
                .getHeight()));
        pos.set(new V2(0, 0));
    }

    @Override
    public void draw(GraphicsContext gc, V2 offset, double zoom) {
        gc.drawImage(image, (pos.x - offset.x) * zoom, zoom
                * (pos.y - offset.y), size.x * zoom, size.y * zoom);
    }

    public void setPos(V2 v) {
        this.pos.set(v);
    }

    @Override
    public void draw(GraphicsContext gc, V2 offset, double zoom,
                     V2 newSize) {
        gc.drawImage(image, (pos.x - offset.x) * zoom, zoom
                * (pos.y - offset.y), newSize.x * zoom, newSize.y * zoom);
    }
}
