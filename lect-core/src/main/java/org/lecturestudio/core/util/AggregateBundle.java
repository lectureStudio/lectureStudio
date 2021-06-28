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

import static java.util.Objects.isNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A ResourceBundle whose content is aggregated from multiple bundles.
 * 
 * @author Alex Andres
 */
public class AggregateBundle extends ResourceBundle {

	private final Map<String, Object> contents;

	/** The locale for this bundle. */
	private final Locale locale;


	/**
	 * Creates a new AggregateBundle.
	 * 
	 * @param locale The locale for this bundle.
	 * @param bundleNames A list of bundles which shall be merged into this bundle.
	 * 
	 * @throws IOException if one or more bundles could not be loaded.
	 */
	public AggregateBundle(Locale locale, String... bundleNames) throws IOException {
		this.contents = new HashMap<>();
		this.locale = locale;

		load(bundleNames);
	}

	public void load(String... bundleNames) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		for (String name : bundleNames) {
			if (name.isEmpty()) {
				continue;
			}

			String resourcePath = name.replaceAll("\\.", "/");
			File bundlePath = new File(resourcePath);
			String parentPath = bundlePath.getParent().replace("\\", "/");
			String baseName = bundlePath.getName();

			scanForBundles(cl, parentPath, baseName);
		}
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public boolean containsKey(String key) {
		return contents.containsKey(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return new IteratorEnumeration<>(contents.keySet().iterator());
	}

	@Override
	protected Object handleGetObject(String key) {
		return contents.get(key);
	}

	private void scanForBundles(ClassLoader cl, String path, String baseName) throws IOException {
		Control control = Control.getControl(Control.FORMAT_DEFAULT);
		List<Locale> candidateLocales = control.getCandidateLocales(baseName, locale);
		Enumeration<URL> names = cl.getResources(path);

		candidateLocales.removeIf(locale -> locale == Locale.ROOT);

		while (names.hasMoreElements()) {
			final URL url = names.nextElement();
			String protocol = url.getProtocol();

			if (protocol.equals("file")) {
				File parent = new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8));

				if (parent.isDirectory()) {
					String[] list = parent.list();

					if (isNull(list)) {
						continue;
					}

					for (String name : list) {
						File file = new File(name);

						if (!file.isDirectory() && name.startsWith(baseName) && name.endsWith(".properties")) {
							// Extract locale from file name.
							Locale tagLocale = FileUtils.extractLocale(name, baseName);

							if (!candidateLocales.contains(tagLocale)) {
								continue;
							}

							File resource = new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8) + "/" + name);

							loadBundle(resource.toURI().toURL().openStream());
						}
					}
				}
			}
			else if (protocol.equals("jar")) {
				String searchPath = path + "/" + baseName;
				String urlPath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
				urlPath = urlPath.substring(0, urlPath.indexOf("!"));

				File jarFile;

				try {
					jarFile = Paths.get(new URL(urlPath).toURI()).toFile();
				}
				catch (URISyntaxException e) {
					throw new IOException(e);
				}

				JarFile jar = new JarFile(jarFile);
				Enumeration<JarEntry> entries = jar.entries();

				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();

					if (!entry.isDirectory() && name.startsWith(searchPath) && name.endsWith(".properties")) {
						// Extract locale from file name.
						Locale tagLocale = FileUtils.extractLocale(name, baseName);

						if (!candidateLocales.contains(tagLocale)) {
							continue;
						}

						loadBundle(jar.getInputStream(entry));
					}
				}

				jar.close();
			}
		}
	}

	private void loadBundle(InputStream stream) throws IOException {
		if (isNull(stream)) {
			return;
		}

		try (stream) {
			Properties props = new Properties();
			props.load(stream);

			Enumeration<Object> keys = props.keys();

			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();

				if (!contents.containsKey(key)) {
					contents.put(key, props.getProperty(key));
				}
			}
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
