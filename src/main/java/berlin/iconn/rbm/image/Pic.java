package berlin.iconn.rbm.image;

import java.awt.image.BufferedImage;

public class Pic implements Comparable<Pic> {

	private String name;
	private String type;
	private int id;

	private boolean isSelected;
	private int rank; // Position bei sortierter 1D-Reihenfolge
	private float distance;
	private int typeOccurrence;

	private float[] data;
	private float[] featureVector;

	// Originalgr����e des Bildes
	private int origWidth;
	private int origHeight;

	// Zeichenpositionen
	private int xStart = 0;
	private int xLen = 0;
	private int yStart = 0;
	private int yLen = 0;

	// zur Visualisierung
	private BufferedImage bImage;
	private BufferedImage featureImage;

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getTypeOccurrence() {
		return typeOccurrence;
	}

	public void setTypeOccurrence(int typeOccurrence) {
		this.typeOccurrence = typeOccurrence;
	}

	public float[] getFeatureVector() {
		return featureVector;
	}

	public void setFeatureVector(float[] featureVector) {
		this.featureVector = featureVector;
	}

	public int getOrigWidth() {
		return origWidth;
	}

	public void setOrigWidth(int origWidth) {
		this.origWidth = origWidth;
	}

	public int getOrigHeight() {
		return origHeight;
	}

	public void setOrigHeight(int origHeight) {
		this.origHeight = origHeight;
	}

	public int getxStart() {
		return xStart;
	}

	public void setxStart(int xStart) {
		this.xStart = xStart;
	}

	public int getxLen() {
		return xLen;
	}

	public void setxLen(int xLen) {
		this.xLen = xLen;
	}

	public int getyStart() {
		return yStart;
	}

	public void setyStart(int yStart) {
		this.yStart = yStart;
	}

	public int getyLen() {
		return yLen;
	}

	public void setyLen(int yLen) {
		this.yLen = yLen;
	}

	public BufferedImage getDisplayImage() {
		return bImage;
	}

	public void setDisplayImage(BufferedImage bImage) {
		this.bImage = bImage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public BufferedImage getFeatureImage() {
		return featureImage;
	}

	public void setFeatureImage(BufferedImage featureImage) {
		this.featureImage = featureImage;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public void setData(float[] data) {
		this.data = data;
	}

	public float[] getData() {
		return this.data;
	}

	@Override
	public int compareTo(Pic argument) {
		if (rank < argument.getRank())
			return -1;
		if (rank > argument.getRank())
			return 1;
		return 0;
	}

}
