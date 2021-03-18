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

package org.lecturestudio.core.audio.analysis;

import java.util.Arrays;

/**
 * Abstract implementation of a window function used for signal processing.
 * 
 * @author Alex Andres
 */
public abstract class WindowFunction {

	/** The length of the window. */
	protected final int length;

	/** The window values. */
	protected final double[] multipliers;


	/**
	 * Create a new window function.
	 *
	 * @param length the window length.
	 */
	public WindowFunction(int length) {
		this.length = length;
		this.multipliers = new double[length];

		createWindow();
	}

	/**
	 * Returns the values of this window function.
	 *
	 * @return the values of this window function.
	 */
	public double[] getValues() {
		return Arrays.copyOf(multipliers, multipliers.length);
	}

	/**
	 * Copies the values of this window function, truncating or padding with
	 * zeros (if necessary) so the copy has the specified length.
	 *
	 * @param length The length of the copied values to be returned.
	 *
	 * @return a copy of the original values of this window function.
	 */
	public double[] getValues(int length) {
		return Arrays.copyOf(multipliers, length);
	}

	/**
	 * Normalizes the values of this window function.
	 */
	public void normalize() {
		final int len = multipliers.length;
		double sum = 0;

		for (double value : multipliers) {
			if (!Double.isNaN(value)) {
				sum += value;
			}
		}
		for (int i = 0; i < len; i++) {
			if (Double.isNaN(multipliers[i])) {
				multipliers[i] = Double.NaN;
			}
			else {
				multipliers[i] = multipliers[i] / sum;
			}
		}
	}

	/**
	 * Initialize the window multipliers.
	 */
	abstract protected void createWindow();

	/**
	 * Apply this window function on the provided frame. The window will be
	 * applied in-place.
	 *
	 * @param frame The input frame.
	 */
	abstract public void apply(float[] frame);

	/**
	 * Apply this window function on the provided frame. The window will be
	 * applied in-place.
	 *
	 * @param frame The input frame.
	 */
	abstract public void apply(double[] frame);

}
