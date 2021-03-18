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

package org.lecturestudio.core.spi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SpiLoader<T extends ServiceProvider> {

	private static final Logger LOG = LogManager.getLogger(SpiLoader.class);
	
	protected ServiceLoader<T> loader;


	protected SpiLoader(File[] repos, String packageName, Class<T> neededClass) {
		createServiceLoader(repos, packageName, neededClass);
	}

	private void createServiceLoader(File[] repos, String packageName, Class<T> neededClass) {
		try {
			List<URL> urls = new ArrayList<>();

			for (File repository : repos) {
				urls.addAll(loadRepository(repository, packageName, neededClass));
			}

			URL[] libs = urls.toArray(new URL[0]);

			loader = ServiceLoader.load(neededClass, getClassLoader(libs));
		}
		catch (Exception e) {
			LOG.error("Create service loader failed.", e);
		}
	}

	public T getProvider(String providerName) {
		try {
			for (T service : loader) {
				if (service.getProviderName().equals(providerName)) {
					return service;
				}
			}
		}
		catch (ServiceConfigurationError e) {
			LOG.error("Get provider failed.", e);
		}

		return null;
	}

	public String[] getProviderNames() {
		List<String> adapters = new ArrayList<>();

		try {
			for (T service : loader) {
				adapters.add(service.getProviderName());
			}
		}
		catch (ServiceConfigurationError e) {
			LOG.error("Get provider names failed.", e);
		}

		return adapters.toArray(new String[0]);
	}

	protected boolean hasClass(File file, String packageName, Class<T> neededClass) {
		if (packageName == null) {
			packageName = "";
		}

		String dirSearched = packageName.replace(".", "/");
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
		}
		catch (Exception e) {
			closeZip(zipFile);
			return false;
		}

		for (Enumeration<? extends ZipEntry> zipEntries = zipFile.entries(); zipEntries.hasMoreElements();) {
			String entryName = zipEntries.nextElement().getName();

			if (!entryName.startsWith(dirSearched) || !entryName.toLowerCase().endsWith(".class")) {
				continue;
			}
			
			entryName = entryName.substring(0, entryName.length() - ".class".length());
			entryName = entryName.replace("/", ".");
			
			try {
				URLClassLoader ucl = new URLClassLoader(new URL[] { file.toURI().toURL() });
				Class<?> clazz = ucl.loadClass(entryName);

				if (neededClass.isAssignableFrom(clazz)) {
					ucl.close();
					closeZip(zipFile);
					return true;
				}
				ucl.close();
			}
			catch (Exception e) {
				closeZip(zipFile);
				return false;
			}
		}
		
		closeZip(zipFile);
		return false;
	}

	protected ClassLoader getClassLoader(URL[] libs) {
		ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
		URLClassLoader urlClassLoader = new URLClassLoader(libs, currentThreadClassLoader);
		Thread.currentThread().setContextClassLoader(urlClassLoader);

		return Thread.currentThread().getContextClassLoader();
	}

	private List<URL> loadRepository(File file, String packageName, Class<T> neededClass) throws Exception {
		List<URL> list = new ArrayList<>();

		if (file.isFile() && file.canRead() && file.getName().endsWith(".jar")) {
			if (hasClass(file, packageName, neededClass)) {
				list.add(file.toURI().toURL());
			}
		}
		else if (file.isDirectory()) {
			for (File f : Objects.requireNonNull(file.listFiles())) {
				list.addAll(loadRepository(f, packageName, neededClass));
			}
		}

		return list;
	}

	private void closeZip(ZipFile zipFile) {
		if (zipFile == null) {
			return;
		}

		try {
			zipFile.close();
		}
		catch (IOException e) {
			LOG.error("Close zip failed.", e);
		}
	}

}
