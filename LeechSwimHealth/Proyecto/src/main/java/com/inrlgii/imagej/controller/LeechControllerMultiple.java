package com.inrlgii.imagej.controller;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.LinkedList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;

import com.inrlgii.imagej.model.LeechModelV2;

public class LeechControllerMultiple {
	
	private ImagePlus[] images;
	private ImagePlus[] copy;
	
	public LeechControllerMultiple(ImagePlus[] images) {
		this.images = images;
		run();
	}
	
	private void run() {
		System.out.println("It enter the image controller with a total of "+ images.length + "images");
		double progress = 0.25;
		IJ.protectStatusBar(true);
		// We calculate the difference, substraction and applied the thresholding function
		calculate();
		// We applied the segmentation by cuadrant's
		detectQuadrant();
		// We erase small objects from the image
		eraseSmallObjects();
		// We check the leech if swim correctly or not
		// Checamos si estan lesionadas o sanas
		checkPercentage();
		IJ.showProgress(progress);
		progress += 0.25;
		
	}
	
	
	/**
	 * This method use the library ImageCalculator to calculate the difference of two images and then
	 * substract the static objects from the result images.
	 */
	private void calculate() {
		this.copy = copyArray();
		for(int n = 0 ; n < images.length; n++) {
			System.out.println("n +1 "+ n + "\tn + 2 "+ ((n + 1 ) % images.length));
			
			// If you want to modify the original images, switch the arrays copy and images from the follow two operations		
			(new ImageCalculator()).run("difference",copy[n], images[(n+1)%images.length]);
			(new ImageCalculator()).run("subtract"  , copy[n], images[(n+1)%images.length]);
		}
		
		double progress = 0.25/images.length;
		int i = 1;
		
		// If you want to modify the original images, switch copy and images arrays 
		for(ImagePlus image: copy) {
			IJ.showProgress(progress*(i++));
			image.getProcessor().autoThreshold();
		}
			
	}
	
	/**
	 * This method copy an images array of type ImagePlus
	 * @return Array of images of type ImagePlus
	 */
	private ImagePlus[] copyArray() {
		ImagePlus[] copyImages = new ImagePlus[images.length];
		int n = 0;
		for(ImagePlus image: images) {
			ImagePlus c = new ImagePlus();
			c.setProcessor(image.getProcessor().duplicate());
			c.setTitle(image.getShortTitle());
			copyImages[n++] = c;
		}
		return copyImages;
	}
	
	
	/**
	 * This method check an image by quadrants (9) and cuts it depending on the number of black pixels in the quadrants
	 */
	// If you want to modify the original image, switch copy array to images array
	private void detectQuadrant() {
		double progress = 0.25/images.length;
		int c = 1;
		for(ImagePlus image: copy) {
			IJ.showProgress(0.25 + (progress * (c++)));
			byte[] pixels = (byte[]) image.getProcessor().getPixels();
			int width = image.getWidth();
			int height = image.getHeight();
			int w3 = width/3;
			int h3 = height/3;


			int[] quadrants = new int[9];
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if(x + y * width < pixels.length) {
						byte value = pixels[x + y * width];
						
						if(value == -1) {
							if(y < h3 && x < w3) 									quadrants[0]++;
							if(y < h3 && x < 2*w3 && x >= w3) 						quadrants[1]++;
							if(y < h3 && x < width && x > 2*w3) 					quadrants[2]++;
							
							if(y >= h3 && y < 2*h3 && x < w3) 						quadrants[3]++;
							if(y >= h3 && y < 2*h3 && x >= w3   && x < 2*w3 ) 		quadrants[4]++;
							if(y >= h3 && y < 2*h3 && x >= 2*w3 && x < width ) 		quadrants[5]++;
							
							if(y >= 2*h3 && y < height && x < w3) 					quadrants[6]++;
							if(y >= 2*h3 && y < height && x >= w3   && x < 2*w3) 	quadrants[7]++;
							if(y >= 2*h3 && y < height && x >= 2*w3 && x < width) 	quadrants[8]++;
						}
					}
				}
			}
			
