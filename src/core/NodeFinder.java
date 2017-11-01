package core;

import java.util.*;

import core.image_filters.JustCopy;
import core.utils.RoundIteratorOfPixels;
import ifaces.I_ColorScheme;
import lib_duke.ImageResource;
import lib_duke.Pixel;

public class NodeFinder implements I_ColorScheme {

    private ImageResource thresholded;
    private ImageResource skeletonized;
    private ArrayList < Node > nodes = new ArrayList < Node > ();
    private int width;
    private int height;
    private ImageResource noded;
    private int nmbOfNodes = 0;
    
    private int lookAheadAndBack;
    private int surfaceConstantInterval1_MinExcl;
    private int surfaceConstantInterval1_MaxIncl;
    private int surfaceConstantInterval2_MinExcl;
    private int surfaceConstantInterval2_MaxIncl;
	private int routableNeighbourghsConstant;
    
    @SuppressWarnings("unused")
	private int thresholded_lookAheadAndBack;
    @SuppressWarnings("unused")
	private int thresholded_surfaceConstantInterval1_MinExcl;
    @SuppressWarnings("unused")
	private int thresholded_surfaceConstantInterval1_MaxIncl;
    @SuppressWarnings("unused")
	private int thresholded_surfaceConstantInterval2_MinExcl;
    @SuppressWarnings("unused")
	private int thresholded_surfaceConstantInterval2_MaxIncl;
	@SuppressWarnings("unused")
	private int thresholded_routableNeighbourghsConstant;
    
	private int surfaceConstant1_1;
	private int surfaceConstant1_2;
	private int surfaceConstant2_1;
	private int surfaceConstant2_2;
	private int neighbourghsConstant;
    
	private final long shotId;

    private RoundIteratorOfPixels iteratorRound = new RoundIteratorOfPixels();
    private RecursiveClusterFinder rcf;

    //represents white pixels clustered around future node
    private HashSet < Pixel > allClusterAroundNode = new HashSet < Pixel > ();
    private final Runner myHandler;
    int borderSharpenStage;
    private double [] bounds;
    private boolean debug;
    private boolean visual;

    private static double spanLon = -1.0;
    private static double spanLat = -1.0;

    /**
     *
     */
    public NodeFinder(
    		ImageResource thresholded,
    		ImageResource skeletonized,
    		
    		int look,
    		int surface1,
    		int surface2,
    		int surface3,
    		int surface4,
    		int neighbours,
    		
    		int thresholded_look,
    		int thresholded_surface1,
    		int thresholded_surface2,
    		int thresholded_surface3,
    		int thresholded_surface4,
    		int thresholded_neighbours,
    		
    		Runner myHandler,
    		double [] bounds,
    		long shotId,
    		boolean debug,
    		boolean visual) {

        this.lookAheadAndBack = look;
        this.surfaceConstantInterval1_MinExcl = surface1;
        this.surfaceConstantInterval1_MaxIncl = surface2;
        this.surfaceConstantInterval2_MinExcl = surface3;
        this.surfaceConstantInterval2_MaxIncl = surface4;
        this.routableNeighbourghsConstant = neighbours;
        
        this.thresholded_lookAheadAndBack = thresholded_look;
        this.thresholded_surfaceConstantInterval1_MinExcl = thresholded_surface1;
        this.thresholded_surfaceConstantInterval1_MaxIncl = thresholded_surface2;
        this.thresholded_surfaceConstantInterval2_MinExcl = thresholded_surface3;
        this.thresholded_surfaceConstantInterval2_MaxIncl = thresholded_surface4;
        this.thresholded_routableNeighbourghsConstant = thresholded_neighbours;
        
        this.skeletonized = skeletonized;
        this.thresholded = thresholded;
        width = skeletonized.getWidth();
        height = skeletonized.getHeight();
        noded = new ImageResource(width, height);
        this.myHandler = myHandler;
        this.borderSharpenStage = myHandler.getBorderInSharpenStage();
        this.bounds = bounds;
        this.shotId = shotId;
        this.debug = debug;
        this.visual = visual;

        //0 lon east
        //1 lat north
        //2 lat south
        //3 lon west
        //4 lat center
        //5 lon center

        //TODO code duplicity
        double spanLonNow = bounds[0] - bounds [3];
        if(spanLon == -1.0) spanLon = spanLonNow;
        else if(spanLon != spanLonNow){
        	System.err.println("------------------------------span discrepancy Lon");
        	System.err.println("spanLon " + spanLon + " now " + spanLonNow);
        	System.err.println("dif " + (spanLon - spanLonNow));
        	spanLon = spanLonNow;
        }
        double spanLatNow = bounds[1] - bounds [2];
        if(spanLat == -1.0) spanLat = spanLatNow;
        else if(spanLat != spanLatNow){
        	System.err.println("------------------------------span discrepancy Lat");
        	System.err.println("spanLat " + spanLat + " now " + spanLatNow);
        	System.err.println("dif " + (spanLat - spanLatNow));
        	spanLat = spanLatNow;
        }

        if(bounds.length != 6) throw new RuntimeException("Node finder bounds length.");
      
        //TODO is it necessary?
        JustCopy copy = new JustCopy(skeletonized, noded,
        		myHandler.getBorderInSharpenStage(),
        		true, debug, -1,-1,-1,-1, myHandler.getBorderInSharpenStage());
        copy.doYourThing();

        if(debug){
            System.out.println("NodeFinder");
            System.out.println("______________________________");
            for (double d : this.bounds) System.out.println(d);
            System.out.println("______________________________");
        }
    }

