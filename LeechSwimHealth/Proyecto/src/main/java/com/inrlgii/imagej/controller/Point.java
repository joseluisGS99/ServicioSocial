package com.inrlgii.imagej.controller;

/**
 * Clase que representa un punto en la matriz de la imagen y cuenta con una etiqueta para
 * asignarle un nombre determinado
 * @author jose_
 */
public class Point {

	public int x;
	public int y;
	String label;
	
	/**
	 * Constructor que recibe la posicion x e y del punto
	 * @param x	Coordenada x
	 * @param y	Coordenada y
	 */
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
		this.label = "";
	}
	
	/**
	 * Contructor que recibe la posicion x e y del punto y una etiqueta de este mismo
	 * @param x	Coordenada x 
	 * @param y Coordenada y 
	 * @param label	Etiqueta asignada
	 */
	public Point(int x, int y, String label) {
		this.x = x;
		this.y = y;
		this.label = label;
	}
	
	/**
	 * Metodo que compara dos puntos y nos dice si son iguales
	 */
	public boolean equals(Object o) {
		Point p = (Point) o;
		return (p.x == this.x && p.y == this.y && p.label.equals(label));
	}
	
	/**
	 * Metodo para imprimir un punto con su etiqueta en la pantalla de la terminal
	 */
	public String toString() {
		return "X "+ x + " Y " + y + " Label " + label; 
	}
}
