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

import java.nio.ByteBuffer;

import org.lecturestudio.core.graphics.Color;

public class AudacityColormapByteBgra implements Colormap<ByteBuffer> {

	private static final double[] values = new double[6];

	private static final Color[] colors = new Color[6];

	private final double minValue;

	private final double maxValue;


	static {
		values[0] = 0.0;
		colors[0] = new Color(210, 210, 210);
		values[1] = 0.5;
		colors[1] = new Color(96, 128, 255);
		values[2] = 0.7;
		colors[2] = new Color(230, 16, 230);
		values[3] = 0.9;
		colors[3] = new Color(255, 16, 192);
		values[4] = 0.95;
		colors[4] = new Color(255, 16, 16);
		values[5] = 1.0;
		colors[5] = Color.WHITE;
	}


	public AudacityColormapByteBgra(double minValue, double maxValue) {
		if (minValue >= maxValue) {
			throw new IllegalArgumentException("min >= max");
		}

		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public void setPixel(double value, ByteBuffer buffer, int offset) {
		double y = (value - this.minValue) / (this.maxValue - this.minValue);
		int exactIndex = exactIndex(y);

		if (exactIndex != -1) {
			setBgra(colors[exactIndex], buffer, offset);
		}
		else {
			int minIndex = minIndex(y);
			int maxIndex = maxIndex(y);

			if (minIndex == -1) {
				setBgra(colors[maxIndex], buffer, offset);
			}
			else if (maxIndex == -1) {
				setBgra(colors[minIndex], buffer, offset);
			}
			else {
				Color minColor = colors[minIndex];
				Color maxColor = colors[maxIndex];

				double y0 = values[minIndex];
				double y1 = values[maxIndex];
				double fraction = (y - y0) / (y1 - y0);

				setBgra(minColor.interpolate(maxColor, fraction), buffer, offset);
			}
		}
	}

	private static int exactIndex(double y) {
		for (int i = 0; i < 6; ++i) {
			if (y == values[i]) {
				return i;
			}
		}

		return -1;
	}

	private static int minIndex(double y) {
		if (y < values[0]) {
			return -1;
		}

		for (int i = 0; i < 5; ++i) {
			if (y < values[i + 1]) {
				return i;
			}
		}

		return 5;
	}

	private static int maxIndex(double y) {
		if (y > values[5]) {
			return -1;
		}

		for (int i = 5; i > 0; --i) {
			if (values[i - 1] < y) {
				return i;
			}
		}

		return 0;
	}

	private static void setBgra(Color color, ByteBuffer buffer, int offset) {
		buffer.put(offset, (byte) color.getBlue());
		buffer.put(offset + 1, (byte) color.getGreen());
		buffer.put(offset + 2, (byte) color.getRed());
		buffer.put(offset + 3, (byte) color.getOpacity());
	}
}
