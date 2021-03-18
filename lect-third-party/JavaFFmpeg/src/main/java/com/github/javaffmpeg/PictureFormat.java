package com.github.javaffmpeg;

public class PictureFormat {

	private PixelFormat format;
	
	private int width;
	
	private int height;
	
	
	public PictureFormat(int width, int height, PixelFormat format) {
		this.width = width;
		this.height = height;
		this.format = format;
	}

	/**
	 * @return the format
	 */
	public PixelFormat getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(PixelFormat format) {
		this.format = format;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isValid() {
		return width != 0 && height != 0 && format != PixelFormat.NONE;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		final PictureFormat other = (PictureFormat) obj;
		
		return width == other.width && height == other.height && format == other.format;
	}

	@Override
	public String toString() {
		return "PictureFormat: " + width + "x" + height + " " + format;
	}
	
}
