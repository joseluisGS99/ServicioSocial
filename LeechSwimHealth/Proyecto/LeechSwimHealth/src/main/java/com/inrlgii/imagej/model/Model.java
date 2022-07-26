package com.inrlgii.imagej.model;

import java.awt.Color;
import java.util.LinkedList;

import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import com.inrlgii.imagej.controller.Point;

public class Model {
	
	private ImagePlus image;
	private ImageProcessor ip;
	private LinkedList<Point> points;
	private LinkedList<Point> segment;
	private int width;
	private int height;
	
	
	public Model(ImagePlus image) {
		image.getProcessor().invert();
		this.image = image;
		this.ip = this.image.getProcessor();
		this.points = new LinkedList<Point>();
		this.segment = new LinkedList<Point>();
		this.width = image.getWidth();
		this.height = image.getHeight();
		run();
	}
	
	private void run() {
		// If you want to view the image changes, uncomment next line
		// image.show();
		
		// We search all black pixels
		find(0);
		
		// We erase pixels with value 120
		depeckle(120);
		
		// We paint the neighbours from black edges
		dilate();
		
		// We erase the pixels that isn't black
		clearImage(0);
		
		// We search the edges of the leech
		edges();
		
		// We erase the small black pixels zones
		depeckle(0);
		depeckle(0);
		
		// We erase the pixels that isn't 120 value
		clearImage(120);
		
		// We look for the segment to draw from the furthest points of the leech
		drawLines();
		
		// We look all points of the segment to save into a list
		searchSegment();	
		
	}
	
