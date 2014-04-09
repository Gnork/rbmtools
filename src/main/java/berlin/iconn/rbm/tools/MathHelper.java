package berlin.iconn.rbm.tools;

public final class MathHelper {

	public static int[][] rgbArrayToMultiDimension(int[] rgb, int imageWidth, int imageHeight) {
		int[][] result = new int[imageHeight][imageWidth];		
		for(int i = 0; i < imageHeight; i++) {
			for(int j = 0; j < imageWidth; j++) {
				result[i][j] = rgb[imageWidth * i + j];
			}
		}
		return result;
	}

	public static double[][][] RGBToYCbCr(int[][] rgb) {	
		double[][][] result = new double[3][rgb.length][rgb[0].length];
		int r, g, b;	
		for(int i = 0; i < rgb.length; i++){
			for(int j = 0; j < rgb[0].length; j++){
				r = (rgb[i][j] >> 16) & 255;
				g = (rgb[i][j] >> 8) & 255;
				b = rgb[i][j] & 255;			
				result[0][i][j] = 0.299 * r + 0.587 * g + 0.114 * b;
				result[1][i][j] = 0.5 * r - 0.41869 * g - 0.08131 * b + 128;
				result[2][i][j] = -0.16874 * r - 0.33126 * g + 0.5 * b + 128;
			}
		}
		return result;
	}

	public static double[][] twoDimDCT(double[][] input) {
		
		int h = input.length;
		int w = input[0].length;

		double[][] output = new double[h][w];

		double[] alf1 = new double[h];
		double[] alf2 = new double[w];

		alf1[0] = 1. / Math.sqrt(h);
		for (int k = 1; k < h; k++) {
			alf1[k] = Math.sqrt(2. / h);
		}

		alf2[0] = 1. / Math.sqrt(w);
		for (int l = 1; l < w; l++) {
			alf2[l] = Math.sqrt(2. / w);
		}

		double sum;
		for (int k = 0; k < h; k++) {
			for (int l = 0; l < w; l++) {
				sum = 0;
				for (int i = 0; i < h; i++) {
					for (int j = 0; j < w; j++) {
						sum += (input[i][j]) * Math.cos((Math.PI * (2 * i + 1) * k) / (2 * h)) * Math.cos((Math.PI * (2 * j + 1) * l) / (2 * w));
					}
				}
				output[k][l] = alf1[k] * alf2[l] * sum;
			}
		}
		return output;
	}
}
