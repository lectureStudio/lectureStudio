package com.github.javaffmpeg;

import java.nio.ByteBuffer;

public class VideoFrame extends MediaFrame {

	private ByteBuffer data;
	
	private PixelFormat format;
	
	private int width;
	private int height;
	
	
	public VideoFrame() {
		this(null, 0, 0, null);
	}
	
	public VideoFrame(ByteBuffer data, int width, int height, PixelFormat format) {
		this.data = data;
		this.width = width;
		this.height = height;
		this.format = format;
	}

	public ByteBuffer getData() {
		return data;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public PixelFormat getPixelFormat() {
		return format;
	}
	
	public PictureFormat getPictureFormat() {
		return new PictureFormat(width, height, format);
	}
	
	@Override
	public Type getType() {
		return Type.VIDEO;
	}
	
	@Override
	public boolean hasFrame() {
		return data != null && data.capacity() > 1;
	}
	
}