	/**
	 * Method that searches among all the pixels of the image only those that contain more than two neighbors
	 * with a scale determined by "value" variable and paints the rest in white
	 * @param value Grayscale value (0 -> 255)
	 */
	private void depeckle(int value) {
		byte[] pixels = (byte[]) ip.getPixels();
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++) {
				if(vecinos(x,y,value) <= 2) {
					pixels[x + y * width] = -1;
					image.updateAndDraw();
				}
			}
	}
	
	/**
	 * Method that searches for pixels with a scale determined by pixel Value and that these have a minimum number of 6 neighbors
	 * @param pixelValue Scale value of the pixel, it's between 0 and 255
	 */
	private void find(int pixelValue) {
		byte[] pixels = (byte[]) ip.getPixels();
		
		for(int y = 0; y< ip.getHeight(); y++)
			for(int x =0 ; x < ip.getWidth(); x++) {
				byte pixel = pixels[x + y * width];
				if(pixel == pixelValue && vecinos(x,y, 0) >= 6) {
					ip.setColor(120);
					ip.drawPixel(x, y);
					points.add(new Point(x,y));
				}
			}
	}
	
	/**
	 * Method that searches for pixels determined by a scale determined by the user and paints the rest white
	 * @param search Scale to search with a value between 0 and 255
	 */
	private void clearImage(int search) {
		byte[] pixels = (byte[]) image.getProcessor().getPixels();
		
		for(int i = 0; i < pixels.length; i++)
			if(pixels[i] != search)
				pixels[i] = -1;
		
	}
	
	/**
	 * Method that search leech edges and draw it gray at scale 120 and reload the list of points
	 * Busca las aristas de la silueta de la sanguijuela, las pinta de gris a escala 120 y 
	 * actualiza la lista points para que solo contenga a estos pixeles  
	 */
	private void edges() {
		byte[] pixels = (byte[])ip.getPixels();
		LinkedList<Point> newPoints = new LinkedList<>();
		System.out.println(image);
		for(int x = 0; x < width; x++) {
			LinkedList<Point> edges = new LinkedList<>();
			for(int y = 0; y< height-1; y++) {
				byte value = pixels[x + y * width];
				byte next = pixels[x + (y+1) * width];
				if(value == -1 && next == 0)
					edges.add(new Point(x, y+1));
				if(value == 0 && next == -1)
					edges.add(new Point(x,y));
			}
			
			for(Point p: edges) {
				ip.setColor(120);
				ip.drawPixel(p.x, p.y);
			}
			newPoints.addAll(edges);
		}
		
		System.out.println(newPoints.size());
		points = newPoints;
		image.updateAndDraw();
	}
	
	/**
	 * Method that takes the pixels that represents the leech, paint all their neighbors and add them to the list of points.
	 */
	private void dilate() {
		LinkedList<Point> pointsE = new LinkedList<>();
		for(Point p : points) {
			pointsE.addAll(drawNeiborhoods(p));
			image.updateAndDraw();
		}
		
		points.addAll(pointsE);
	}
	
	
	/**
	 * Method that paints black all neighbors from determined pixel 
	 * @param p Point that represents the pixel
	 * @return List with the pixels drawn black
	 */
	private LinkedList<Point> drawNeiborhoods(Point p) {
		LinkedList<Point> pointsNew = new LinkedList<>();
		ip.setColor(0);
		for(int j = p.y-1; j < p.y +2 ; j++) {
			for(int i = p.x - 1; i < p.x + 2; i++) {
				ip.drawPixel(i, j);
				pointsNew.add(new Point(i,j));
			}
		}
		return pointsNew;
	}
	
	
	
	/**
	 * Method that finds the farthest points of the leech's edges and draws a segment between them.
	 */
	private void drawLines() {
		ip.setColor(80);
		Point p1 = new Point(0,0);
		Point p2 = new Point(0,0);
		double len = 0;
		for(Point p: points) {
			for(int i = points.indexOf(p); i < points.size(); i++) {
				Point l = points.get(i);
				if(vecinos(l.x,l.y, 120) != 0) {
					double length = Math.sqrt(Math.pow((l.x - p.x), 2) + Math.pow((l.y-p.y), 2));
					if(length > len) {
						len = length;
						p1 = p;
						p2 = l;
					}					
				}
			}
		}
		
		ip.drawLine(p1.x, p1.y, p2.x, p2.y);
		image.updateAndDraw();	
		
	}
	
	/**
	 * Method that search the points which will are in the list of the segment and add them in the list.
	 */
	private void searchSegment() {
		byte[] pixels = (byte[]) ip.getPixels();
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++) {
				if(pixels[x + y * width] == 80) {
					segment.add(new Point(x,y, "segment"));
					pixels[x + y * width] = -1;					// Comentar esta linea de codigo si se desea observar el segmento entre los puntos mas lejandos de la silueta de la sanguijuela
				}
			}		
	}
	
	/**
	 * Method that search the pixels with value 120 and angle determined beginning by a point.
	 * @param p Starting point 
	 * @param angle	Angle determined to search
	 * @return Distance between the starting point and the point finded, in other case the method returned 0.
	 */
	private int getDistance(Point p, int angle) {
		int x = p.x;
		int y = p.y;
		int count = 0;
		byte[] pixels = (byte[]) ip.getPixels();
		if(angle == 45) {
			while(x < width && y > 0) {
				if(pixels[x + y * width] == 120) {
					return count;
				}else {
					x = x +1;
					y = y -1;
					count++;
				}
			}
		}else if(angle == 90) {
			while(y > 0) {
				if(pixels[x + y * width] == 120) {
					return count;
				}else {
					y = y -1;
					count++;
				}
			}
		}else if(angle == 135) {
			while(x > 0 && y > 0) {
				if(pixels[x + y * width] == 120) {
					return count;
				}else {
					x = x - 1;
					y = y - 1;
					count++;
				}
			}
		}else if(angle == 180) {
			while(x > 0) {
				if(pixels[x + y * width] == 120) {
					return count;
				}else {
					x = x - 1;
					count++;
				}
			}
		}else if(angle == 225) {
			while(x > 0 && y > 0) {
				if(pixels[x + y * width] == 120) {
					return count;
				}else {
					x = x - 1;
					y = y - 1;
					count++;
				}
			}
		}else if(angle == 270) {
			while(y < height) {
				if(pixels[x + y * width] == 120) {
					return count;
				}else {
					y = y + 1;
					count++;
				}
			}
		}else if(angle == 315) {
			while(x < width && y < height) {
				if(pixels[x + y * width] == 120) {
					return count;
				}else {
					x = x + 1;
					y = y + 1;
					count++;
				}
			}
		}
		return 0;
	}

	/**
	 * Method that search and draw all the possible circles generated beginning the segment to the leech's edges
	 * and determined if the leech's swimming is correctly or not. 
	 */
	public boolean search() {
		/**
		 * We go through the segment looking for the longest distance in all the angles of each pixel and 
		 * save it in a list, as we go through the points in order, it is not necessary to match between 
		 * the two lists since they will be in the same order in the same way we save in another list the 
		 * angle corresponding to the longest distance.
		 */
		LinkedList<Integer> distancespp = new LinkedList<>();
		LinkedList<Integer> angles = new LinkedList<>();
		for(Point p: segment) {
			int general = 0;
			int i = 0;
			int angle = 45;
			int angleFinal = 0;
			while(i < 7) {
				int dist = getDistance(p, angle*i);
				if(dist  > general) {
					general = dist;
					angleFinal = angle*i;
				}
				i++;
			}
			
			distancespp.add(general);
			angles.add(angleFinal);
		}
		
		
		/**
		 * We order the lists (distances, segment, angles) from largest to smallest based on the distances obtained previously
		 */
		for(int d = 0; d < distancespp.size()-1; d++) {
			for(int s = d+1; s < distancespp.size(); s++) {
				if(distancespp.get(d) < distancespp.get(s)) {
					int temp = distancespp.get(s);
					distancespp.set(s, distancespp.get(d));
					distancespp.set(d, temp);
					Point temP = segment.get(s);
					segment.set(s, segment.get(d));
					segment.set(d, temP);					
					int tempA = angles.get(s);
					angles.set(s, angles.get(d));
					angles.set(d, tempA);
				}
			}
		}
		
		/**
		 * We delete all the elements that generate circles with similar diameters +- 5 pixels
		 */
		for(int d=0; d < distancespp.size()-1; d++) {
			for(int s = d+1; s < distancespp.size(); s++) {
				if(distancespp.get(d) >= distancespp.get(s) - 5 || distancespp.get(d) <= distancespp.get(s) + 5 ) {
					distancespp.remove(s);
					segment.remove(s);
					angles.remove(s);
				}
			}
		}
		
		
		/**
		 * We delete the elements of the lists that have a distance less than or equal to 30
		 */
		for(int i = 0; i < distancespp.size(); i++) {
			if(distancespp.get(i) <= 30) {
				distancespp.remove(i);
				segment.remove(i);
				angles.remove(i);
			}
		}
		
		
		int[] intersections = new int[distancespp.size()];
		/**
		 * 
		 * We go through the elements of the lists and draw a straight segment from the midpoint 
		 * to all its angles in order to simulate a circunference
		 */
		for(int i= 0; i<distancespp.size(); i++) {
			int dist = distancespp.get(i);
			Point l = recorrer(segment.get(i), angles.get(i), dist/2);
			
			intersections[i] = drawCircles(l, dist/2, false);
			
		}
		
		
		/**
		 * We order the circles depending on the intersections they have with the leech edges, 
		 * but when moving some list (intersections, distances, segment, angles), they have to 
		 * be modified all at the same time.
		 */
		for(int i = 0;  i < intersections.length-1; i++) {
			for(int j = i+1; j < intersections.length; j++) {
				if(intersections[i] < intersections[j]) {
					// Moving list of intersections
					int temp = intersections[i];
					intersections[i] = intersections[j];
					intersections[j] = temp;
					// Moving list of distances
					int tempD = distancespp.get(i);
					distancespp.set(i, distancespp.get(j));
					distancespp.set(j, tempD);
					// Moving list of segment points
					Point temP = segment.get(i);
					segment.set(i, segment.get(j));
					segment.set(j, temP);					
					// Moving list of angles
					int tempA = angles.get(i);
					angles.set(i, angles.get(j));
					angles.set(j, tempA);
				}
			}
		}
		
		
//		System.out.println("Image "+ image + " -> Con un total de "+ distancespp.size() + " circulos");
		LinkedList<Point> pCenter = new LinkedList<>();
		int distL = 0;
		/**
		 * Take the circles and draw it on the image
		 */
		for(int i = 0; i < distancespp.size(); i++) {
			int dist = distancespp.get(i);
			if(dist/2 > distL)
				distL = dist;
			Point l = recorrer(segment.get(i), angles.get(i), dist/2);
			pCenter.add(l);
			
			drawCircles(l, dist/2, false); 										// If you want to draw the circles generated switch false by true
		}

		System.out.println("Longest distance "+ distL);
		int count = 0;
		for(Point p: pCenter) {
			for(int i = pCenter.indexOf(p)+1; i < pCenter.size(); i++) {
				Point p2 = pCenter.get(i);
				System.out.println("Distance between points "+ distance(p,p2));
				if(distance(p,p2) > distL)
					count++;
			}
		}
		System.out.println(count);
		System.out.println(count > 1 ? "true" : "false");
		
		// If you want to write on the image "swim correctly or swim incorrectly" uncomment the next lines
//		if(count >= 1) {
//			ip.drawString("Leech swim correctly", 10,10);
//		}else {
//			ip.drawString("Leech swim incorrectly", 10,10);
//		}
//		image.updateAndDraw();
		return count >= 1 ? true : false;
	}
	
	
	/**
	 * Method to know the distance between two points 
	 * @param p1 First point 
	 * @param p2 Second point
	 * @return Distance between the two points
	 */
	private int distance(Point p1, Point p2) {
		int x = Math.abs(p1.x - p2.x);
		int y = Math.abs(p1.y - p2.y);
		
		return (int) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		
	}
	
	
	/**
	 * Method to traverse a point the required distance and at a specified angle
	 * @param p Point 
	 * @param angle	Angle at which the point will be traversed (45, 90, 135, 180, 225, 270, 315)
	 * @param distance Distance the point will travel
	 * @return Found point
	 */
	private Point recorrer(Point p, int angle, int distance) {
		int x = p.x;
		int y = p.y;
		int count = 0;
		if(angle == 45) {								
			while(x < width && y > 0) {
				if(count == distance) {
					return new Point(x,y);
				}else {
					x = x +1;
					y = y -1;
					count++;
				}
			}
		}else if(angle == 90) {
			while(y > 0) {
				if(count == distance) {
					return new Point(x,y);
				}else {
					y = y -1;
					count++;
				}
			}
		}else if(angle == 135) {
			while(x > 0 && y > 0) {
				if(count == distance) {
					return new Point(x,y);
				}else {
					x = x - 1;
					y = y - 1;
					count++;
				}
			}
		}else if(angle == 180) {
			while(x > 0) {
				if(count == distance) {
					return new Point(x,y);
				}else {
					x = x - 1;
					count++;
				}
			}
		}else if(angle == 225) {
			while(x > 0 && y > 0) {
				if(count == distance) {
					return new Point(x,y);
				}else {
					x = x - 1;
					y = y - 1;
					count++;
				}
			}
		}else if(angle == 270) {
			while(y < height) {
				if(count == distance) {
					return new Point(x,y);
				}else {
					y = y + 1;
					count++;
				}
			}
		}else if(angle == 315) {
			while(x < width && y < height) {
				if(count == distance) {
					return new Point(x,y);
				}else {
					x = x + 1;
					y = y + 1;
					count++;
				}
			}
		}
		return new Point(0,0);
	}
	
	/**
	 * Method to draw a circle with a center point and a determined radio.
	 * @param center Center point
	 * @param rad Radio of the circle
	 * @param drawer Boolean that indicates if the circle will be drawn on the image or will only be superimposed on it.
	 */
	private int drawCircles(Point center, int rad, boolean drawer) {	
		
		// Drawing the circle
		Overlay overlay = new Overlay();
		Roi circle = new OvalRoi(center.x-rad, center.y - rad, rad*2, rad*2);
		circle.setStrokeColor(Color.red);
		circle.setStrokeWidth(2);
		overlay.add(circle);
		image.setOverlay(overlay);
		
		// Drawing a inside circle representing the center
		Overlay overlay2 = new Overlay();
		Roi roi = new OvalRoi(center.x, center.y, 1, 1);
		roi.setStrokeColor(Color.green);
		roi.setStrokeWidth(5);
		overlay2.add(roi);
		image.setOverlay(overlay2);
		image.updateAndDraw();
		
		// If drawer is true it means that the circles are painted in the image and in another case they only overlap
		if(drawer) {
			ip.drawOverlay(overlay);
			image.updateAndDraw();
		}
		
		// We get the points that make up the circle
		java.awt.Point[] point = circle.getContainedPoints();
		
		// We go through the points looking for matches with the points generated by the edges of the leech
		int count = 0;
		for(java.awt.Point p: point) {
			Point copy = new Point(p.x, p.y);
			if(points.contains(copy)) {
				count++;
			}
		}
		
		return count;
		
	}
	
	/**
	 * Method that looks for the neighbors of a certain pixel and that comply with having a value in gray scale equal to "value"
	 * @param x X Coordinate of the pixel
	 * @param y Y Coordinate of the pixel
	 * @param value Value of the pixels to search
	 * @return Number of neighboring pixels found with the given value
	 */
	private int vecinos(int x, int y, int value) {
		int count = 0;
		
		for(int j = y-1; j < y +2 ; j++) {
			for(int i = x - 1; i < x + 2; i++) {
				int pixel = image.getProcessor().getPixel(i, j);
				if(pixel == value)
					count++;
			}
		}
		return count;
	}	
}