    /**
     *
     */
    public void findNodes() {

    	/*
	    surfaceConstant1_1 = thresholded_surfaceConstantInterval1_MinExcl;
	    surfaceConstant1_2 = thresholded_surfaceConstantInterval1_MaxIncl;
	    surfaceConstant2_1 = thresholded_surfaceConstantInterval2_MinExcl; 
	    surfaceConstant2_2 = thresholded_surfaceConstantInterval2_MaxIncl;
	    neighbourghsConstant = thresholded_routableNeighbourghsConstant;
	    
    	System.out.println("surfaceConstant1_1 " + surfaceConstant1_1);
    	System.out.println("surfaceConstant1_2 " + surfaceConstant1_2);
    	System.out.println("surfaceConstant2_1 " + surfaceConstant2_1);
    	System.out.println("surfaceConstant2_2 " + surfaceConstant2_2);
    	System.out.println("thresholded_lookAheadAndBack " + thresholded_lookAheadAndBack);
    	
    	detectSalientAreas(thresholded, thresholded_lookAheadAndBack, false);
    	*/
    	
	    surfaceConstant1_1 = surfaceConstantInterval1_MinExcl;
	    surfaceConstant1_2 = surfaceConstantInterval1_MaxIncl;
	    surfaceConstant2_1 = surfaceConstantInterval2_MinExcl; 
	    surfaceConstant2_2 = surfaceConstantInterval2_MaxIncl;
	    neighbourghsConstant = routableNeighbourghsConstant;
	    
    	System.out.println("surfaceConstant1_1 " + surfaceConstant1_1);
    	System.out.println("surfaceConstant1_2 " + surfaceConstant1_2);
    	System.out.println("surfaceConstant2_1 " + surfaceConstant2_1);
    	System.out.println("surfaceConstant2_2 " + surfaceConstant2_2);
    	System.out.println("lookAheadAndBack " + lookAheadAndBack);
	    
	    detectSalientAreas(false);// boolean compare against thresholded?
	    
        if(visual) {
        	noded.draw();
        	//thresholded.draw();
        	Pause.pause(2000);
            //SALIENT SALIENT
        	//System.out.println("DRAWING SALIENT AREA");
            //salientArea.draw();
        }
        

        //
        rcf = new RecursiveClusterFinder(noded, whiteScheme[0], whiteScheme[1], whiteScheme[2]);
        //

        int x, y;
        double lon, lat;

        double dLon = Math.abs(bounds [0] - bounds [3]);
        double dLat = Math.abs(bounds [1] - bounds [2]);

        if(debug){
        	System.out.println("dLon " + dLon);
        	System.out.println("dLat " + dLat);
        }

        for (Pixel pOfNoded: noded.pixels()) { //1

            if (isSetToClusterAround(pOfNoded)) { //2 white check only

                allClusterAroundNode.clear();
                rcf.resetAllCluster();

                //now define recursively a node object as a small bitmap

                setClustered(pOfNoded); //as this emerges from recursion we have filled allClusterAroundNode

                if (debug && nmbOfNodes % 20 == 0) printAllClustered(allClusterAroundNode); //size of print REDUCED

                int neighboursToFindMax;
                int maxNeighbours = 0;

                int sumX = 0;
                int sumY = 0;
                int centerGravityX = 0;
                int centerGravityY = 0;

                ArrayList < Node > maximusNodes = new ArrayList < Node > ();

                for (Pixel pChecked: allClusterAroundNode) { //find int max

                    neighboursToFindMax = getNumberOfSurrWhites(pChecked, noded);

                    if (neighboursToFindMax > maxNeighbours) maxNeighbours = neighboursToFindMax;

                }

                for (Pixel maybeMaximus: allClusterAroundNode) {

                    if (getNumberOfSurrWhites(maybeMaximus, noded) == maxNeighbours) {

                    	//bounds

                        //0 lon east
                        //1 lat north
                        //2 lat south
                        //3 lon west
                        //4 lat center
                        //5 lon center

                    	x = maybeMaximus.getX();
                    	y = maybeMaximus.getY();

                    	lon = bounds [3] + (   ( (double) x / ((double) (width - 1)) ) * dLon   );
                    	lat = bounds [1] - (   ( (double) y / ((double) (height - 1)) ) * dLat   );

                    	//the only instantiation of Node in the project
                        Node node = new Node(
                        		x,
                        		y,
                        		lon,
                        		lat,
                        		myHandler.incrAndGetId(),
                        		shotId
                        		);

                        maximusNodes.add(node);

                        //find average X Y for this cluster of maximuses
                        sumX += node.getX();
                        sumY += node.getY();

                    }
                }

                out: {
                    @SuppressWarnings("unused")
                    int closestXsoFar = Integer.MAX_VALUE;
                    @SuppressWarnings("unused")
                    int closestYsoFar = Integer.MAX_VALUE;
                    @SuppressWarnings("unused")
                    int xToOut = 0;
                    @SuppressWarnings("unused")
                    int yToOut = 0;

                    centerGravityX = sumX / maximusNodes.size();
                    centerGravityY = sumY / maximusNodes.size();

                    double minDist = Double.MAX_VALUE;

                    //select one of maximusNodes and add it to nodes;

                    for (Node maybeClosest: maximusNodes) {

                        //we do have (among maximusNodes) a node with exactly avg x y values
                    	
                        if (maybeClosest.getX() == centerGravityX && maybeClosest.getY() == centerGravityY) {
                            nodes.add(maybeClosest);
                            nmbOfNodes++;
                            if(debug) System.out.println(maybeClosest.toString());
                            break out;
                        }

                        //calculate dist to center of gravity and set it to node POJO

                        int deltaX = Math.abs(maybeClosest.getX() - centerGravityX);
                        int deltaY = Math.abs(maybeClosest.getY() - centerGravityY);
                        double dist = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

                        maybeClosest.setDstToCenter(dist);
                        if (dist < minDist) minDist = dist;

                    }

                    for (Node possiblyClosestNode: maximusNodes) {

                        if (possiblyClosestNode.getDstToCenter() == minDist) {
                            nodes.add(possiblyClosestNode);
                            xToOut = possiblyClosestNode.getX();
                            yToOut = possiblyClosestNode.getY();
                            if(debug) System.out.println(possiblyClosestNode.toString());
                            break;
                        }
                    }

                    nmbOfNodes++;
                } //out

                for (Pixel toRed: allClusterAroundNode) {
                    setRed(toRed);
                }
            } //2
        } //1

        //now set pixels white
        Pixel px;
        for (Node n: nodes) {
            px = noded.getPixel(n.getX(), n.getY());
            setWhite(px);
        }
    }

