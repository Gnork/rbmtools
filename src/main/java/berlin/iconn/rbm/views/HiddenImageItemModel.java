package berlin.iconn.rbm.views;

import javafx.scene.image.Image;

public class HiddenImageItemModel {

	private HiddenImageItemController hiddenImageItemController;
	
	private Image hiddenImage;
	private boolean useFeature;
	private float weight;
	private int index;

	public HiddenImageItemModel(HiddenImageItemController hiddenImageItemController) {
		this.hiddenImageItemController = hiddenImageItemController;
	}
	
	public void setHiddenImage(Image hiddenImage) {
		this.hiddenImage = hiddenImage;
	}

	public void setUseFeature(boolean useFeature) {
		this.useFeature = useFeature;
		if(this.useFeature) {
			this.weight = 1.0f;
		} else {
			this.weight = 0.0f;
		}
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}

	public Image getHiddenImage() {
		return hiddenImage;
	}

	public boolean isUseFeature() {
		return useFeature;
	}

	public float getWeight() {
		return weight;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
