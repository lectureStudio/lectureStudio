/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.swing.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JComponent;

import dev.onvoid.webrtc.media.FourCC;
import dev.onvoid.webrtc.media.video.VideoBufferConverter;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoFrameBuffer;

import org.lecturestudio.core.PageMetrics;

public class VideoFrameConverter {

	public static BufferedImage convertVideoFrame(VideoFrame videoFrame, BufferedImage image) throws Exception {
		final VideoFrameBuffer buffer = videoFrame.buffer;
		final int width = buffer.getWidth();
		final int height = buffer.getHeight();

		if (isNull(image) || image.getWidth() != width || image.getHeight() != height) {
			if (nonNull(image)) {
				image.flush();
			}
			image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		}

		byte[] imageBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		VideoBufferConverter.convertFromI420(buffer, imageBuffer, FourCC.RGBA);

		buffer.release();

		return image;
	}

	public static BufferedImage convertVideoFrameToComponentSize(BufferedImage image, BufferedImage srcImage,
																 JComponent component) {
		Insets insets = component.getInsets();
		// Scale the video frame to the component size.
		double uiScale = component.getGraphicsConfiguration()
				.getDefaultTransform().getScaleX();
		int padW = insets.left + insets.right;
		int padH = insets.top + insets.bottom;
		int viewWidth = (int) ((component.getWidth() - padW) * uiScale);
		int viewHeight = (int) ((component.getHeight() - padH) * uiScale);

		return convertVideoFrame(image, srcImage, viewWidth, viewHeight);
	}

	public static BufferedImage convertVideoFrame(BufferedImage image, BufferedImage srcImage,
												  int imageWidth, int imageHeight) {
		final int width = srcImage.getWidth();
		final int height = srcImage.getHeight();

		final PageMetrics metrics = new PageMetrics(width, height);

		final var size = metrics.convert(imageWidth, imageHeight);

		int scaleWidth = (int) size.getWidth();
		int scaleHeight = (int) size.getHeight();

		// Return early if scaling dimensions are invalid (less than 1 pixel).
		if (scaleHeight < 1 || scaleWidth < 1) {
			return image;
		}

		// Create or reuse the target image with the scaled dimensions.
		// If the existing image has different dimensions, flush it and create a new one.
		if (isNull(image) || image.getWidth() != scaleWidth || image.getHeight() != scaleHeight) {
			if (nonNull(image)) {
				image.flush();
			}
			image = new BufferedImage(scaleWidth, scaleHeight, srcImage.getType());
		}

		// Scale the image using an affine transformation with bilinear interpolation.
		AffineTransform scalingTransform = new AffineTransform();
		scalingTransform.scale(scaleWidth / (double) width, scaleHeight / (double) height);
		AffineTransformOp scaleOp = new AffineTransformOp(scalingTransform, AffineTransformOp.TYPE_BILINEAR);

		image = scaleOp.filter(srcImage, image);

		/*
		final VideoFrameBuffer croppedBuffer = buffer.cropAndScale(0, 0, width, height, (int) size.getWidth(), (int) size.getHeight());
		final int cWidth = croppedBuffer.getWidth();
		final int cHeight = croppedBuffer.getHeight();

		if (isNull(image) || image.getWidth() != cWidth || image.getHeight() != cHeight) {
			if (nonNull(image)) {
				image.flush();
			}
			image = new BufferedImage(cWidth, cHeight, BufferedImage.TYPE_4BYTE_ABGR);
		}

		byte[] imageBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

		VideoBufferConverter.convertFromI420(croppedBuffer, imageBuffer, FourCC.RGBA);

		// Release resources.
		croppedBuffer.release();
		buffer.release();
		*/

		return image;
	}

}
