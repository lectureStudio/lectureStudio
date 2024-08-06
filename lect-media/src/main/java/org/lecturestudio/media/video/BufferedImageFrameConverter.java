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

package org.lecturestudio.media.video;

import static java.util.Objects.*;
import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_BGR24;

import java.awt.image.BufferedImage;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import org.lecturestudio.core.geometry.Dimension2D;

/**
 * Converts video frames into BufferedImages.
 *
 * @author Alex Andres
 */
public class BufferedImageFrameConverter implements VideoFrameConverter<BufferedImage> {

	private final Java2DFrameConverter converter = new Java2DFrameConverter();

	private FFmpegFrameFilter filter;

	private Dimension2D imageSize;

	private BufferedImage image;


	/**
	 * Sets the image size of the converted video frame.
	 *
	 * @param size The size of the image into which the frames will be converted.
	 */
	public void setImageSize(Dimension2D size) {
		this.imageSize = size;
	}

	@Override
	public BufferedImage convert(Frame frame) throws Exception {
		requireNonNull(frame);

		if (isNull(frame.image)) {
			throw new IllegalArgumentException("Frame is not a video frame");
		}

		// Init frame filter if required.
		if (isNull(filter) && nonNull(imageSize)) {
			String scale = String.format("scale=%dx%d", (int) imageSize.getWidth(), (int) imageSize.getHeight());

			filter = new FFmpegFrameFilter(scale, frame.imageWidth, frame.imageHeight);
			filter.setPixelFormat(AV_PIX_FMT_BGR24);
			filter.start();
		}

		// Apply frame filters, e.g., resizing.
		if (nonNull(filter)) {
			filter.push(frame);
			frame = filter.pull();
		}

		// Convert frame into a buffered image.
		BufferedImage converted = converter.convert(frame);

		// Create a new buffered image if required.
		if (isNull(image) || image.getWidth() != frame.imageWidth || image.getHeight() != frame.imageHeight) {
			image = new BufferedImage(frame.imageWidth, frame.imageHeight, BufferedImage.TYPE_INT_RGB);
		}

		// Convert type byte to type int image.
		var g2d = image.createGraphics();
		g2d.drawImage(converted, null, 0, 0);
		g2d.dispose();

		return image;
	}

	@Override
	public void dispose() {
		if (nonNull(filter)) {
			try {
				filter.stop();
				filter.release();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
