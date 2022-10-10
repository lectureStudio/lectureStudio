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

package org.lecturestudio.presenter.api.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.presenter.api.model.ScreenSourceVideoFrame;

public class ScreenFrameConverter {

	public static BufferedImage createBufferedImage(ScreenSourceVideoFrame frame, int width, int height) {
		final ByteBuffer buf = frame.buffer;
		final Dimension dim = frame.frameSize;
		final int frameWidth = frame.frameSize.width;
		final int frameHeight = frame.frameSize.height;

		PageMetrics metrics = new PageMetrics(frameWidth, frameHeight);
		Dimension2D size = metrics.convert(width, height);

		int imageWidth = (int) (size.getWidth());
		int imageHeight = (int) (size.getHeight());

		// Copy frame to buffered image.
		int bytesPerPixel = 4;
		int bufferSize = dim.width * dim.height * bytesPerPixel;

		DataBufferByte dataBuffer = new DataBufferByte(bufferSize);

		WritableRaster raster = Raster.createInterleavedRaster(dataBuffer,
				dim.width,
				dim.height,
				dim.width * bytesPerPixel,
				bytesPerPixel,
				new int[] { 2, 1, 0, 3 },
				null);

		ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
				new int[] { 8, 8, 8, 8 },
				true,
				false,
				ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

		BufferedImage tempImage = new BufferedImage(colorModel, raster, false, null);
		byte[] imageBuffer = ((DataBufferByte) tempImage.getRaster().getDataBuffer()).getData();

		buf.get(imageBuffer);

		// Draw frame.
//		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
//
//		Graphics2D g2 = image.createGraphics();
//		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//		g2.drawImage(tempImage, 0, 0, imageWidth, imageHeight, null);
//		g2.dispose();

		return tempImage;
	}

}
