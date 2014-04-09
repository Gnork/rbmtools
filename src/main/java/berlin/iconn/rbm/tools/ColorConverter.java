package berlin.iconn.rbm.tools;

public class ColorConverter {

	public enum ColorSpace { RGB, AdvYCbCr, Genetic };
	
	public static double[][] convertRGBTo(double[][] rgbColors, ColorSpace to) {
		double[][] result = new double[0][0];
		
		switch (to) {
			case AdvYCbCr:
				result = rgb2AdvYCbCr(rgbColors);
				break;
	
			default:
				result = rgbColors;
				break;
		}
		
		return result;
	}
	
	
	/**
	 * YCbCr Farbraum verkrümmt von Kai
	 *  
	 * @param rgb
	 * @return
	 */
	public static double[][] rgb2AdvYCbCr(double[][] rgbs) {
		double[][] result = new double[rgbs.length][3];
		for (int i = 0; i < rgbs.length; i++)
			result[i] = rgb2AdvYCbCr(rgbs[i][0], rgbs[i][1], rgbs[i][2]);
		return result;
	}
	
	/**
	 * YCbCr Farbraum verkrümmt von Kai
	 *  
	 * @param rgb
	 * @return
	 */
	public static double[] rgb2AdvYCbCr(double[] rgb) {
		return rgb2AdvYCbCr(rgb[0], rgb[1], rgb[2]);
	}
	
	/**
	 * YCbCr Farbraum verkrümmt von Kai
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static double[] rgb2AdvYCbCr(double r, double g, double b) {
		double[] YCbCr = new double[3];
		float w1 = 0.25f; 
		float w2 = 1.446491f;  
		YCbCr[0] = w1*(r+ 2*g + b) - 128;
		YCbCr[1] = w2*(-r + 2*g -b); 
		YCbCr[2] = w2*(r - b); 
		return YCbCr;
	}	
}
