package com.inrlgii.imagej.model;

import java.awt.Color;
import java.util.LinkedList;

import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import com.inrlgii.imagej.controller.Point;

public class LeechModelV2 {
	
	private ImagePlus image;
	private ImageProcessor ip;
	private LinkedList<Point> points;
	private LinkedList<Point> segment;
	private int width;
	private int height;
	
	
	public LeechModelV2(ImagePlus image) {
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
		 * Recorremos el segmento buscando en todos los angulos de cada pixel la distancia mas larga
		 * y la guardamos en una lista, como recorremos los puntos en orden, no es necesario hacer match entre las dos listas ya estaran en el mismo orden
		 * de igual forma guardamos en otra lista el angulo correcpondiente a la distancia mas larga
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
		 * Ordenamos las listas (distancias, segmento, angulos) de mayor a menor basandonos en las distancias obtenidas anteriormente
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
		 * Eliminamos todos los elementos que generan circulos con diametros similares +- 5 pixeles
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
		 * Borramos los elementos de las listas que tienen una distancia menor o igual a 30
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
		 * Recorremos los elementos de las listas y trazamos un segmento recto del punto medio hacia todos sus angulos
		 * con el fin de simular una circunferencia
		 */
		for(int i= 0; i<distancespp.size(); i++) {
			int dist = distancespp.get(i);
			Point l = recorrer(segment.get(i), angles.get(i), dist/2);
			
			intersections[i] = drawCircles(l, dist/2, false);
			
		}
		
		
		/**
		 * Ordenamos los circulos dependiendo de las intersecciones que tienen con las aristas de la sanguijuela, pero al mover alguna lista 
		 * (intersecciones, distancias, segmento, angulos), se tienen que modificar todas al mismo tiempo.
		 */
		for(int i = 0;  i < intersections.length-1; i++) {
			for(int j = i+1; j < intersections.length; j++) {
				if(intersections[i] < intersections[j]) {
					// Moviendo lista de intersecciones
					int temp = intersections[i];
					intersections[i] = intersections[j];
					intersections[j] = temp;
					// Moviendo lista de distancias
					int tempD = distancespp.get(i);
					distancespp.set(i, distancespp.get(j));
					distancespp.set(j, tempD);
					// Moviendo lista de puntos del segmento
					Point temP = segment.get(i);
					segment.set(i, segment.get(j));
					segment.set(j, temP);					
					// Moviendo lista de angulos
					int tempA = angles.get(i);
					angles.set(i, angles.get(j));
					angles.set(j, tempA);
				}
			}
		}
		
		
		System.out.println("Imagen "+ image + " -> Con un total de "+ distancespp.size() + " circulos");
		LinkedList<Point> pCenter = new LinkedList<>();
		int distL = 0;
		/**
		 * Tomamos los circulos y los dibujamos sobre la imagen
		 */
		for(int i = 0; i < distancespp.size(); i++) {
//		for(int i = 0; i < 2; i++) {
			int dist = distancespp.get(i);
			if(dist/2 > distL)
				distL = dist;
			Point l = recorrer(segment.get(i), angles.get(i), dist/2);
			pCenter.add(l);
			
			drawCircles(l, dist/2, false); 										// Cambiar el false por un true si se desea dibujar los circulos generados
		}

		System.out.println("distancia mas larga "+ distL);
		int count = 0;
		for(Point p: pCenter) {
			for(int i = pCenter.indexOf(p)+1; i < pCenter.size(); i++) {
				Point p2 = pCenter.get(i);
				System.out.println("distancia entre puntos "+ distance(p,p2));
				if(distance(p,p2) > distL)
					count++;
			}
		}
		System.out.println(count);
		System.out.println(count > 1 ? "true" : "false");
		
		// Descomentar las lineas para que escriba en cada imagen sana o lesionada dependiendo los resultados
//		if(count >= 1) {
//			ip.drawString("Sana", 10,10);
//		}else {
//			ip.drawString("lesionada", 10,10);
//		}
//		image.updateAndDraw();
		return count >= 1 ? true : false;
	}
	
	
	/**
	 * Metodo para saber la distancia entre dos puntos del plano
	 * @param p1 Primer punto en el plano cartesiano 
	 * @param p2 Segundo punto en el plano cartesiano
	 * @return Distancia entre los dos puntos
	 */
	private int distance(Point p1, Point p2) {
		int x = Math.abs(p1.x - p2.x);
		int y = Math.abs(p1.y - p2.y);
		
		return (int) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		
	}
	
	
	/**
	 * Metodo para recorrer un punto la distancia requerida y a un angulo especifico
	 * @param p Punto a recorrer
	 * @param angle	Angulo en el que se recorrerá el punto (45, 90, 135, 180, 225, 270, 315)
	 * @param distance Distancia que recorrerá el punto
	 * @return Punto encontrado
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
	 * Metodo para dibujar un circulo con un punto de centro y un determinado radio
	 * @param center Punto centro 
	 * @param rad Entero que indica el radio del circulo
	 * @param drawer Booleano que indica si el circulo se dibujara sobre la imagen o solo se sobre-pondrá en ella.
	 */
	private int drawCircles(Point center, int rad, boolean drawer) {	
		
		// Trazando el circulo
		Overlay overlay = new Overlay();
		Roi circle = new OvalRoi(center.x-rad, center.y - rad, rad*2, rad*2);
		circle.setStrokeColor(Color.red);
		circle.setStrokeWidth(2);
		overlay.add(circle);
		image.setOverlay(overlay);
		
		// Trazando un circulo interno representando el centro
		Overlay overlay2 = new Overlay();
		Roi roi = new OvalRoi(center.x, center.y, 1, 1);
		roi.setStrokeColor(Color.green);
		roi.setStrokeWidth(5);
		overlay2.add(roi);
		image.setOverlay(overlay2);
		image.updateAndDraw();
		
		// Si drawer es true quiere decir que se pintaran los circulos en la imagen y en otro caso solo se sobre ponen
		if(drawer) {
			ip.drawOverlay(overlay);
			image.updateAndDraw();
		}
		
		// Obtenemos los puntos que conforman el circulo
		java.awt.Point[] point = circle.getContainedPoints();
		
		// Recorremos los puntos buscando coincidencias con los puntos generados por las aristas de la sanguijuela
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
	 * Metodo que busca a los vecinos de un pixel determinado y que cumplan con tener una valor en escala de grises
	 * igual a "value"
	 * @param x Coordenada x del pixel en cuestion
	 * @param y Coordenada y del pixel en cuestion
	 * @param value Valor de los pixeles a buscar
	 * @return Cantidad de pixeles vecinos encontrados con el valor determinado
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