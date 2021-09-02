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

package org.lecturestudio.core.pdf.pdfbox;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * This class extends the fundamental {@link GraphicsConfiguration} to be used by PdfGraphics2DStream for PDF creation.
 * 
 * @author Alex Andres
 */
public class PDFGraphicsConfiguration extends GraphicsConfiguration {
	
	/** Use this to get a transparency color model. */
	private static final BufferedImage BI_ARGB = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	
	/** Use this to get an opaque color model. */
	private static final BufferedImage BI_OPAQUE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

	
	@Override
	public BufferedImage createCompatibleImage(int width, int height, int transparency) {
		if (transparency == Transparency.OPAQUE) {
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		else {
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}
	}

	@Override
	public BufferedImage createCompatibleImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	@Override
    public ColorModel getColorModel() {
        return BI_ARGB.getColorModel();
    }

	@Override
    public ColorModel getColorModel(int transparency) {
		if (transparency == Transparency.OPAQUE) {
			return BI_OPAQUE.getColorModel();
		}
		else {
			return BI_ARGB.getColorModel();
		}
    }

	@Override
    public AffineTransform getDefaultTransform() {
        return new AffineTransform();
    }

	@Override
    public AffineTransform getNormalizingTransform() {
        return new AffineTransform();
    }

	@Override
    public GraphicsDevice getDevice() {
        return new PDFGraphicsDevice(this);
    }

	@Override
	public Rectangle getBounds() {
		return null;
	}

}
