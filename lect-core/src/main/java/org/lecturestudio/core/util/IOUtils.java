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

import java.util.Locale;

/**
 * Utility class providing I/O related helper methods.
 *
 * @author Alex Andres
 */
public class IOUtils {

	/**
	 * Formats a byte size into a human-readable string with binary units (KiB, MiB, etc.).
	 * The result is formatted with one decimal place and the appropriate unit suffix.
	 *
	 * @param bytes the size in bytes to format.
	 *
	 * @return a human-readable string representation of the byte size (e.g., "1.5 MiB").
	 */
	public static String formatSize(final long bytes) {
		final String[] units = { "B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB" };
		final int base = 1024;

		if (bytes < base) {
			return bytes + " " + units[0];
		}

		final int exponent = (int) (Math.log(bytes) / Math.log(base));
		final String unit = units[exponent];

		return String.format(Locale.getDefault(), "%.1f %s",
				bytes / Math.pow(base, exponent), unit);
	}

	/**
	 * Shortens a string to a specified maximum length by truncating and adding ellipsis.
	 * If the string is already shorter than the maximum length, it is returned unchanged.
	 *
	 * @param str       the string to shorten.
	 * @param maxLength the maximum length of the resulting string (including ellipsis).
	 *
	 * @return the shortened string, or the original if it's already short enough.
	 *
	 * @throws IllegalArgumentException if maxLength is less than 4.
	 */
	public static String shortenString(String str, int maxLength) {
		if (str == null) {
			return null;
		}
		if (maxLength < 4) {
			throw new IllegalArgumentException("Max length must be at least 4 to accommodate dots");
		}
		if (str.length() <= maxLength) {
			return str;
		}
		return str.substring(0, maxLength - 3) + "...";
	}
}
