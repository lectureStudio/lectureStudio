package com.github.javaffmpeg;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Camera {

    /** Represents the current camera state: capturing or not. */
    private final AtomicBoolean open = new AtomicBoolean(false);

    private Demuxer demuxer;

    private String name;
    
    private String device;
    
    private String format;
    
    private BufferedImage image;


    public Camera(String name, String device, String format) {
    	this.name = name;
        this.device = device;
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public BufferedImage getImage() {
		if (open.get()) {
			try {
				MediaFrame mediaFrame = demuxer.readFrame();

				if (mediaFrame == null) {
					return null;
				}

				if (mediaFrame.getType() == MediaFrame.Type.VIDEO) {
					VideoFrame videoFrame = (VideoFrame) mediaFrame;

					int width = videoFrame.getWidth();
					int height = videoFrame.getHeight();

					if (image == null || image.getWidth() != width || image.getHeight() != height) {
						if (image != null) {
							image.flush();
							image = null;
						}
						image = new BufferedImage(videoFrame.getWidth(), videoFrame.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
					}

					byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

					ByteBuffer buffer = videoFrame.getData();
					buffer.get(imageData);

					return image;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
        return null;
    }

    public void open(int width, int height, double frameRate) throws JavaFFmpegException {
        if (!open.get()) {
            demuxer = new Demuxer();
            demuxer.setInputFormat(format);
            demuxer.setImageWidth(width);
            demuxer.setImageHeight(height);
            demuxer.setFramerate(frameRate);
            demuxer.open(device);

            open.set(true);
        }
    }

    public void close() {
        if (open.compareAndSet(true, false) && demuxer != null) {
            demuxer.close();
        }
    }

    public boolean isOpen() {
        return open.get();
    }

}
