package com.inrlgii.imagej.model;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;

import ij.ImagePlus;
import ij.process.ImageProcessor;

public class LeechModel {
	
	private class Point{
		public int x ;
		public int y;
	 
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public String toString() {
			return "("+ x + ", " + y +")";
		}
	}
	
	private ImagePlus[] images;
	private int width;
	private int height;
	private boolean position; // if true the image Horizontal else image Vertical
	private LinkedList<Point> points1;
	private LinkedList<Point> points2;
	
	public LeechModel(ImagePlus image) {
		this.points1 = new LinkedList<Point>();
		this.points2 = new LinkedList<Point>();
		this.width = image.getWidth();
		this.height = image.getHeight();
		if(width > height) 
			position = true;
		
		System.out.println(position);
		
		images = new ImagePlus[2];
		images[0] = createNewImage(image, "Curva superior");  // points1
		images[1] = createNewImage(image, "Curva inferior");  // points2
		run();
	}
	
	
	private void run() {
		int nImage = 0;
		for(ImagePlus image: images) {
			image.show();
			findEdges(image, nImage++);
			showColor(image);			
			drawLine(image);
			image.updateAndDraw();
			nImage++;
		}
		
//		checkPercentage();
//		getIntersections(images[0]);
	}
	
	private void savePoints(ImagePlus image, int nImage) {
		byte[] pixels = (byte[]) image.getProcessor().getPixels();
		if(nImage == 0) {
			points1 = new LinkedList<Point>();
			
			for(int x = 0; x < pixels.length; x++) {
				if(pixels[x] == -1) {
					points1.add(new Point(x, 0));
//					pixels[x] = 0;
					image.updateAndDraw();
				}
			}			
		}else if (nImage == 1) {
			points2 = new LinkedList<Point>();
			
			for(int x = 0; x < pixels.length; x++) {
				if(pixels[x] == -1) {
					points2.add(new Point(x, 0));
//					pixels[x] = 0;
					image.updateAndDraw();
				}
			}
		}
		
	}
	
	private void lines(ImagePlus image, int nImage) {
		
		Iterator<Point> it;
		ImageProcessor ip = image.getProcessor();
		ip.setColor(128);
		
		if(nImage == 0) {
			it = points1.iterator();
			while(it.hasNext()) {
				Point p1 = it.next();
				ip.drawPixel(p1.x, p1.y);
				image.updateAndDraw();
			}
			
		}else if(nImage == 1) {
			it = points2.iterator();
			while(it.hasNext()) {
				Point p1 = it.next();
				ip.drawPixel(p1.x, p1.y);
				image.updateAndDraw(); 
			}
		}
		

	}
	
