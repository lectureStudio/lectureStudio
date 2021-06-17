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

package org.lecturestudio.core.util;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

public class ImageUtils {

    /**
     * Creates a buffered image of a given width and height filled with a given color.
     *
     * @param width
     * @param height
     * @param color
     * @return
     */
    public static BufferedImage createBufferedImage(int width, int height, Color color) {
        BufferedImage image = createBufferedImage(width, height);
        Graphics2D g = image.createGraphics();

        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();

        return image;
    }

    /**
     * Creates a buffered image with a given width and height.
     *
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage createBufferedImage(int width, int height) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = { 8, 8, 8, 8 };
        int[] bOffs = { 2, 1, 0, 3 }; // bgra
        ColorModel colorModel = new ComponentColorModel(cs, nBits, true, false,
                Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);

        WritableRaster wr = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                width, height,
                width * 4, 4,
                bOffs, null);

        return new BufferedImage(colorModel, wr, false, null);
    }
}
