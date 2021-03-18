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

/**
 * This class extends the fundamental GraphicsDevice to be used by
 * PDFGraphicsConfiguration for PDF creation. This device is a printer.
 * 
 * @author Alex Andres
 */
public class PDFGraphicsDevice extends GraphicsDevice {

	private GraphicsConfiguration gc;
	
	
	/**
	 * Create a new PDFGraphicsDevice with a specific PDFGraphicsConfiguration.
	 * 
	 * @param gc The GraphicsConfiguration.
	 */
	public PDFGraphicsDevice(PDFGraphicsConfiguration gc) {
		this.gc = gc;
	}
	
	@Override
	public int getType() {
		return GraphicsDevice.TYPE_PRINTER;
	}

	@Override
	public String getIDstring() {
		return getClass().getName();
	}

	@Override
	public GraphicsConfiguration[] getConfigurations() {
		return new GraphicsConfiguration[] { gc };
	}

	@Override
	public GraphicsConfiguration getDefaultConfiguration() {
		return gc;
	}

}
