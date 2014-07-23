package berlin.iconn.rbm.image;

public class ImagePair implements Comparable<ImagePair> {

	private double distance;
	private Pic searchImage;
	private Pic queryImage;
        /**
         * image pair calculates and stores the distance between two images
         * @param queryImage
         * @param searchImage
         * @param distance 
         */
	
	public ImagePair(Pic queryImage, Pic searchImage, double distance) {
		this.distance = distance;
		this.queryImage = queryImage;
		this.searchImage = searchImage;
	}

	public double getDistance() {
		return distance;
	}

	public Pic getSearchImage() {
		return searchImage;
	}

	public Pic getQueryImage() {
		return queryImage;
	}
	
	public boolean isSameCategory() {
		return queryImage.getCategory().equalsIgnoreCase(searchImage.getCategory());
	}

	@Override
	public int compareTo(ImagePair pic) {
		
		double d1 = distance;
		double d2 = pic.getDistance();	

		if( d1 < d2 ) 
			return -1;
		else if( d1 > d2 ) 
			return 1;
		else if (searchImage.getId() == pic.getSearchImage().getId())
			return 0;
		else 
			return 0;
	}
	
}
