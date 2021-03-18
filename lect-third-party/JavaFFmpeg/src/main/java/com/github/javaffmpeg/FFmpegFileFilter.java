package com.github.javaffmpeg;

import java.io.File;
import java.io.FileFilter;

public class FFmpegFileFilter implements FileFilter {

	private static String[] decline = { "mf" };
	
	@Override
	public boolean accept(File file) {
		if (file == null)
			return false;

		String fileName = file.getName();
		int i = fileName.lastIndexOf('.');
		
		if (i > 0) {
			String extension = fileName.substring(i + 1).toLowerCase();
			
			for (String ext : decline) {
				if (extension.equals(ext)) {
					return false;
				}
			}
		}
		
		// Accept everything.
		return true;
	}

}
