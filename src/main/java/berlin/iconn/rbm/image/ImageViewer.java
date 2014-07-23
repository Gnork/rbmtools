/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.image;

import java.awt.image.BufferedImage;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 * shows a frame for a Buffered Image to be displayed
 * 
 */
public class ImageViewer { //extends AController implements IVisualizeObserver

    // letzter Zoomfaktor (zur Berechnung der Verschiebung des Bildes bei Zoomaenderung)
    private double zoomFactorLast = 1;
    private double zoomFactor = 1;

    private boolean drawFeatures = false;

    // diese Variablen steuern die Verschiebung der Ansicht (ueber Mouse-Drag)
    private int xm = 0;
    private int ym = 0;
    private int xMouseMove;
    private int yMouseMove;
    private int xMouseStartPos;
    private int yMouseStartPos;
    protected int xMousePos;
    protected int yMousePos;
    private final double borderFactor = 0.9;

    private final int width = 600;
    private final int height = 400;

    private Scene scene = null;
    private Group root = null;
    private Stage stage = null;
    private Pic[] images = null;
    private ImageManager imageManager = null;

    public double getHeight() {
        return stage.getHeight();
    }

    public double getY() {
        return stage.getY();
    }
    
    public double getWidth() {
        return stage.getWidth();
    }

    public double getX() {
        return stage.getX();
    }
    
    public ImageViewer() {
    	initalize();
    }
    
    public ImageViewer(ImageManager imageManager) {
    	this.imageManager = imageManager;
    	this.images = imageManager.getImages();
    	initalize();
    }
    
    public void setImages(Pic[] images) {
    	this.images = images;
    	initalize();
    }

