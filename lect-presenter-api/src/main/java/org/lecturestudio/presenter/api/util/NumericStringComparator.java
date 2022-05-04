/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.util;

import java.util.Comparator;

/**
 * Comma separated numerical string comparator implementation.
 *
 * @author Alex Andres
 */
public class NumericStringComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		try {
			String[] parts1 = o1.split(",");
			String[] parts2 = o2.split(",");

			int count = Math.min(parts1.length, parts2.length);

			for (int i = 0; i < count; i++) {
				String s1 = o1.split(",")[i].trim();
				String s2 = o2.split(",")[i].trim();

				Integer n1 = Integer.parseInt(s1);
				Integer n2 = Integer.parseInt(s2);

				if (!n1.equals(n2)) {
					return n1.compareTo(n2);
				}
			}
			return 0;
		}
		catch (Throwable e) {
			return 0;
		}
	}
}
