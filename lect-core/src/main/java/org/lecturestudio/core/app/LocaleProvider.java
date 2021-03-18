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

package org.lecturestudio.core.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lecturestudio.core.util.FileUtils;

/**
 * Locale helper class to conveniently handle application locales. Application
 * internationalisation dictionaries are located in "i18n" resource folders.
 *
 * @author Alex Andres
 */
public class LocaleProvider {

	/**
	 * Get a list of supported locales which are based on the files located
	 * in the "i18n" folder.
	 *
	 * @return the available localizations.
	 */
	public List<Locale> getLocales() throws Exception {
		List<Locale> locales = new ArrayList<>();

		// Load only files which have '.properties' as extension.
		String[] listing = FileUtils.getResourceListing("/resources/i18n",
				(name) -> name.endsWith(".properties"));

		for (String fileName : listing) {
			String tag = fileName.substring(0, fileName.lastIndexOf("."));
			tag = tag.substring(tag.indexOf("_") + 1);
			tag = tag.replace("_", "-");

			locales.add(Locale.forLanguageTag(tag));
		}

		return locales;
	}

	/**
	 * Get the locale that best matches the specified locale. If no match can be
	 * found, the first available locale is returned.
	 *
	 * @param locale The locale for which to find the best match.
	 *
	 * @return The locale that best matches the provided locale.
	 *
	 * @throws Exception - if the application locales could not be loaded.
	 */
	public Locale getBestSupported(Locale locale) throws Exception {
		List<Locale> locales = getLocales();

		// Try to find an exact match.
		var result = locales.stream().filter(l -> l.equals(locale)).findFirst();
		if (result.isPresent()) {
			return result.get();
		}

		// Compare by language.
		result = locales.stream()
				.filter(l -> l.getLanguage().equals(locale.getLanguage()))
				.findFirst();

		return result.orElseGet(() -> locales.get(0));
	}

}
