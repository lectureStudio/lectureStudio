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

package org.lecturestudio.core.io;

public abstract class BitConverter {

	/** Lookup table for byte->float conversion */
	public static final float[] BYTE_TO_FLOAT_LUT;

	static {
		BYTE_TO_FLOAT_LUT = new float[256];
		for (int i = 0; i < BYTE_TO_FLOAT_LUT.length; i++)
			BYTE_TO_FLOAT_LUT[i] = i / 255f;
	}

	/**
	 * Converts the specified byte array into a big endian integer.
	 *
	 * @param b The byte array.
	 *
	 * @return The specified byte array converted into a big endian integer.
	 */
	public static int getBigEndianInt(byte[] b) {
		return ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
	}

	/**
	 * Converts the specified integer value into a big endian byte array.
	 *
	 * @param value The integer value.
	 *
	 * @return The specified integer value converted into a big endian byte array.
	 */
	public static byte[] getBigEndianBytes(int value) {
		byte[] b = new byte[4];
		b[0] = (byte) ((value >>> 24) & 0xFF);
		b[1] = (byte) ((value >>> 16) & 0xFF);
		b[2] = (byte) ((value >>> 8) & 0xFF);
		b[3] = (byte) (value & 0xFF);

		return b;
	}

	/**
	 * Converts the specified integer value into a little endian byte array.
	 *
	 * @param value The integer value.
	 *
	 * @return The specified integer value converted into a little endian byte array.
	 */
	public static byte[] getLittleEndianBytes(int value) {
		byte[] b = new byte[4];
		b[0] = (byte) (value);
		b[1] = (byte) (value >> 8);
		b[2] = (byte) (value >> 16);
		b[3] = (byte) (value >> 24);

		return b;
	}

	/**
	 * Converts the specified short value into a little endian byte array.
	 *
	 * @param value The short value.
	 *
	 * @return The specified integer value converted into a little endian byte array.
	 */
	public static byte[] getLittleEndianBytes(short value) {
		byte[] b = new byte[2];
		b[0] = (byte) (value);
		b[1] = (byte) (value >> 8);

		return b;
	}

}
