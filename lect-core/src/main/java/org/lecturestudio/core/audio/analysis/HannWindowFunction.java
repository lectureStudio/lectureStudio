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

import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.cos;

/**
 * Hann window function implementation.
 * 
 * @author Alex Andres
 */
public class HannWindowFunction extends WindowFunction {

	/**
	 * Create the {@link HannWindowFunction} with specified window length.
	 *
	 * @param length the length of the window.
	 */
	public HannWindowFunction(int length) {
		super(length);
	}

	@Override
	public void apply(float[] frame) {
		if (frame.length != length) {
			throw new IllegalArgumentException("Window and frame have different lengths!");
		}

		for (int i = 0; i < length; i++) {
			frame[i] = (float) (frame[i] * multipliers[i]);
		}
	}

	@Override
	public void apply(double[] frame) {
		if (frame.length != length) {
			throw new IllegalArgumentException("Window and frame have different lengths!");
		}

		for (int i = 0; i < length; i++) {
			frame[i] = frame[i] * multipliers[i];
		}
	}

	@Override
	protected void createWindow() {
		for (int i = 0; i < length; i++) {
			multipliers[i] = 0.5 * (1 - cos(2 * PI * i / length));
		}
	}

}
