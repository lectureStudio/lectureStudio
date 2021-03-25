/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.core.audio.filter;

/**
 * This audio filter increases or decreases the audio sample values based on the
 * provided scalar value.
 *
 * @author Alex Andres
 */
public class AudioVolumeFilter implements AudioFilter {

	private double scalar = 1;


	@Override
	public void process(byte[] data, int offset, int length) {
		length = Math.min(data.length, length);

		for (int i = 0; i < length; i += 2) {
			int value = (short) ((data[i + 1] << 8) | (data[i] & 0xFF));
			value *= scalar;

			// Clip
			if (value < Short.MIN_VALUE) {
				value = Short.MIN_VALUE;
			}
			if (value > Short.MAX_VALUE) {
				value = Short.MAX_VALUE;
			}

			data[i] = (byte) (value & 0xFF);
			data[i + 1] = (byte) ((value >> 8) & 0xFF);
		}
	}

	/**
	 * Set the new scalar value. A value of 1 causes no effect on the processed
	 * samples.
	 *
	 * @param scalar The new scalar.
	 */
	public void setVolumeScalar(double scalar) {
		this.scalar = scalar;
	}
}
