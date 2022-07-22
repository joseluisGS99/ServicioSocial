package com.inrlgii.imagej;

import java.io.File;

import javax.swing.JFileChooser;

import com.inrlgii.imagej.controller.LeechControllerMultiple;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

public class Leech_MainM implements PlugInFilter{
	
	private String[] openWindows;
	private int[] windows_id;
	private String[] titles;
	private int nImages;
	
	public Leech_MainM() {
		this.windows_id = null;
	}
	
	
	public int setup(String arg, ImagePlus imp) {
		if(imp == null)
			IJ.noImage();
		
		this.openWindows = new String[WindowManager.getImageCount()];
		this.nImages = this.openWindows.length;
		this.titles = new String[nImages];
		
		if(this.nImages <= 1) {
			IJ.showMessage("it's required at least two images");
			return -1;				
			
		}
		
		this.windows_id = WindowManager.getIDList();
		for(int i = 0; i < this.openWindows.length; ++i) 
			this.openWindows[i] = WindowManager.getImage(this.windows_id[i]).getTitle();
		
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}
	
	public void run(final ImageProcessor ip) {
		
		alert();
		
		if(!this.showDialog())
			return;
		
		final ImagePlus[] images = new ImagePlus[this.nImages];
		for(int i = 0; i < this.nImages; i++)
			images[i] = WindowManager.getImage(titles[i]);
		
		for(ImagePlus image: images)
			if(image.getType() == ImagePlus.COLOR_RGB) {
				new ImageConverter(image).convertToGray8();
				image.show();
			}
		
		new LeechControllerMultiple(images);
	}
	
	private void alert() {
		GenericDialog gd = new GenericDialog("Alerta");
		gd.addMessage("This program works as long as the inserted images were obtained from videos with a static background, \n"
					+ "without foreign objects (tweezers, hands, etc.) in the image, and the images should not be blurred. \n"
					+ "If any of the above is present, the program will not work correctly and will give erroneous results.\n"
					+ "It should be clarified that the inserted images must belong to the same video and in case of using it \n"
					+ "with images from different videos, the program will give erroneous results or may even not work.");
		gd.hideCancelButton();
		gd.showDialog();
	}
	
	private boolean showDialog() {
		for(int i =0 ; i < openWindows.length; i++) {
			System.out.println(openWindows[i]);
			this.titles[i] = this.openWindows[i];
		}
		return true;
	}
	
	public void showError(String error) {
		IJ.showMessage(error);
	}
	
	
	public static void main (String[] args) throws Exception{
		
		Class<?> clazz = Leech_MainM.class;
		java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
		java.io.File file = new java.io.File(url.toURI());
		System.setProperty("plugins.dir", file.getAbsolutePath());
		
		new ImageJ();
		
		openFiles();
		
		IJ.runPlugIn(clazz.getName(), "");
	}

	private static void openFiles() {
		JFileChooser fc = null;
		try {
			fc = new JFileChooser();
		}catch (Throwable e) {
			IJ.error("This plugin requires Java 2 or Swing.");
		}
		fc.setMultiSelectionEnabled(true);
		
		File dir = fc.getSelectedFile();
		if (dir==null) {
			String sdir = OpenDialog.getDefaultDirectory();
			if (sdir!=null)
				dir = new File(sdir);
		}
		if (dir!=null)
			fc.setCurrentDirectory(dir);
		
		int returnVal = fc.showOpenDialog(IJ.getInstance());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			IJ.error("Error");
		
		File[] files = fc.getSelectedFiles();
		if (files.length==0) { // getSelectedFiles does not work on some JVMs
			files = new File[1];
			files[0] = fc.getSelectedFile();
		}
		
		String path = fc.getCurrentDirectory().getPath()+Prefs.getFileSeparator();
		dir = fc.getCurrentDirectory();
		Opener opener = new Opener();
		ImagePlus[] images = new ImagePlus[files.length];
		
		for (int i=0; i<files.length; i++) {
			images[i] = opener.openImage(path, files[i].getName());
			images[i].show();
		}  
		
	}

}
