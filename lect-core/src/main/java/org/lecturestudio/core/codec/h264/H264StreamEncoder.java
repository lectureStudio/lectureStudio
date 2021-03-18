/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.core.codec.h264;

import com.github.javaffmpeg.Codec;
import com.github.javaffmpeg.CodecID;
import com.github.javaffmpeg.Encoder;
import com.github.javaffmpeg.Image;
import com.github.javaffmpeg.JavaFFmpegException;
import com.github.javaffmpeg.MediaPacket;
import com.github.javaffmpeg.MediaType;
import com.github.javaffmpeg.Options;
import com.github.javaffmpeg.PixelFormat;
import com.github.javaffmpeg.VideoFrame;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.codec.VideoEncoder;
import org.lecturestudio.core.geometry.Rectangle2D;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * H.264 video encoder implementation.
 *
 * @author Alex Andres
 */
public class H264StreamEncoder implements VideoEncoder {

	private static final Logger LOG = LogManager.getLogger(H264StreamEncoder.class);

	/** Internal FFmpeg video encoder. */
	private Encoder encoder;

	/** The start time of encoding. */
	private long startTime;

	/** The timestamp of the last encoded frame. */
	private long timestamp;

	/** Temporary image used for cropping. */
	private BufferedImage croppedImage;

	/** The encoder configuration. */
	private VideoCodecConfiguration codecConfiguration;


	/**
	 * Create an H264StreamEncoder with the specified encoder configuration.
	 *
	 * @param codecConfiguration The encoder configuration.
	 */
	public H264StreamEncoder(VideoCodecConfiguration codecConfiguration) {
	 	initialize(codecConfiguration);
	}

	@Override
	public ByteBuffer encode(BufferedImage image) {
		long now = System.currentTimeMillis();

		if (startTime == 0) {
			startTime = now;
		}

		timestamp = now - startTime;

		BufferedImage croppedImage = prepareImage(image);

		int width = croppedImage.getWidth();
		int height = croppedImage.getHeight();

		PixelFormat format = Image.getPixelFormat(image);

		if (format == null) {
			return null;
		}

		VideoFrame frame = new VideoFrame(Image.createImageBuffer(croppedImage), width, height, format);

		try {
			MediaPacket packet = encoder.encodeVideo(frame);

            image.flush();
            image = null;

			if (packet != null) {
				return packet.getData();
			}

			return null;
		}
		catch (JavaFFmpegException e) {
			LOG.error("Encode video frame failed.", e);
		}

		return null;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	private void initialize(VideoCodecConfiguration codecConfiguration) {
		this.codecConfiguration = codecConfiguration;

		double frameRate = codecConfiguration.getFrameRate();
		int bitRate = codecConfiguration.getBitRate();
        Rectangle2D viewRectangle = codecConfiguration.getViewRect();
		String preset = codecConfiguration.getPreset();

		int width  = (int) viewRectangle.getWidth();
		int height = (int) viewRectangle.getHeight();

		Options videoOptions = new Options();
		videoOptions.put("tune", "zerolatency");
		videoOptions.put("preset", preset);

        LOG.debug("Using H.264 Preset: " + preset);

		try {
		  	encoder = new Encoder(Codec.getEncoderById(CodecID.H264));
			encoder.setMediaType(MediaType.VIDEO);
			encoder.setPixelFormat(PixelFormat.YUV420P);
			encoder.setImageWidth(width);
			encoder.setImageHeight(height);
			encoder.setGOPSize((int) frameRate);
			encoder.setBitrate(bitRate * 1000);
			encoder.setFramerate(frameRate);
			encoder.open(videoOptions);
		}
		catch (JavaFFmpegException e) {
			LOG.error("Create H264 encoder failed.", e);
		}
	}

    private BufferedImage prepareImage(BufferedImage image) {
        Rectangle2D viewRectangle = codecConfiguration.getViewRect();

        int confX = (int) viewRectangle.getX();
        int confY = (int) viewRectangle.getY();
        int confWidth = (int) viewRectangle.getWidth();
        int confHeight = (int) viewRectangle.getHeight();

        // If nothing to do.
        if (confWidth == image.getWidth() && confHeight == image.getHeight()) {
            return image;
        }

        // Crop image.
        if (croppedImage == null || croppedImage.getWidth() != confWidth || croppedImage.getHeight() != confHeight) {
        	if (croppedImage != null) {
        		croppedImage.flush();
        		croppedImage = null;
        	}
        	croppedImage = new BufferedImage(confWidth, confHeight, image.getType());
        }
        
        if (confX + confWidth <= image.getWidth() && confY + confHeight <= image.getHeight()) {
	        Graphics2D g = croppedImage.createGraphics();
	        g.drawImage(image.getSubimage(confX, confY, confWidth, confHeight), 0, 0, null);
	        g.dispose();
        }
        else if (confWidth <= image.getWidth() && confHeight <= image.getHeight()) {
        	LOG.warn("Could not crop image with configured values. Cropping from original.");

            croppedImage = image.getSubimage(0, 0, confWidth, confHeight);
        }
        else {
        	LOG.warn("Could not crop image with configured values. Aborting.");

            return image;
        }

        image.flush();
        image = null;

        return croppedImage;
    }

}
