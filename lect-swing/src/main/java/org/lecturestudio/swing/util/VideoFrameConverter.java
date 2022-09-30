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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JComponent;

import org.lecturestudio.core.PageMetrics;

public class VideoFrameConverter {

	public static BufferedImage convertVideoFrameToComponentSize(
			VideoFrame videoFrame, BufferedImage image, JComponent component)
			throws Exception {
		// Scale video frame to the component size.
		double uiScale = component.getGraphicsConfiguration()
				.getDefaultTransform().getScaleX();
		int viewWidth = (int) (component.getWidth() * uiScale);
		int viewHeight = (int) (component.getHeight() * uiScale);

		return convertVideoFrame(videoFrame, image, viewWidth, viewHeight);
	}

	public static BufferedImage convertVideoFrame(VideoFrame videoFrame,
			BufferedImage image, int imageWidth, int imageHeight)
			throws Exception {
		VideoFrameBuffer buffer = videoFrame.buffer;
		int width = buffer.getWidth();
		int height = buffer.getHeight();

		PageMetrics metrics = new PageMetrics(width, height);

		var size = metrics.convert(imageWidth, imageHeight);

		buffer = buffer.cropAndScale(0, 0, width, height, (int) size.getWidth(), (int) size.getHeight());
		width = buffer.getWidth();
		height = buffer.getHeight();

		if (isNull(image) || image.getWidth() != width || image.getHeight() != height) {
			if (nonNull(image)) {
				image.flush();
			}
			image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		}

		byte[] imageBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

		VideoBufferConverter.convertFromI420(buffer, imageBuffer, FourCC.RGBA);

		// Release resources.
		buffer.release();

		return image;
	}

}
