package core.image_filters;

import java.util.HashMap;
import java.util.Map;

import core.image_filters.filter_utils.BorderWatch;
import core.image_filters.filter_utils.ChunksNotMessedAssertion;
import core.image_filters.filter_utils.ChunksOrWhole;
import ifaces.IImageProcesor;
import lib_duke.AugmentedPixel;
import lib_duke.ImageResource;
import lib_duke.Pixel;

public class CannyDetect implements IImageProcesor {

    private Map < Pixel, AugmentedPixel > toAugmented = new HashMap < Pixel, AugmentedPixel > ();

    private int borderSharpenStage;
    private boolean wholeImage;
    private boolean debug;
    private int [] args;//first 4 always chunk, maybe dummy

    public CannyDetect(boolean w, boolean d, int...intArgs) {
        this.wholeImage = w;
        this.debug = d;
        if (intArgs.length != 5) throw new RuntimeException("Arguments length"); 
        this.args = intArgs;
        borderSharpenStage = intArgs[4];
        if(borderSharpenStage < Sobel.KERNEL_BORDER) throw new RuntimeException("kernel mess");
        if (debug) for (int i: intArgs) System.out.println("Args in Sharpen: " + i);
    }

    static class Sobel {
    	private static final int KERNEL_BORDER = 1;
        private static final int[][] VER_KERNEL = new int[][] {
            {
                1,0,-1
            }, {
                2,0,-2
            }, {
                1,0,-1
            }
        };
        private static final int[][] HOR_KERNEL = new int[][] {
            {
                1,2,1
            }, {
                0,0,0
            }, {
            	-1,-2,-1
            }
        };
    }

    @Override
    public void doYourThing(ImageResource in , ImageResource out) {

        int [] values = ChunksOrWhole.decide(args, wholeImage, in.getWidth(), in.getHeight());
    	
        final int xSize = in .getWidth();
        final int ySize = in .getHeight();
        final boolean halt = ChunksNotMessedAssertion.assertOK(xSize, ySize, values, borderSharpenStage);
        if (halt) throw new RuntimeException("chunks messed");
        
        BorderWatch border = new BorderWatch(values, borderSharpenStage, xSize, ySize);
        
        int widthFrom = border.getWidthFrom();
        int widthTo = border.getWidthTo();
        int heightFrom = border.getHeightFrom();
        int heightTo = border.getHeightTo();
        int borderS = Sobel.KERNEL_BORDER;
        
        int countX, countY;
        
        for (int absX = widthFrom; absX < widthTo; absX++) {
            for (int absY = heightFrom; absY < heightTo; absY++) {
                //SobelOperator
            	//iterate kernel

                countX = 0;
                countY = 0;
                for (int absKernelX = absX - borderS; absKernelX < absX + borderS + 1; absKernelX++) {
                    for (int absKernelY = absY - borderS; absKernelY < absY + borderS + 1; absKernelY++) {
            	    //System.out.println("ITER KERNEL " + absKernelX + " " +absKernelY + " count " + countX + " " + countY);
            	    
                    	
                    	
                    countY++;
                    }
                    countY = 0;
                    countX++;
                }
                //System.out.println("_______________________________________________");
            }
        }
    }
}
