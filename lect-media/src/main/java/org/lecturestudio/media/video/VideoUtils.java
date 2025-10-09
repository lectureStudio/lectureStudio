/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

import static java.util.Objects.nonNull;

import org.lecturestudio.core.geometry.Dimension2D;

/**
 * Utility class providing methods for video processing operations.
 */
public class VideoUtils {

	/**
	 * Generates FFmpeg filter commands for cropping and scaling video frames.
	 * This method calculates appropriate cropping dimensions and positions to maintain
	 * the content's aspect ratio when scaling to the target width.
	 *
	 * @param frameWidth  The width of the original video frame.
	 * @param frameHeight The height of the original video frame.
	 * @param contentSize The desired content dimensions, determines the target aspect ratio.
	 * @param targetWidth The desired width of the output frame.
	 *
	 * @return A string containing FFmpeg filter commands for cropping and scaling
	 *         If contentSize is valid, returns "crop=w:h:x:y,scale=width:-1"
	 *         Otherwise, returns a simple scaling filter "scale=width:-1"
	 */
	public static String getFFmpegFrameFilters(int frameWidth, int frameHeight, Dimension2D contentSize,
											   int targetWidth) {
		final String filters;

		if (nonNull(contentSize) && contentSize.getWidth() > 0 && contentSize.getHeight() > 0) {
			double contentAspectRatio = contentSize.getWidth() / contentSize.getHeight();
			double frameAspectRatio = (double) frameWidth / frameHeight;

			int cropWidth;
			int cropHeight;
			int cropX;
			int cropY;

			if (contentAspectRatio > frameAspectRatio) { // Letterbox (horizontal bars)
				cropWidth = frameWidth;
				cropHeight = (int) Math.round(frameWidth / contentAspectRatio);
				cropX = 0;
				cropY = (frameHeight - cropHeight) / 2;
			}
			else { // Pillarbox (vertical bars)
				cropHeight = frameHeight;
				cropWidth = (int) Math.round(frameHeight * contentAspectRatio);
				cropX = (frameWidth - cropWidth) / 2;
				cropY = 0;
			}

			filters = String.format("crop=%d:%d:%d:%d,scale=%d:-1", cropWidth, cropHeight, cropX, cropY, targetWidth);
		}
		else {
			// Fallback to simple scaling if no content size is available
			filters = String.format("scale=%d:-1", targetWidth);
		}
		return filters;
	}
}
