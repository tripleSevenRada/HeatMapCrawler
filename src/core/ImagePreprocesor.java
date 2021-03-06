package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import core.image_filters.CannyDetect;
import core.image_filters.EdgeHighlight;
import core.image_filters.GaussianBlur;
import core.image_filters.JustCopy;
import core.image_filters.Threshold;
import core.image_filters.filter_utils.MapMerge;
import core.image_morpho_transform.DistanceMapSkeleton;
import ifaces.I_ColorScheme;
import ifaces.I_ImageProcesor;
import lib_duke.AugmentedPixel;
import lib_duke.ImageResource;
import lib_duke.Pixel;

public class ImagePreprocesor implements I_ColorScheme {

	private final ImageResource inputImageResource;
	private final ImageResource procesedImageResourceStage1;
	private ImageResource procesedImageResourceStage2;
	private ImageResource procesedImageResourceStage3;// once no visualization
														// kept is needed, make
														// a cyclic queue
	@SuppressWarnings("unused")
	private final ImageResource procesedImageResourceStage4 = null;
	@SuppressWarnings("unused")
	private final ImageResource procesedImageResourceStage5 = null;

	private final int devToMakeItValidRoutable; // 80;
	private final int borderAtSharpenStage;

	private boolean visual, debug;
	private final List<Map<Pixel, AugmentedPixel>> chopsToAugmentedList = new ArrayList<Map<Pixel, AugmentedPixel>>();

	public ImagePreprocesor(int deviation, int border, boolean visual, boolean debug, ImageResource im) {

		this.devToMakeItValidRoutable = deviation;
		this.borderAtSharpenStage = border;
		this.inputImageResource = im;

		int w = inputImageResource.getWidth();
		int h = inputImageResource.getHeight();

		procesedImageResourceStage1 = new ImageResource(w, h);
		procesedImageResourceStage2 = new ImageResource(w, h);

		this.visual = visual;
		this.debug = debug;
	}

	public int getX() {
		return inputImageResource.getWidth();
	}

	public int getY() {
		return inputImageResource.getHeight();
	}

	/**
	 * 
	 * @param widthFrom
	 * @param widthTo
	 * @param heightFrom
	 * @param heightTo
	 * @param wholePicture
	 *            - so no params needed, only dummies
	 */
	public void procesSharpen(int widthFrom, int widthTo, int heightFrom, int heightTo, boolean wholePicture) {

		I_ImageProcesor sharpen = new Threshold(inputImageResource, procesedImageResourceStage1, borderAtSharpenStage,
				wholePicture, debug, widthFrom, widthTo, heightFrom, heightTo, borderAtSharpenStage,
				devToMakeItValidRoutable);

		debugPrint("procesSharpen");
		sharpen.doYourThing();

	}

	/**
	 * 
	 * @param widthFrom
	 * @param widthTo
	 * @param heightFrom
	 * @param heightTo
	 * @param wholePicture
	 */
	public void procesGaussian(int widthFrom, int widthTo, int heightFrom, int heightTo, boolean wholePicture) {

		I_ImageProcesor gaussian = new GaussianBlur(procesedImageResourceStage1, procesedImageResourceStage2,
				wholePicture, debug, widthFrom, widthTo, heightFrom, heightTo, borderAtSharpenStage);
		debugPrint("procesGaussian");
		gaussian.doYourThing();
	}

	CannyDetect canny; // implements IImageProcesor

	/**
	 * 
	 * @param widthFrom
	 * @param widthTo
	 * @param heightFrom
	 * @param heightTo
	 * @param wholePicture
	 */
	public void procesCanny(int widthFrom, int widthTo, int heightFrom, int heightTo, boolean wholePicture) {

		canny = new CannyDetect(procesedImageResourceStage2, this, wholePicture, debug, widthFrom, widthTo, heightFrom,
				heightTo, borderAtSharpenStage);
		debugPrint("procesCanny");
		canny.doYourThing();
	}

	Map<Pixel, AugmentedPixel> toAugmented;

	/**
	 * 
	 * @param widthFrom
	 * @param widthTo
	 * @param heightFrom
	 * @param heightTo
	 * @param wholePicture
	 */
	public void procesHighlight(int widthFrom, int widthTo, int heightFrom, int heightTo, boolean wholePicture) {

		if (canny == null)
			throw new RuntimeException("INVARIANT 1");
		if (toAugmented == null)
			toAugmented = this.getToAugmented();
		if (toAugmented == null || toAugmented.size() == 0)
			throw new RuntimeException("INVARIANT 2");

		I_ImageProcesor highlight = new EdgeHighlight(procesedImageResourceStage2, procesedImageResourceStage3,
				borderAtSharpenStage, toAugmented, wholePicture, debug, widthFrom, widthTo, heightFrom, heightTo,
				borderAtSharpenStage);
		debugPrint("procesHighlight");
		highlight.doYourThing();
	}

	/**
	 * 
	 * @param xFromIncl
	 * @param xToExcl
	 * @param yFromIncl
	 * @param yToExcl
	 * @param whole
	 */
	public void procesJustCopy(int xFromIncl, int xToExcl, int yFromIncl, int yToExcl, boolean whole) {
		I_ImageProcesor copy = new JustCopy(procesedImageResourceStage1, procesedImageResourceStage2,
				borderAtSharpenStage, whole, debug, xFromIncl, xToExcl, yFromIncl, yToExcl, borderAtSharpenStage);
		copy.doYourThing();
	}

	/**
	 * 
	 * @param xFromIncl
	 * @param xToExcl
	 * @param yFromIncl
	 * @param yToExcl
	 * @param whole
	 */
	public void procesSkeleton(int xFromIncl, int xToExcl, int yFromIncl, int yToExcl, boolean whole) {

		/*
		 * I_ImageProcesor skeleton = new Skeleton(
		 * 
		 * procesedImageResourceStage2, borderAtSharpenStage, whole, debug, xFromIncl,
		 * xToExcl, yFromIncl, yToExcl, borderAtSharpenStage
		 * 
		 * );
		 */

		I_ImageProcesor skeleton = new DistanceMapSkeleton(

				procesedImageResourceStage2, borderAtSharpenStage, whole, debug, xFromIncl, xToExcl, yFromIncl, yToExcl,
				borderAtSharpenStage

		);

		skeleton.doYourThing();
	}

	/**
	* 
	*/
	public void invokeSequencialQueue() {
		procesSkeleton(-1, -1, -1, -1, true);
	}

	public void addMap(Map<Pixel, AugmentedPixel> chop) {
		chopsToAugmentedList.add(chop);
	}

	private Map<Pixel, AugmentedPixel> getToAugmented() {
		MapMerge<Pixel, AugmentedPixel> merge = new MapMerge<Pixel, AugmentedPixel>(chopsToAugmentedList);
		return merge.getMerged();
	}

	/**
	*
	*/
	public ImageResource getProcesedStage() {
		//
		// test save
		// procesedImageResourceStage3.draw();
		// procesedImageResourceStage2.saveAs();
		//
		//
		return procesedImageResourceStage1;
	}

	/**
	 *
	 */
	public ImageResource getProcesed() {
		//
		// test save
		// procesedImageResourceStage3.draw();
		// procesedImageResourceStage2.saveAs();
		//
		//
		return procesedImageResourceStage2;
	}

	private void debugPrint(String job) {
		if (debug || visual)
			System.out.println(this.getClass().toString() + " call " + job);
	}
}