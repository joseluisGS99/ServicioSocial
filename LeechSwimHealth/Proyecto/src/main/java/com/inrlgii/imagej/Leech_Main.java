package com.inrlgii.imagej;


import com.inrlgii.imagej.controller.LeechController;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.plugin.filter.PlugInFilter;


public class Leech_Main implements PlugInFilter {
	
	private ImagePlus image;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}

		image = imp;
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}
	
	@Override
	public void run(ImageProcessor ip) {
		if(image.getType() == ImagePlus.COLOR_RGB) {
			new ImageConverter(image).convertToGray8();
			image.updateAndDraw();
		}
		new LeechController(image);
	}
	
	public void showAbout() {
		IJ.showMessage("Leech Movement",
			"a template for processing and analysis of leech movement "
		);
	}
	
	
	public static void main (String[] args) throws Exception{
		
		System.out.println("Entro OpenImages");
		Class<?> clazz = Leech_Main.class;
		java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
		java.io.File file = new java.io.File(url.toURI());
		System.setProperty("plugins.dir", file.getAbsolutePath());
		
		// start ImageJ
		new ImageJ();

		
		// Open Dialog to search the image
		OpenDialog.setDefaultDirectory("C:\\Users\\jose_\\Desktop\\SS\\Imagenes\\Videos y Fotogramas");
		OpenDialog dialog = new OpenDialog("Subir imagen");
		// Get the path of image
		String path = dialog.getPath();
		
		System.out.println(path);
		
		// Open the image from the path
		ImagePlus image = IJ.openImage(path);
		image.show();
		
		if(image.getType() == ImagePlus.COLOR_RGB) {
			new ImageConverter(image).convertToGray8();
			image.updateAndDraw();
		}
		
		IJ.runPlugIn(clazz.getName(), "");
	}

	
}