    /**
     * 
     * @param image
     * @param lookBothDir
     */
    private void detectSalientAreas(boolean testAgainstThresholded){
        
    	System.out.println("border sharpen stage " + myHandler.getBorderInSharpenStage());
        iteratorRound.setImageResource(skeletonized);
        Set <Pixel> toBeWhite = new HashSet<Pixel>();
        
    	for (int x = lookAheadAndBack; x < width - lookAheadAndBack; x++) {
            for (int y = lookAheadAndBack; y < height - lookAheadAndBack; y++) {
            	
            	Pixel p = skeletonized.getPixel(x, y);
            	if( !isForeground(p) ) continue;
            	
            	iteratorRound.setPixelToCheckAround(p);
            	
            	int count = 0;
            	for (Pixel pIt : iteratorRound){
            		if(isForeground(pIt))count ++;
            	}
            	
                //do nothing based on heuristics affected by borders
            	
            	if(count == 1 && isOkBorders(x,y))
            		toBeWhite.add(noded.getPixel(x, y));
            }
        }
    	
        Pixel p = null;
        Pixel pIn = null;
        
        int surfaceArea = 0;
        int routableNeighbours = 0;

        int minSurface = Integer.MAX_VALUE;
        int maxSurface = Integer.MIN_VALUE;
        //
        //finds salient pixels that probably belong to a region of
        //interest in terms of future node.
        //
        for (int x = lookAheadAndBack; x < width - lookAheadAndBack; x++) {
            for (int y = lookAheadAndBack; y < height - lookAheadAndBack; y++) {

            	if(testAgainstThresholded && !isSalientPixInThresholded(x,y)) continue;
            	
                p = noded.getPixel(x, y);

                if (isForeground(p)) {

                    for (int xIn = x - lookAheadAndBack; xIn < x + lookAheadAndBack + 1; xIn++) {
                        for (int yIn = y - lookAheadAndBack; yIn < y + lookAheadAndBack + 1; yIn++) {

                            pIn = noded.getPixel(xIn, yIn);

                            if (isForeground(pIn)) {

                            	routableNeighbours ++;
                                iteratorRound.setPixelToCheckAround(pIn);

                                for (Pixel p1: iteratorRound) {
                                    if (isBackground(p1)) surfaceArea ++;
                                }
                            }
                        }
                    }
                 
                    if ( evaluateAgainstConstants(surfaceArea, routableNeighbours)){
                    	
                        //do nothing based on heuristics affected by borders
                        
                        if(isOkBorders(x,y)){
                        	toBeWhite.add(noded.getPixel(x, y));
                        }
                        
                    }
                    
                    if(surfaceArea > maxSurface) maxSurface = surfaceArea;
                    if(surfaceArea < minSurface) minSurface = surfaceArea;
                    
                    surfaceArea = 0;
                    routableNeighbours = 0;
                }
            }
        }
        
        for(Pixel px : toBeWhite){
        	setWhite(px);
        }
        
        if(visual || debug) System.out.println("Surface min max " + minSurface + " " + maxSurface);
    }

