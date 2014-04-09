package berlin.iconn.rbm.tools;

public class ColorMetric {

	private static DistanceInterface distInterface;
	
	public static void setDistanceInterface(DistanceInterface distanceInterface) {
		distInterface = distanceInterface;
	}
	
	public static double getL1Distance(double[] rgb1, double[] rgb2) {
		double result = 0;
		for (int i = 0; i < rgb1.length; i++)
			result += Math.abs(rgb1[i] - rgb2[i]);
		return result;
	}
	
	public static double getL4Distance(double[] rgb1, double[] rgb2) {
		double result = 0;
		for (int i = 0; i < rgb1.length; i++) {
			double diff = rgb1[i] - rgb2[i];
			result += diff * diff * diff * diff;
		}
		return result;
	}
	
	public static double getL1Distance(double r1, double g1, double b1, double r2, double g2, double b2) {
		return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
	}
	
	public static double getL2Distance(double[] rgb1, double[] rgb2) {
		double result = 0;
		for (int i = 0; i < rgb1.length; i++)
			result += (rgb1[i] - rgb2[i])*(rgb1[i] - rgb2[i]);
		return Math.sqrt(result);
	}
	
	public static double getL2Distance(double r1, double g1, double b1, double r2, double g2, double b2) {
		return Math.sqrt((r1 - r2)*(r1 - r2) + (g1 - g2)*(g1 - g2) + (b1 - b2)*(b1 - b2));
	}

	public static double getGeneticSolutionDistance(double[] fv1, double[] fv2) {
		return distInterface.getDistanceBetween(fv1, fv2);
	}
}
