package berlin.iconn.rbm.sorter;

import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;

import berlin.iconn.rbm.image.Pic;

public abstract class ASorter {
	
	private Pic[] images;
	private ForkJoinPool pool;
	
	public ASorter(Pic[] images, ForkJoinPool pool) {
		this.images = images;
		this.pool = pool;
	}
	
	
	public void getFeatureVectors() {
		getFeatureVectors(this.images);
	}
	
	public void getFeatureVectors(Pic[] images) {
		ForkSorterCalculations fsc = new ForkSorterCalculations(this, images, 0, images.length);
		pool.invoke(fsc);
	}
	
	/**
	 * Euklidische Distanz
	 * @param val1
	 * @param val2
	 * @return
	 */
	public static float getEuclideanDist(float[] val1, float[] val2) {
		float dist = 0;
		for (int i = 0; i < val2.length; i++) {
			float buff = val1[i] - val2[i];
			dist += buff * buff;
		}
		return dist;
	}	
	
	public static float getL1Dist(float[] val1, float[] val2) {
		float dist = 0;
		for (int i = 0; i < val2.length; i++)
			dist += Math.abs(val1[i] - val2[i]);
		return dist;
	}	
	
	public abstract BufferedImage getFeatureImage(Pic image);
	public abstract float getDistance(float[] fv1, float[] fv2); 
	public abstract String getName();
}