    /**
     * 
     */
	private boolean evaluateAgainstConstants(int surface, int routableNeighbours ){
    	
        boolean firstInterval = (surface > surfaceConstant1_1 &&
                  surface <= surfaceConstant1_2);
        boolean secondInterval = (surface > surfaceConstant2_1 &&
                  surface <= surfaceConstant2_2);
        boolean neighbours = routableNeighbours > neighbourghsConstant ;
    	
    	return (firstInterval || secondInterval) && neighbours;
    }
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isOkBorders(int x, int y){
		boolean okX = (x >= borderSharpenStage + lookAheadAndBack) && 
				(x < width - borderSharpenStage - lookAheadAndBack);
		boolean okY = (y >= borderSharpenStage + lookAheadAndBack) && 
				(y < height - borderSharpenStage - lookAheadAndBack);
		return okX && okY;
	}
    
    /**
     * 
     * @param x
     * @param y
     * @return
     */
	private boolean isSalientPixInThresholded(int x, int y){
    	Pixel salientInTh = thresholded.getPixel(x, y);
    	if(isSetToClusterAround(salientInTh)) return true; else return false;
    }
    
    /**
     * loads recursively set of white pixels into neighbours and then allClusterAroundNode
     */
    private void setClustered(Pixel p) {

        rcf.setCluster(p);
        this.allClusterAroundNode = rcf.getAllCluster();

    }

