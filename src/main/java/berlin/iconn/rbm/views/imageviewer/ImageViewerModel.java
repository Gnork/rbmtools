package berlin.iconn.rbm.views.imageviewer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import com.badlogic.gdx.math.Vector2;

import berlin.iconn.rbm.enhancement.IVisualizeObserver;
import berlin.iconn.rbm.enhancement.RBMInfoPackage;
import berlin.iconn.rbm.image.DataConverter;
import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.image.Pic;
import berlin.iconn.rbm.imageviewer.drawables.ADrawable;
import berlin.iconn.rbm.imageviewer.drawables.FlowGroup;
import berlin.iconn.rbm.imageviewer.drawables.Image;
import berlin.iconn.rbm.logistic.DefaultLogisticMatrixFunction;
import berlin.iconn.rbm.rbm.IRBM;
import berlin.iconn.rbm.rbm.RBMJBlasAVG;

public class ImageViewerModel implements IVisualizeObserver {

	private final ImageViewerController controller;

	Camera camera;
	Paper paper;

	Vector2 pos = new Vector2(0, 0);

	Canvas canvas;
	GraphicsContext gc;

	private Vector2 lastMousePosition = new Vector2(0, 0);

	ImageViewerModel(ImageViewerController controller) {
		this.controller = controller;
		canvas = controller.canvas;

		setSize(new Vector2(600, 400));
		gc = canvas.getGraphicsContext2D();
		camera = new Camera();

	}

	public void setImages(Pic[] images) {
		// sort images according to rank
		java.util.Arrays.sort(images);

		paper = new Paper();

		ArrayList<ADrawable> elements = new ArrayList<>();
		for (Pic p : images) {
			elements.add(new Image(p));
		}

		paper.addDrawable(new FlowGroup(elements, canvas));
		paper.autoSize();

		zoomFitCamera(.95f);
		centerCamera();

		draw();
	}

	public void setImages(ImageManager imageManager) {
		Pic[] images = imageManager.getImages();
		setImages(images);
	}

	void zoomFitCamera(double factor) {
		float w, h;

		w = getSize().x / paper.getSize().x;
		h = getSize().y / paper.getSize().y;

		camera.setZoomFactor((float) (factor * Math.min(w, h)));
	}

	void onKeyPressed(KeyEvent e) {
		System.out.println("Key Pressed: " + e.getCharacter());
		switch (e.getCharacter()) {
		case "R":
			break;
		}
	}

	Vector2 getMousePos(MouseEvent e) {
		float a = (float) e.getX();
		float b = (float) e.getY();
		return new Vector2(a, b);
	}

	Vector2 getMousePos(ScrollEvent e) {
		float a = (float) e.getX();
		float b = (float) e.getY();
		return new Vector2(a, b);
	}

	void onResize(int w, int h) {
		setSize(new Vector2(w, h));
	}

	void onMouseWheel(ScrollEvent e) {

		Vector2 mpos = getMousePos(e);
		Vector2 a = (mpos.add(camera.getRelPos())).mul((float) (1 / camera
				.getZoomFactor()));

		if (e.getDeltaY() > 0)
			camera.setZoomFactor(camera.getZoomFactor() * 1.1f);
		else
			camera.setZoomFactor(camera.getZoomFactor() / 1.1f);

		Vector2 newPos = (a.mul(camera.getZoomFactor())).sub(getMousePos(e));

		camera.setRelPos(newPos);
	}

	void onMouseDown(MouseEvent e) {
		lastMousePosition.set(getMousePos(e));
	}

	void onMouseDragging(MouseEvent e) {
		Vector2 offset = lastMousePosition.sub(getMousePos(e));
		camera.setPos(camera.getPos().add(
				offset.mul(1 / camera.getZoomFactor())));
		lastMousePosition.set(getMousePos(e));
	}

	void centerCamera() {
		Vector2 desiredPaperSize = paper.getSize().mul(camera.getZoomFactor());
		Vector2 tmp = desiredPaperSize.sub(getSize());
		camera.setPos(tmp.mul(1 / (2 * camera.getZoomFactor())));
	}

	void draw() {
		// draw background
		gc.setFill(new Color(0.2, 0.2, 0.2, 1));
		gc.fillRect(0, 0, getSize().x, getSize().y);

		// draw all drawables

		for (ADrawable d : paper.getDrawables()) {
			d.draw(gc, camera.getPos(), camera.getZoomFactor());
		}
	}

	void setSize(Vector2 s) {
		canvas.setWidth(s.x);
		canvas.setHeight(s.y);
	}

	Vector2 getSize() {
		return new Vector2((float) canvas.getWidth(),
				(float) canvas.getHeight());
	}

	void setPos(Vector2 p) {
		pos.set(p);
	}

	Vector2 getPos() {
		return pos;
	}
	
	  @Override
	  public void update(RBMInfoPackage pack) {
		  
		  int inputSize =  pack.getWeights().length;
		  int outputSize = pack.getWeights()[0].length;
	      
	      IRBM rbm = new RBMJBlasAVG(inputSize, outputSize, 0.01f, new DefaultLogisticMatrixFunction(), false, 0, pack.getWeights());
	      
	      Pic[] pics = new Pic[outputSize - 1];
	      
	      for (int i = 0; i < outputSize - 1; i++) {
	        float[][] hiddenData = new float[1][outputSize - 1];
	        hiddenData[0][i] = 1.0f; 
	        
	        float[][] visibleData = rbm.getVisible(hiddenData, false);
	        
	        BufferedImage image = DataConverter.pixelDataToImage(visibleData[0], 0, false);
	        
	        Pic pic = new Pic();
	        pic.setDisplayImage(image);
	        pic.setOrigWidth(image.getWidth());
	        pic.setOrigHeight(image.getHeight());
	        pic.setRank(i);
	        pics[i] = pic;
	      }
	      
	      this.setImages(pics);
	  }
}
