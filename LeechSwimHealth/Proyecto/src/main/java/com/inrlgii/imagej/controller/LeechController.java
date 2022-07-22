package com.inrlgii.imagej.controller;


import java.awt.Color;
import java.awt.Rectangle;

import com.inrlgii.imagej.model.LeechModelV2;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.process.ImageProcessor;



public class LeechController {

	private ImagePlus image;
	private ImageProcessor ip;
	private int width;
	private int height;
	
	public LeechController(ImagePlus image){
		System.out.println("Entro a LeechController");
		this.image = image;
		this.ip = image.getProcessor();
		this.width = ip.getWidth();
		this.height = ip.getHeight();
		run();		
	}
	
	private void run(){
		System.out.println("Entro metodo run");
		thresholding();
		findRoi();
		
		LeechModelV2 model = new LeechModelV2(getImageLeech());
		boolean lesion = model.search();
		if(!lesion)
			showDialog("Resultados", "Sanguijuela Sana");
		else
			showDialog("Resultados", "Sanguijuela Lesionada");
	}

	private void thresholding() {
		byte[] pixels = (byte[]) ip.getPixels();
		
		for(int y= 0; y < height; y++)
			for(int x = 0; x < width; x++) {
				byte pixel = pixels[x + y * width];
				if(pixel >= 80 || pixel <= 20 ) {
					ip.setColor(Color.WHITE);
					ip.drawPixel(x, y);
					image.updateAndDraw();
				}else {
					ip.setColor(Color.BLACK);
					ip.drawPixel(x, y);
					image.updateAndDraw();
				}
			}
	}
	
	
	private void findRoi(){
		while(image.getRoi() == null) 
			try {
				showDialog("Alert","Selecciona el area donde esta la sanguijuela");
				Thread.sleep(3000);
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}		
	}
	
	public void showDialog(String title, String text) {
		GenericDialog gd = new GenericDialog(title);
		gd.addMessage(text);
		gd.hideCancelButton();
		gd.showDialog();
	}
	
	private ImagePlus getImageLeech() {
		ImagePlus image2 = image.createImagePlus();
		
		Rectangle rec = image.getProcessor().getRoi();
		ImageProcessor imageP = image.getProcessor().resize((int) rec.getWidth(),(int) rec.getHeight());
		image2.setProcessor(imageP);
		
		image2.setTitle(image.getShortTitle() + "-Recortada");
		image2.show();
		return image2;
	}

	
}
