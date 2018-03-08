package core.image_filters;

import ifaces.I_ColorScheme;
import lib_duke.ImageResource;
import lib_duke.Pixel;

public class GaussianBlur extends BaseFilter implements I_ColorScheme {

	private ImageResource in, out;
	private int borderG = Gaussian.BORDER_GAUS_5;

	public GaussianBlur(ImageResource in, ImageResource out, boolean w, boolean d, int... intArgs) {
		super(in.getWidth(), in.getHeight(), Gaussian.BORDER_GAUS_5, w, d, 5, intArgs);
		this.in = in;
		this.out = out;
	}

	@Override
	public void doYourThing() {

		int r, g, b;
		int matrixX, matrixY;
		int cumulR, cumulG, cumulB;
		Pixel pixelOut, matrixPixel;

		// matrix counters
		int countX;
		int countY;

		final int MAX = 255;

		for (int absX = widthFrom; absX < widthTo; absX++) {
			for (int absY = heightFrom; absY < heightTo; absY++) {

				pixelOut = out.getPixel(absX, absY);

				matrixX = 0;
				matrixY = 0;
				cumulR = 0;
				cumulG = 0;
				cumulB = 0;

				/*
				 * IF TO SLOW ... A Gaussian blur effect is typically generated
				 * by convolving an image with a kernel of Gaussian values. In
				 * practice, it is best to take advantage of the Gaussian blur’s
				 * separable property by dividing the process into two passes.
				 * In the first pass, a one-dimensional kernel is used to blur
				 * the image in only the horizontal or vertical direction. In
				 * the second pass, the same one-dimensional kernel is used to
				 * blur in the remaining direction. The resulting effect is the
				 * same as convolving with a two-dimensional kernel in a single
				 * pass, but requires fewer calculations.
				 * 
				 * https://en.wikipedia.org/wiki/Gaussian_blur
				 */

				// iterate kernel

				countX = 0;
				countY = 0;
				for (int absKernelX = absX - borderG; absKernelX < absX + borderG + 1; absKernelX++) {
					for (int absKernelY = absY - borderG; absKernelY < absY + borderG + 1; absKernelY++) {

						matrixX = countX;
						matrixY = countY;

						matrixPixel = in.getPixel(absKernelX, absKernelY);
						cumulR += (matrixPixel.getRed() * Gaussian.GAUS_KERNEL_5[matrixX][matrixY]);
						cumulG += (matrixPixel.getGreen() * Gaussian.GAUS_KERNEL_5[matrixX][matrixY]);
						cumulB += (matrixPixel.getBlue() * Gaussian.GAUS_KERNEL_5[matrixX][matrixY]);
						countY++;
					}
					countY = 0;
					countX++;
				}

				r = (int) (((double) cumulR) * Gaussian.NORMALIZE_GAUS_5);
				g = (int) (((double) cumulG) * Gaussian.NORMALIZE_GAUS_5);
				b = (int) (((double) cumulB) * Gaussian.NORMALIZE_GAUS_5);

				if (r > MAX || g > MAX || b > MAX) {
					System.out.println("CULPRIT R " + r);
					System.out.println("CULPRIT G " + g);
					System.out.println("CULPRIT B " + b);
					System.out.println("CULPRIT MAX " + MAX);
					throw new RuntimeException("MAX_VALUES_IN_GB");
				}

				pixelOut.setRed(r);
				pixelOut.setGreen(g);
				pixelOut.setBlue(b);

			}
		}
	}

	static class Gaussian {

		// --------------------------------------------------------

		@SuppressWarnings("unused")
		private static final int[][] GAUS_KERNEL_3 = new int[][] { { 2, 2, 2 }, { 2, 6, 2 }, { 2, 2, 2 } };
		@SuppressWarnings("unused")
		private static final double NORMALIZE_GAUS_3 = 1 / 22d;
		@SuppressWarnings("unused")
		private static final byte BORDER_GAUS_3 = 1;

		// --------------------------------------------------------

		private static final int[][] GAUS_KERNEL_5 = new int[][] { { 2, 4, 5, 4, 2 }, { 4, 9, 12, 9, 4 },
				{ 5, 12, 15, 12, 5 }, { 4, 9, 12, 9, 4 }, { 2, 4, 5, 4, 2 } };
		private static final double NORMALIZE_GAUS_5 = 1 / 159d;
		private static final byte BORDER_GAUS_5 = 2;

	}
}
