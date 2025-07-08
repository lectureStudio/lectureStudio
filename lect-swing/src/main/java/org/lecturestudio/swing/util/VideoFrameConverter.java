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

import dev.onvoid.webrtc.media.FourCC;
import dev.onvoid.webrtc.media.video.VideoBufferConverter;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoFrameBuffer;

import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JComponent;

import org.lecturestudio.core.PageMetrics;

public class VideoFrameConverter {

	public static BufferedImage convertVideoFrameToComponentSize(
			VideoFrame videoFrame, BufferedImage image, JComponent component)
			throws Exception {
		Insets insets = component.getInsets();
		// Scale the video frame to the component size.
		double uiScale = component.getGraphicsConfiguration()
				.getDefaultTransform().getScaleX();
		int padW = insets.left + insets.right;
		int padH = insets.top + insets.bottom;
		int viewWidth = (int) ((component.getWidth() - padW) * uiScale);
		int viewHeight = (int) ((component.getHeight() - padH) * uiScale);

		return convertVideoFrame(videoFrame, image, viewWidth, viewHeight);
	}

	public static BufferedImage convertVideoFrame(VideoFrame videoFrame,
												  BufferedImage image, int imageWidth, int imageHeight)
			throws Exception {
		final VideoFrameBuffer buffer = videoFrame.buffer;

		final int width = buffer.getWidth();
		final int height = buffer.getHeight();

		final PageMetrics metrics = new PageMetrics(width, height);

		final var size = metrics.convert(imageWidth, imageHeight);

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
		try {
			croppedBuffer.release();
		}
		catch (Exception e) {
			// May happen if the buffer is already released.
		}

		return image;
	}

}
