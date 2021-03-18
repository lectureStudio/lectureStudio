package org.bushe.swing;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

public final class ImageUtils {

	private ImageUtils() {
		
	}
	
	/**
	 * Gets image dimensions for given file.
	 * 
	 * @param imgFile image file.
	 * @return dimension of the image.
	 * 
	 * @throws IOException if the file is not a known image
	 */
	public static Dimension getImageDimension(File imgFile) throws IOException {
		int dot = imgFile.getName().lastIndexOf(".");
		if (dot == -1)
			throw new IOException("No extension for file: " + imgFile.getAbsolutePath());
		
		String suffix = imgFile.getName().substring(dot + 1);
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		
		if (iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new FileImageInputStream(imgFile);
				reader.setInput(stream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				
				return new Dimension(width, height);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				reader.dispose();
			}
		}

		throw new IOException("Unknown image: " + imgFile.getAbsolutePath());
	}
	
}