	private void findEdges(ImagePlus image, int nImage) {
		ImageProcessor ip = image.getProcessor();
		byte[] pixels = (byte[]) ip.getPixels();
		Point p = null;
		
		if(position) // Image Horizontal
			if(nImage == 0)   // Superior	// Points1
				for(int x = 0;  x < width; x++) {
					int y = 0;
					byte value = pixels[x + y * width];
					while(y+1 < height &&  value != -1) {
						y++;
						value = pixels[x + y * width];
					}
					if(value == -1) {
						if(p == null) {
							p = new Point(x,y);
							points1.add(p);
						}else {
							if(Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2)) < 100){
								p = new Point(x,y);
								points1.add(p);
							}
						}
						ip.setColor(128);
						ip.drawPixel(x, y);		
						image.updateAndDraw();
					}
				}
			else             // Inferior	// Points2
				for(int x = width -1 ; x > 0; x--) {
					int y = height - 1;
					while(y > 0 && pixels[x + y * width] != -1)
						y--;
					if(pixels[x + y * width] == -1) {
						if(p == null) {
							p = new Point(x,y);
							points2.add(p);
						}else
							if(Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2)) < 100){
								p = new Point(x,y);
								points2.add(p);
							}
						ip.setColor(128);
						ip.drawPixel(x, y);	
						image.updateAndDraw();
					}
				}
		else   // Image vertical
			if(nImage == 1) // Extreme left		// points1
				for(int y = 0; y < height; y++) {
					int x = 0;
					if(x < width) {
						byte value = pixels[x + y * width];
						while(value != -1 && x+1 < width) {
							x++;
							value = pixels[x + y * width];
						}
						if(value == -1) {
							if(p == null) {
								p = new Point(x,y);
								points1.add(p);
							}else
								if(Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2)) < 100){
									p = new Point(x,y);
									points1.add(p);
								}
							ip.setColor(128);
							ip.drawPixel(x, y);		
							image.updateAndDraw();
						}
					}
				}
			else 	// Extreme right		// points2
				for(int y = 0; y < height; y++) {
					int x = width - 1;
					byte value = pixels[x + y * width];
					while(value != -1 && x > 0) {
						x--;
						value = pixels[x + y * width];
					}
					if(value == -1) {
						if(p == null) {
							p = new Point(x,y);
							points2.add(p);
						}else
							if(Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2)) < 100){
								p = new Point(x,y);
								points2.add(p);
							}
						ip.setColor(128);
						ip.drawPixel(x, y);		
						image.updateAndDraw();
					}
				}
	}
	
	private void showColor(ImagePlus image) {
		
		ImageProcessor ip = image.getProcessor();		
	
		byte[] pixels = (byte[]) ip.getPixels();
		
		for(int y= 0; y < height; y++)
			for(int x =0; x < width; x++) {				
				if(Math.abs(pixels[x + y * width]) == 128) {  // antes 0 = 128
					ip.setColor(Color.BLACK);
					ip.drawPixel(x, y);
				}else {
					ip.setColor(Color.WHITE);
					ip.drawPixel(x, y);
				}
			}
	}
	
	private void drawLine(ImagePlus image) {
		
//		Point p1;
//		Point p2;
//		ImageProcessor ip = image.getProcessor();
//		if (ip != null) {
//			ip.setColor(100);
//			ip.setLineWidth(1);
//			
//			if(image == images[0] && points1.size() >= 2) {	// Points1
//				p1 = points1.getFirst();
//				p2 = points1.getLast();
//				
//				ip.drawLine(p1.x, p1.y, p2.x, p2.y);
//			}
//			
//			if(image == images[1] && points1.size() >= 2) {	// Points2
//				p1 = points2.getFirst();
//				p2 = points2.getLast();
//				ip.drawLine(p1.x, p1.y, p2.x, p2.y);
//			}
//		}

		ImageProcessor ip = image.getProcessor();
		byte[] pixels = (byte[]) ip.getPixels();
		int xMn = 0, yMn = 0, xMx = 0, yMx = 0;
		for(int y=0 ; y < height; y++) {
			int x =0 ;
			byte value = pixels[x + y * width];
			
			while(x+1 < width && pixels[x + y * width] != 0) {
				x++;
				value = pixels[x + y * width];
			}
			if(value == 0) {
				xMn = x;
				yMn = y;
			}
		}
		
		for(int y =height-1; y > 0; y--) {
			int x = 0;
			byte value = pixels[x + y * width];
			
			while(x+1 < width && pixels[x + y * width] != 0) {
				x++;
				value = pixels[x + y * width];
			}
			if(value == 0) {
				xMx = x;
				yMx = y;
			}
		}
		
		System.out.println("xMin "+ xMn + "\txMax "+ xMx + "\nyMin" + yMn + "\tyMax "+ yMx );
		ip.setColor(100);
		ip.setLineWidth(4);
		ip.drawLine(xMn, yMn, xMx, yMx);
		image.draw();
		image.updateAndDraw();
		
	}

	/**
	 * Método para obtener el porcentaje de las graficas (si esta correcta o incorrecta)
	 */
	public int checkPercentage() {
		int sum = 0;
			
		for(ImagePlus image: images) 
			sum += getIntersections(image);

		
		System.out.println("Suma total de intersecciones : "+ sum);
		
//		if( sum > 6)
//			LeechView.showDialog("Resultados","Sanguijuela Sana");
//		else
//			LeechView.showDialog("Resultados","Sanguijuela Lesionada");
		
		return (sum > 6) ? 1 : 0;
		
	}
	
	/**
	 * Método para obtener cuantas intersecciones tuvo la linea recta con nuestras graficas en la imagen
	 * @param image Imagen que representa una grafica y contiene una linea recta (color 100 a escala de grises 8 bits)
	 * @return	- Cantidad de intersecciones que se encontraron
	 */
	private int getIntersections(ImagePlus image) {
		int sum = 0;
		if(image == images[0]) {
			byte[] pixels = (byte[]) image.getProcessor().getPixels();
			int width = image.getWidth();
			Iterator<Point> it = points1.iterator();
			while(it.hasNext()) {
				Point p = it.next();
				if(pixels[p.x + p.y * width] == 100)
					sum++;
			}
			System.out.println(sum);
			return sum;
		}
		
		if (image == images[1]) {
			byte[] pixels = (byte[]) images[1].getProcessor().getPixels();
			int width = images[1].getWidth();
			Iterator<Point> it = points2.iterator();
			while(it.hasNext()) {
				Point p = it.next();
				if(pixels[p.x + p.y * width] == 100)
					sum++;
			}
			System.out.println(sum);
			return sum;
		}
		return sum;
	}
	
	/**
	 * Método para checar si existen vecinos de un pixel en especifico (v,u) que esten 
	 * pintados de color negro (escala 0) 
	 * @param v Coordenadas del pixel en el eje horizontal
	 * @param u Coordenadas del pixel en el eje vertical 
	 * @param ip Procesador de la imagen
	 * @return	1 en caso de encontrar vecinos y 0 en otro caso
	 */
	private int checkVecinos(int v, int u, ImageProcessor ip) {
		byte[] pixels = (byte[]) ip.getPixels();
		int count = 0;
		for(int y = u-1; y < u+2; y++)
			for(int x = v-1; x < v+2; x++)
				if((x + y * width) < pixels.length && (x + y * width) > 0) {
					byte value = pixels[x + y * width];
					if(value == 0)
						count++;				
				}
		
		return count;
	}
	
	
	
	public void cleanImage() {
		for (ImagePlus image: images) {
			ImageProcessor ip = image.getProcessor();
			byte[] pixels = (byte[]) ip.getPixels();
			for(int y = 0; y < image.getHeight(); y++)
				for(int x = 0; x < image.getWidth(); x++) {
					if(pixels[x + y * image.getWidth()] != 255)
						if(checkVecinos(x, y, ip) <= 2) {
							System.out.println("Cleaning "+ x + ", "+ y);
							ip.setColor(255);
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							ip.drawPixel(x, y);
							image.updateAndDraw();
							
						}else {
							System.out.println("except "+ x + ", "+ y);
						}
				}
			
		}
	}
	
	/**
	 * Método para crear copias de un objeto de tipo ImagePlus
	 * @param image - Imagen a copiar 
	 * @param name	- Nombre de la nueva imagen
	 * @return	- Copia de la imagen ingresada como parametro
	 */
	private ImagePlus createNewImage(ImagePlus image, String name) {
		ImagePlus nImage = new ImagePlus();
		nImage.setTitle(name);
		nImage.setProcessor(image.getProcessor().duplicate());
		return nImage;
	}
	
	
	private void showPoints() {
		ImagePlus im = createNewImage(images[1], "points2");
		ImageProcessor ip = im.getProcessor();
		byte[] pixels = (byte[]) ip.getPixels();
		
		for (int i = 0; i < pixels.length; i++ )
			pixels[i] = (byte) 255;
		
		im.updateAndDraw();
		im.show();
		
		for(Point p : points1) {
			ip.drawPixel(p.x, p.y);
			im.updateAndDraw();
		}
		
	}
}
