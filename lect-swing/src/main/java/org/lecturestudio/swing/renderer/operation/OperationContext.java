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

package org.lecturestudio.swing.renderer.operation;

import java.awt.CompositeContext;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Implementation of {@link CompositeContext} using an {@link ImageOperation}.
 * 
 * @author Tobias
 * 
 */
public class OperationContext implements CompositeContext {

	private final ImageOperation operation;

	public OperationContext(ImageOperation operation) {
		this.operation = operation;
	}

	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
		// apply operation to each pair of pixels in src and dstIn and write
		// result back to dstOut.

		int width = Math.min(src.getWidth(), dstIn.getWidth());
		int height = Math.min(src.getHeight(), dstIn.getHeight());
		int length = width * height;

		int[] srcPixel = new int[4];
		int[] dstPixel = new int[4];

		int[] srcPixels = new int[length];
		int[] dstPixels = new int[length];

		src.getDataElements(0, 0, width, height, srcPixels);
		dstIn.getDataElements(0, 0, width, height, dstPixels);

		for (int idx = 0; idx < length; idx++) {
			// pixel format is INT_ARGB.
			int pixel = srcPixels[idx];
			srcPixel[0] = (pixel >> 24) & 0xFF; // A
			srcPixel[1] = (pixel >> 16) & 0xFF; // R
			srcPixel[2] = (pixel >> 8) & 0xFF; // G
			srcPixel[3] = pixel & 0xFF; // B

			pixel = dstPixels[idx];
			dstPixel[0] = (pixel >> 24) & 0xFF;
			dstPixel[1] = (pixel >> 16) & 0xFF;
			dstPixel[2] = (pixel >> 8) & 0xFF;
			dstPixel[3] = pixel & 0xFF;

			dstPixels[idx] = operation.execute(srcPixel, dstPixel);
		}
		dstOut.setDataElements(0, 0, width, height, dstPixels);
	}

	public void dispose() {
	}

}
