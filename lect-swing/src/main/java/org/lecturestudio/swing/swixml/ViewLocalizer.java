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

package org.lecturestudio.swing.swixml;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.swixml.Localizer;

public class ViewLocalizer extends Localizer {

	private final ResourceBundle resourceBundle;


	ViewLocalizer(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	@Override
	public String getString(String key) {
		String result = null;

		if (isUsable() && resourceBundle.containsKey(key)) {
			result = resourceBundle.getString(key);
		}

		return isNull(result) ? key : result;
	}

	@Override
	public boolean isUsable() {
		return nonNull(resourceBundle);
	}

	@Override
	public void setLocale(Locale locale) {
		// Ignored
	}

	@Override
	public void setResourceBundle(String bundleName) throws
			MissingResourceException {
		// Ignored
	}
}