    private void initalize() {
        this.root = new Group();
        this.stage = new Stage();

        this.scene = new Scene(root, width, height);
        this.scene.setFill(Color.LIGHTSLATEGREY);
        this.stage.setScene(this.scene);
        this.stage.setResizable(true);
        this.stage.setMinHeight(50);
        this.stage.setMinWidth(50);

        this.stage.setWidth(width);
        this.stage.setHeight(height);

        this.stage.setX(0);
        this.stage.setY(0);

        ChangeListener<Number> onResize = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newHeight) {
                xMouseMove = 0;
                yMouseMove = 0;
                ImageViewer.this.show();
            }
        };
        
        this.scene.widthProperty().addListener(onResize);
        this.scene.heightProperty().addListener(onResize);

        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                stage.close();
            }
        });

        this.scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouse) {
                xMousePos = xMouseStartPos = (int) Math.ceil(mouse.getX());
                yMousePos = yMouseStartPos = (int) Math.ceil(mouse.getY());
            }
        });

        this.scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouse) {
                xMousePos = (int) Math.ceil(mouse.getX());
                yMousePos = (int) Math.ceil(mouse.getY());
            }
        });

        this.scene.setOnMouseReleased(new EventHandler() {
            @Override
            public void handle(Event t) {
                xMouseMove = 0;
                yMouseMove = 0;
            }
        });

        this.scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouse) {
                xMousePos = (int) Math.ceil(mouse.getX());
                yMousePos = (int) Math.ceil(mouse.getY());
                xMouseMove = xMousePos - xMouseStartPos;
                yMouseMove = yMousePos - yMouseStartPos;
                xMouseStartPos = xMousePos;
                yMouseStartPos = yMousePos;
                show();
            }
        });
        this.scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                System.out.println("keypress: " + t.getCode());

                if (t.getCode() == KeyCode.ADD) {
                    zoomFactor *= 1.1;
                }

                if (t.getCode() == KeyCode.SUBTRACT) {
                    zoomFactor /= 1.1;
                }

                // Features anzeigen
                if (t.getCode() == KeyCode.F) {
                    drawFeatures = true;
                }

                // Bilder anzeigen
                if (t.getCode() == KeyCode.B) {
                    drawFeatures = false;
                }

                if (t.getCode() == KeyCode.R) {
                    for (Pic image : ImageViewer.this.images) {
                        image.setRank(image.getId());
                    }
                }
                show();
            }
        });

        this.scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouse) {
                xMousePos = (int) Math.ceil(mouse.getX());
                yMousePos = (int) Math.ceil(mouse.getY());
                Pic image = getImage(xMousePos, yMousePos);
                if (image != null) {

                    if (mouse.getButton() == MouseButton.PRIMARY) { //linke Maustaste
                        if (mouse.getClickCount() == 2) { //Doppelklick
                            if (!image.getCategory().equals("x")) {
                                System.out.println("Testen Bild " + image.getId());

                                System.out.println("NotImplemented Yet: ImageViewer -> sortByImage()");
                                // controller.sortByImage(image);
                                show();
                            }
                        }
                    }
                }

                xMouseMove = 0;
                yMouseMove = 0;

            }
        });

        this.scene.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scroll) {
                double delta = scroll.getDeltaY();
                if (delta > 0) {
                    zoomFactor *= 1.1;
                    if (zoomFactor > 50) {
                        zoomFactor = 50;
                    }
                } else {
                    zoomFactor /= 1.1;
                    if (zoomFactor < 1) {
                        zoomFactor = 1;
                    }
                }
                ImageViewer.this.show();
            }
        });
	}

	/**
     * Liefert das Bild zur������ck dass sich an einer bestimmten // Mausposition
     * befindet. Null bedeutet dass unter der Maus kein Bild ist
     *
     * @param xMouse
     * @param yMouse
     * @return
     */
    public Pic getImage(int xMouse, int yMouse) {
        for (Pic image : images) {
            int xs = image.getxStart();
            int ys = image.getyStart();
            int xLen = image.getxLen();
            int yLen = image.getyLen();

            if (xMouse > xs && xMouse < xs + xLen && yMouse > ys && yMouse < ys + yLen) {
                return image;
            }
        }
        return null; // kein Bild gefunden
    }

    public void show() {
        if (images != null) {
            root.getChildren().clear();
//          System.out.println(xMousePos + " " + yMousePos + "    " + xMouseMove + " " + yMouseMove);
            calculateDrawingPositions(xMousePos, yMousePos, xMouseMove, yMouseMove, zoomFactor);
            for (Pic image : images) {
                BufferedImage bi = (drawFeatures) ? image.getFeatureImage() : image.getDisplayImage();

                Image img = SwingFXUtils.toFXImage(bi, null);
                ImageView imgView = new ImageView(img);

                imgView.setX(image.getxStart());
                imgView.setY(image.getyStart());
                imgView.setFitWidth(image.getxLen());
                imgView.setFitHeight(image.getyLen());

                imgView.setVisible(true);
                root.getChildren().add(imgView);
            }
        }
        stage.show();
    }

    public void close() {
        stage.close();
    }

    private void calculateDrawingPositions(int xMousePos, int yMousePos, int xMouseMove, int yMouseMove, double zoomFactor) {
        int nThumbs = images.length;

        int hCanvas = (int) scene.getHeight();
        int wCanvas = (int) scene.getWidth();
        if (wCanvas <= 1) {
            wCanvas = this.width;
        }
        if(hCanvas <= 1){
            hCanvas = this.height;
        }
        int h2 = hCanvas / 2;
        int w2 = wCanvas / 2;

        // Groesse eines thumbnail-Bereichs
        int thumbSize = (int) Math.sqrt((double) wCanvas * hCanvas / nThumbs);
        while (thumbSize > 0 && (wCanvas / thumbSize) * (hCanvas / thumbSize) < nThumbs) {
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

        double scaledThumbSizeX = thumbSizeX * zoomFactor;
        double scaledThumbSizeY = thumbSizeY * zoomFactor;

        double sizeX = scaledThumbSizeX * borderFactor;
        double sizeY = scaledThumbSizeY * borderFactor;
        double size = Math.min(sizeX, sizeY);

        double xDelta = (w2 - xMousePos) * (zoomFactor / zoomFactorLast - 1);
        double yDelta = (h2 - yMousePos) * (zoomFactor / zoomFactorLast - 1);
        zoomFactorLast = zoomFactor;

        double xmLast = xm;
        double ymLast = ym;

        xm -= (xMouseMove + xDelta) / scaledThumbSizeX;
        ym -= (yMouseMove + yDelta) / scaledThumbSizeY;

        int xMinPos = (int) (w2 - xm * scaledThumbSizeX);
        int xMaxPos = (int) (xMinPos + mapPlacesX * scaledThumbSizeX);
        int yMinPos = (int) (h2 - ym * scaledThumbSizeY);
        int yMaxPos = (int) (yMinPos + mapPlacesY * scaledThumbSizeY);

        // disallow to move out of the map by dragging
        if (xMinPos > 0 || xMaxPos < wCanvas - 1) {
            xm = (int) xmLast;
            xMinPos = (int) (w2 - xm * scaledThumbSizeX);
            xMaxPos = (int) (xMinPos + mapPlacesX * scaledThumbSizeX);
        }
        // when zooming out (centered at the mouseposition) it might be
        // necessary to shift the map back to the canvas
        if (xMaxPos < wCanvas - 1) {
            int xMoveCorrection = wCanvas - 1 - xMaxPos;
            xMinPos += xMoveCorrection;
            xm -= xMoveCorrection / scaledThumbSizeX;
        } else if (xMinPos > 0) {
            xm += xMinPos / scaledThumbSizeX;
            xMinPos = 0;
        }

        // same for y
        if (yMinPos > 0 || yMaxPos < hCanvas - 1) {
            ym = (int) ymLast;
            yMinPos = (int) (h2 - ym * scaledThumbSizeY);
            yMaxPos = (int) (yMinPos + mapPlacesY * scaledThumbSizeY);
        }
        if (yMaxPos < hCanvas - 1) {
            int yMoveCorrection = hCanvas - 1 - yMaxPos;
            yMinPos += yMoveCorrection;
            ym -= yMoveCorrection / scaledThumbSizeY;
        } else if (yMinPos > 0) {
            ym += yMinPos / scaledThumbSizeY;
            yMinPos = 0;
        }

        // Zeichenposition errechnen
        for (Pic image : images) {

            int w = (drawFeatures) ? 64 : image.getOrigWidth();
            int h = (drawFeatures) ? 64 : image.getOrigHeight();

            // skalierung, keep aspect ratio
            double s = Math.max(w, h);
            double scale = size / s;

            int xLen = (int) (scale * w);
            int yLen = (int) (scale * h);

            int pos = image.getRank();

            int xStart = (int) (xMinPos + (pos % mapPlacesX) * scaledThumbSizeX);
            int yStart = (int) (yMinPos + (pos / mapPlacesX) * scaledThumbSizeY);

            int xs = xStart + (int) ((scaledThumbSizeX - xLen + 1) / 2); // xStart mit Rand
            int ys = yStart + (int) ((scaledThumbSizeY - yLen + 1) / 2);

            image.setxStart(xs);
            image.setxLen(xLen);
            image.setyStart(ys);
            image.setyLen(yLen);
        }
    }

		


}
