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

package org.lecturestudio.core.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lecturestudio.core.beans.Converter;

public class RegexConverter implements Converter<String, String> {

	private final Pattern pattern;


	public RegexConverter(String regex) {
		pattern = Pattern.compile(regex);
	}

	@Override
	public String to(String value) {
		return value;
	}

	@Override
	public String from(String value) {
		Matcher matcher = pattern.matcher(value);

		return matcher.find() ? matcher.group(1) : value;
	}
}
