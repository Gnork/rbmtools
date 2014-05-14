package berlin.iconn.rbm.views.imageviewer;

import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.image.Pic;
import berlin.iconn.rbm.image.V2;
import berlin.iconn.rbm.imageviewer.drawables.ADrawable;
import berlin.iconn.rbm.imageviewer.drawables.FlowGroup;
import berlin.iconn.rbm.imageviewer.drawables.Image;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
public class ImageViewerModel{

	private final ImageViewerController controller;

	Camera camera;
	Paper paper;

	V2 pos = new V2(0, 0);

	Canvas canvas;
	GraphicsContext gc;

	private V2 lastMousePosition = new V2(0, 0);

	ImageViewerModel(ImageViewerController controller) {
		this.controller = controller;
		canvas = controller.canvas;

		setSize(new V2(600, 400));
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

	V2 getMousePos(MouseEvent e) {
		float a = (float) e.getX();
		float b = (float) e.getY();
		return new V2(a, b);
	}

	V2 getMousePos(ScrollEvent e) {
		float a = (float) e.getX();
		float b = (float) e.getY();
		return new V2(a, b);
	}

	void onResize(int w, int h) {
		setSize(new V2(w, h));
	}

	void onMouseWheel(ScrollEvent e) {

		V2 mpos = getMousePos(e);
		V2 a = (mpos.add(camera.getRelPos())).mul((float) (1 / camera
				.getZoomFactor()));

		if (e.getDeltaY() > 0)
			camera.setZoomFactor(camera.getZoomFactor() * 1.1f);
		else
			camera.setZoomFactor(camera.getZoomFactor() / 1.1f);

		V2 newPos = (a.mul(camera.getZoomFactor())).sub(getMousePos(e));

		camera.setRelPos(newPos);
	}

	void onMouseDown(MouseEvent e) {
		lastMousePosition.set(getMousePos(e));
	}

	void onMouseDragging(MouseEvent e) {
		V2 offset = lastMousePosition.sub(getMousePos(e));
		camera.setPos(camera.getPos().add(
				offset.mul(1 / camera.getZoomFactor())));
		lastMousePosition.set(getMousePos(e));
	}

	void centerCamera() {
		V2 desiredPaperSize = paper.getSize().mul(camera.getZoomFactor());
		V2 tmp = desiredPaperSize.sub(getSize());
		camera.setPos(tmp.mul(1 / (2 * camera.getZoomFactor())));
	}

	void draw() {
                if(paper == null){
                    return;
                }
		// draw background
		gc.setFill(new Color(0.2, 0.2, 0.2, 1));
		gc.fillRect(0, 0, getSize().x, getSize().y);

		// draw all drawables
		for (ADrawable d : paper.getDrawables()) {
			d.draw(gc, camera.getPos(), camera.getZoomFactor());
		}
	}

	void setSize(V2 s) {
		canvas.setWidth(s.x);
		canvas.setHeight(s.y);
	}

	V2 getSize() {
		return new V2((float) canvas.getWidth(),
				(float) canvas.getHeight());
	}

	void setPos(V2 p) {
		pos.set(p);
	}

	V2 getPos() {
		return pos;
	}
	
	/*  @Override
	  public void update(RBMInfoPackage pack) {
		  
		  int inputSize =  pack.getWeights().length;
		  int outputSize = pack.getWeights()[0].length;
	      
	      IRBM rbm = new RBMJBlasOpti(inputSize, outputSize, 0.01f, new DefaultLogisticMatrixFunction(), false, 0, pack.getWeights());
	      
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
        */
}