    /**
     * checks if p is set to white scheme
     */
    private boolean isSetToClusterAround(Pixel p) {

        if (p.getRed() == whiteScheme[0] && p.getGreen() == whiteScheme[1] && p.getBlue() == whiteScheme[2]) return true;
        return false;
    }

    /**
     *
     */
    private int getNumberOfSurrWhites(Pixel p, ImageResource ir) {

        int neighbours = 0;

        iteratorRound.setImageResource(ir);
        iteratorRound.setPixelToCheckAround(p);

        for (Pixel myPx: iteratorRound) {

            if (myPx.getRed() == whiteScheme[0] && myPx.getGreen() == whiteScheme[1] && myPx.getBlue() == whiteScheme[2]) neighbours++;

        }

        return neighbours;
    }
    
    /**
     * prints simplistic visualisation of clusters
     */
    private void printAllClustered(HashSet < Pixel > cluster) {

        System.out.println("--------------------------");

        boolean[][] clusterA = new boolean[(lookAheadAndBack * 2 + 1)][(lookAheadAndBack * 2 + 1)];

        StringBuilder sb = new StringBuilder();

        int minX = 100000;
        int minY = 100000;

        for (Pixel p: cluster) {

            if (p.getX() < minX) minX = p.getX();
            if (p.getY() < minY) minY = p.getY();

        }

        int xToVis = 0;
        int yToVis = 0;

        for (Pixel p: cluster) {

            xToVis = p.getX() - minX;
            xToVis = xToVis >= (lookAheadAndBack * 2 + 1) ? (lookAheadAndBack * 2) : xToVis;
            yToVis = p.getY() - minY;
            yToVis = yToVis >= (lookAheadAndBack * 2 + 1) ? (lookAheadAndBack * 2) : yToVis;

            clusterA[xToVis][yToVis] = true;

        }

        for (int x = 0; x < clusterA.length; x++) {
            for (int y = 0; y < clusterA.length; y++) {

                if (clusterA[x][y]) sb.append("X");
                else sb.append("_");

            }
            System.out.println(sb.toString());
            sb = new StringBuilder();
        }
    }

    public ImageResource getNodedImage() {
        return noded;
    }
    public ArrayList < Node > getNodes() {
        return nodes;
    }
    private boolean isForeground(Pixel pIn){
    	return pIn.getRed() == redScheme[0] && pIn.getGreen() == redScheme[1] && pIn.getBlue() == redScheme[2];
    }
    private boolean isBackground(Pixel pIn){
    	return !isForeground(pIn);
    }
    private void setRed(Pixel p) {
        p.setRed(redScheme[0]);
        p.setGreen(redScheme[1]);
        p.setBlue(redScheme[2]);
    }
    private void setWhite(Pixel p) {
        p.setRed(whiteScheme[0]);
        p.setGreen(whiteScheme[1]);
        p.setBlue(whiteScheme[2]);
    }
    @SuppressWarnings("unused")
    private void setYellow(Pixel p) {
        p.setRed(yellowScheme[0]);
        p.setGreen(yellowScheme[1]);
        p.setBlue(yellowScheme[2]);
    }
}