			regionInterest(image, quadrants, width, height);

		}
	}

//	System.out.println(image);
//	for(int i = 0; i < quadrants.length; i++) {
//		System.out.println("Quadrant "+ (i+1) + " => "+ quadrants[i]);
//	}
	
	/**
	 * This method cut an image depending on the number of black pixels in the quadrants 
	 * @param image 		Image to be cropped
	 * @param quadrants		Array of the quadrants (size 9) with the sum of the black pixels by quadrant
	 * @param width			Width of the image
	 * @param height		Height of the image
	 */
	private void regionInterest(ImagePlus image, int[] quadrants, int width, int height) {
		int c1 = quadrants[0];
		int c2 = quadrants[1];
		int c3 = quadrants[2];
		int c4 = quadrants[3];
		int c5 = quadrants[4];
		int c6 = quadrants[5];
		int c7 = quadrants[6];
		int c8 = quadrants[7];
		int c9 = quadrants[8];
		int h3 = height/3;
		int w3 = width/3;
		int x1 = 0, x2 = 0; 
		int y1 = 0, y2 = 0;
		
		// Horizontal cases
		if(c1 > 200 && c2 > 200 && c1 < 7500 && c2 < 7500) {x2 = 2*w3; y2 = h3;}									// quadrants 1 y 2 
		else if(c2 > 200 && c3 > 200 && c2 < 7500 && c3 < 7500) {x1 = w3; x2 = width; y2 = h3;}						// quadrants 2 y 3
		else if(c4 > 200 && c5 > 200 && c4 < 7500 && c5 < 7500) {y1 = h3; x2 = 2*w3; y2 = 2*h3; }					// quadrants 4 y 5
		else if(c5 > 200 && c6 > 200 && c5 < 7500 && c6 < 7500) {x1 = w3; y1 = h3; x2 = width; y2 = 2*h3;}			// quadrants 5 y 6
		else if(c7 > 200 && c8 > 200 && c7 < 7500 && c8 < 7500) {y1 = 2*h3; x2 = 2*w3; y2 = height;}				// quadrants 7 y 8
		else if(c8 > 200 && c9 > 200 && c8 < 7500 && c9 < 7500) {x1 = w3; y1 = 2*h3; x2 = width; y2 = height;}		// quadrants 8 y 9
		
		// Vertical cases
		else if(c1 > 200 && c4 > 200 && c1 < 7500 && c4 < 7500) {x2 = w3; y2 = 2*h3;}								// quadrants 1 y 4
		else if(c2 > 200 && c5 > 200 && c2 < 7500 && c5 < 7500) {x1 = w3; x2 = 2*w3; y2 = 2*h3;}					// quadrants 2 y 5
		else if(c3 > 200 && c6 > 200 && c3 < 7500 && c6 < 7500) {x1 = 2*w3; x2 = width; y2 = 2*h3;}					// quadrants 3 y 6
		else if(c4 > 200 && c7 > 200 && c4 < 7500 && c7 < 7500) {x2 = w3; y2 = height;}								// quadrants 4 y 7
		else if(c5 > 200 && c8 > 200 && c5 < 7500 && c8 < 7500) {x1 = w3; x2 = 2*w3; y2 = height;}					// quadrants 5 y 8
		else if(c6 > 200 && c9 > 200 && c6 < 7500 && c9 < 7500) {x2 = 2*w3; x2 = width; y2 = height;}				// quadrants 6 y 9
		
		// Individual cases
		else if(c1 > 200 && c1 < 8500) {x2 = w3; y2 = h3;}															// quadrant 1
		else if(c2 > 200 && c2 < 8500) {x1 = w3; x2 = (2*w3); y2 = h3;}												// quadrant 2
		else if(c3 > 200 && c3 < 8500) {x1 = (2*w3); x2 = width; y2 = h3;}											// quadrant 3
		else if(c4 > 200 && c4 < 8500) {y1 = h3; x2 = w3; y2 = (2*h3); }											// quadrant 4
		else if(c5 > 200 && c5 < 8500) {x1 = w3; y1 = h3; x2 = (2*w3); y2 = (2*h3);}								// quadrant 5
		else if(c6 > 200 && c6 < 8500) {x1 = (2*w3); y1 = h3; x2 = width; y2 = 2*h3;}								// quadrant 6
		else if(c7 > 200 && c7 < 8500) {y1 = (2*h3); x2 = w3; y2 = height;}											// quadrant 7
		else if(c8 > 200 && c8 < 8500) {x1 = w3; y1 = (2*h3); x2 = (2*w3); y2= height;}								// quadrant 8
		else if(c9 > 200 && c9 < 8500) {x1 = (2*w3); y1 = (2*h3); x2 = width; y2 = height;}							// quadrant 9
		
		// Especial cases
		else if(c1 < 200 && c2 < 200 && c1 < 7500 && c2 < 7500) {x2 = 2*w3; y2 = h3;}								// quadrants 1 y 2
		else if(c2 < 200 && c3 < 200 && c2 < 7500 && c3 < 7500) {x1 = w3; x2 = width; y2 = h3;}						// quadrants 2 y 3
		else if(c4 < 200 && c5 < 200 && c4 < 7500 && c5 < 7500) {y1 = h3; x2 = 2*w3; y2 = 2*h3; }					// quadrants 4 y 5
		else if(c5 < 200 && c6 < 200 && c5 < 7500 && c6 < 7500) {x1 = w3; y1 = h3; x2 = width; y2 = 2*h3;}			// quadrants 5 y 6
		else if(c7 < 200 && c8 < 200 && c7 < 7500 && c8 < 7500) {y1 = 2*h3; x2 = 2*w3; y2 = height;}				// quadrants 7 y 8
		else if(c8 < 200 && c9 < 200 && c8 < 7500 && c9 < 7500) {x1 = w3; y1 = 2*h3; x2 = width; y2 = height;}		// quadrants 8 y 9
		
		// Especial vertically cases
		else if(c1 < 200 && c4 < 200 && c1 < 7500 && c4 < 7500) {x2 = w3; y2 = 2*h3;}								// quadrants 1 y 4
		else if(c2 < 200 && c5 < 200 && c2 < 7500 && c5 < 7500) {x1 = w3; x2 = 2*w3; y2 = 2*h3;}					// quadrants 2 y 5
		else if(c3 < 200 && c6 < 200 && c3 < 7500 && c6 < 7500) {x1 = 2*w3; x2 = width; y2 = 2*h3;}					// quadrants 3 y 6
		else if(c4 < 200 && c7 < 200 && c4 < 7500 && c7 < 7500) {x2 = w3; y2 = height;}								// quadrants 4 y 7
		else if(c5 < 200 && c8 < 200 && c5 < 7500 && c8 < 7500) {x1 = w3; x2 = 2*w3; y2 = height;}					// quadrants 5 y 8
		else if(c6 < 200 && c9 < 200 && c6 < 7500 && c9 < 7500) {x2 = 2*w3; x2 = width; y2 = height;}				// quadrants 6 y 9
		
		System.out.println("Region of interest  ("+ x1 + " , "+ y1 +" )  =>  ("+ x2 +" , "+ y2 +" )");
		
		// Select the rectangle of interest
		image.setRoi(new Rectangle(x1 , y1, x2-x1, y2-y1));
		cutImage(image, x1, y1, x2, y2);
	}
	
	/**
	 * This method erase smallest objects from the images
	 */
	private void eraseSmallObjects() {
		double progress = 0.25/images.length;
		int c = 1;
		
		// If you want to modify the original image, switch copy array to images array
		for(ImagePlus image: copy) {
			IJ.showProgress(0.5 + (progress * (c++)));
			LinkedList<LinkedList<Point>> objects = new LinkedList<>();
			ImageProcessor ip = image.getProcessor();
			int width = ip.getWidth();
			int height = ip.getHeight();
			byte[] pixels = (byte[]) ip.getPixels();
			
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if(pixels[x + y * width] == -1) {
						Overlay overlay = new Overlay();
						Roi roi = new OvalRoi(x, y, 2, 2);
						roi.setStrokeColor(Color.red);
						roi.setStrokeWidth(2);
						overlay.add(roi);
						image.setOverlay(overlay);
						
						boolean include = false;
						if(objects.isEmpty()) {
							LinkedList<Point> list = new LinkedList<Point>();
							list.add(new Point(x,y,"1"));
							objects.add(list);
							include = true;
						}else {
							for(LinkedList<Point> list: objects) {
								String label = list.getFirst().label;
								Point p1 = new Point(x-1, y-1, label);
								Point p2 = new Point(x-1, y, label);
								Point p3 = new Point(x-1, y+1, label);
								Point p4 = new Point(x, y-1, label);
								Point p5 = new Point(x, y+1, label);
								Point p6 = new Point(x+1, y-1, label);
								Point p7 = new Point(x+1, y, label);
								Point p8 = new Point(x+1, y+1, label);
								
								if(list.contains(p1) || list.contains(p2) || list.contains(p3) || list.contains(p4) || list.contains(p5) ||
										list.contains(p6) || list.contains(p7) || list.contains(p8)) {
									list.add(new Point(x,y, label));
									include = true;
								}
							}
						}
						if(!include) {
							String label = objects.getLast().getFirst().label;
							Integer labelN = Integer.parseInt(label);
							labelN++;
							LinkedList<Point> list = new LinkedList<>();
							list.add(new Point(x,y, ""+labelN));
							objects.add(list);
						}
					}
				}
			}
			
			System.out.println("Total objects found "+ objects.size());
			for(LinkedList<Point> list: objects) {
				if(list.size() < 30) {
					for(Point p: list) {
						ip.setColor(0);
						ip.drawPixel(p.x, p.y);
						image.updateAndDraw();
					}
				}
			}
		}
		
	}
	
	/**
	 * This method checks if the leech swim correctly 
	 */
	private void checkPercentage() {
		double progress = 0.25/images.length;
		int c = 1;
		boolean lesion = false;
		
		// If you want to modify the original image, switch copy array to images array
		for(ImagePlus image: copy) {
			IJ.showProgress(0.75 + (progress * (c++)));
			boolean ret = new LeechModelV2(image).search();
			if(!lesion)
				lesion = ret;
		}
		
		if(lesion)
			showDialog("Results", "The leech swim correctly");
		else
			showDialog("Results", "The leech swim incorrectly");
	}
	
	/**
	 * This method show a message dialog to the user 
	 * @param title Title of the box dialog
	 * @param text Message to the user
	 */
	public void showDialog(String title, String text) {
		GenericDialog gd = new GenericDialog(title);
		
		gd.addMessage(text);
		gd.hideCancelButton();
		gd.showDialog();
		
	}
	
	/**
	 * This method slices an image by reference to two points at opposite corners (x1,y1) and (x2,y2).
	 * @param image Image to be cut
	 * @param x1	Parameter X of the first point of reference.
	 * @param y1	Parameter Y of the first point of reference.
	 * @param x2	Parameter X of the second point of reference.
	 * @param y2	Parameter Y of the second point of reference.
	 */
	private void cutImage(ImagePlus image, int x1, int y1, int x2, int y2) {
		ImageProcessor ip = image.getProcessor();
		Roi roi = image.getRoi();
		// We create a new image with the same size of the rectangle.
		ImagePlus im = new ImagePlus();
		im.setProcessor(ip.createProcessor(x2-x1, y2-y1));
		im.setImage(roi.getImage().crop());
		im.setTitle(image.getShortTitle()+ "-recortada");
		
		// We assign the image in the position of the last image.
		image.setImage(im);
		image.updateAndDraw();
	}
}
