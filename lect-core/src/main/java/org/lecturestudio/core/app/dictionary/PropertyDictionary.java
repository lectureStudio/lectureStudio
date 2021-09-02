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

package org.lecturestudio.core.app.dictionary;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dictionary implementation for loading dictionary translation files in the
 * properties format.
 *
 * @see ResourceBundle
 *
 * @author Alex Andres
 */
public class PropertyDictionary implements Dictionary {

	/** Logger for {@link PropertyDictionary} */
	private static final Logger LOG = LogManager.getLogger(PropertyDictionary.class);

	/** The resource bundle containing locale-specific translations. */
	private ResourceBundle bundle;

	/** The locale of this dictionary. */
	private Locale locale;

	/** The dictionary file paths. */
	private String[] dictPaths;


	/**
	 * Creates a new instance of {@link PropertyDictionary} and loads the
	 * dictionary file for the specified locale.
	 *
	 * @param locale         The dictionary localization.
	 * @param dictionaryPath One or more paths to dictionary files.
	 *
	 * @throws NullPointerException If the dictionary path is {@code null}.
	 */
	public PropertyDictionary(Locale locale, String... dictionaryPath) {
		if (dictionaryPath == null) {
			throw new NullPointerException("Dictionary path must be set.");
		}

		this.dictPaths = dictionaryPath;

		setLocale(locale);
	}

	/**
	 * Returns the value to which the key is mapped in this dictionary. If the
	 * dictionary contains an entry for the specified key, the associated value
	 * is returned, otherwise the string {@code [*]} is returned.
	 */
	@Override
	public String get(String key) throws NullPointerException {
		try {
			return bundle.getString(key);
		}
		catch (MissingResourceException e) {
			LOG.warn("Missing resource translation", e);
			return "[*]";
		}
	}

	/**
	 * Returns the current locale of the dictionary.
	 *
	 * @return the current locale.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the locale of the dictionary. If the new locale differs from the
	 * current one, the new dictionary file is loaded.
	 *
	 * @param locale The new locale.
	 */
	public void setLocale(Locale locale) {
		if (this.locale != null && this.locale.equals(locale)) {
			return;
		}

		List<ResourceBundle> bundles = new ArrayList<>();

		for (String path : dictPaths) {
			ResourceBundle bundle = ResourceBundle.getBundle(path, locale);

			if (bundle != null) {
				bundles.add(bundle);
			}
		}

		this.locale = locale;
		this.bundle = new AggregateBundle(bundles);
	}

	@Override
	public boolean contains(String key) {
		return bundle.containsKey(key);
	}



	/**
	 * A ResourceBundle whose content is aggregated from multiple bundles.
	 */
	private static class AggregateBundle extends ResourceBundle {

		private final Map<String, Object> contents = new HashMap<>();

		/**
		 * Creates a new {@link AggregateBundle} with the contents of the specified
		 * resource bundles.
		 *
		 * @param bundles A list of bundles which shall be merged into this
		 *                bundle.
		 */
		AggregateBundle(List<ResourceBundle> bundles) {
			for (ResourceBundle bundle : bundles) {
				Enumeration<String> keys = bundle.getKeys();

				while (keys.hasMoreElements()) {
					String key = keys.nextElement();

					if (!contents.containsKey(key)) {
						contents.put(key, bundle.getObject(key));
					}
				}
			}
		}

		@Override
		public Enumeration<String> getKeys() {
			return new IteratorEnumeration<>(contents.keySet().iterator());
		}

		@Override
		protected Object handleGetObject(String key) {
			return contents.get(key);
		}

	}



	/**
	 * An Enumeration implementation that wraps an Iterator.
	 *
	 * @param <T> The enumerated type.
	 */
	private static class IteratorEnumeration<T> implements Enumeration<T> {

		private final Iterator<T> source;

		/**
		 * Creates a new IterationEnumeration.
		 *
		 * @param source The source iterator.
		 */
		IteratorEnumeration(Iterator<T> source) {
			this.source = source;
		}

		@Override
		public boolean hasMoreElements() {
			return source.hasNext();
		}

		@Override
		public T nextElement() {
			return source.next();
		}

	}

}
