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
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.lecturestudio.core.app.configuration.Configuration;

public class FileUtils {

	/**
	 * The default path resolves to 'user.home'.
	 */
	public static final String DEFAULT_PATH = System.getProperty("user.home");


	public static Path getContextPath(Configuration config, String pathContext) {
		return getContextPath(config, pathContext, DEFAULT_PATH);
	}

	public static Path getContextPath(Configuration config, String pathContext, String defaultPath) {
		if (isNull(defaultPath) || defaultPath.isEmpty()) {
			defaultPath = DEFAULT_PATH;
		}

		Map<String, String> contextPaths = config.getContextPaths();
		String pathStr = contextPaths.getOrDefault(pathContext, defaultPath);
		Path dirPath = Paths.get(pathStr);

		if (Files.notExists(dirPath) || !Files.isDirectory(dirPath)) {
			dirPath = Paths.get(defaultPath);
		}

		return dirPath;
	}

	public static File ensureExtension(File file, String extension) {
		String path = file.getAbsolutePath();

		if (!path.endsWith(extension)) {
			return new File(path + extension);
		}

		return file;
	}

	public static File stripExtension(File file) {
		String path = file.getPath();
		int extensionIndex = path.lastIndexOf(".");
		if (extensionIndex != -1) {
			path = path.substring(0, extensionIndex);
		}
		
		return new File(path);
	}
	
	public static String stripExtension(String path) {
		int extensionIndex = path.lastIndexOf(".");
		if (extensionIndex != -1) {
			path = path.substring(0, extensionIndex);
		}
		
		return path;
	}
	
	public static String getExtension(String path) {
		int extensionIndex = path.lastIndexOf(".");
		int pathSeparatorIndex = Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\"));
		int index = (pathSeparatorIndex > extensionIndex ? -1 : extensionIndex);

		if (index == -1) {
			return "";
		}
		
		return path.substring(index + 1);
	}

	/**
	 * Returns the decoded path string with %20 in original path string replaced
	 * by white space character.
	 */
	public static String decodePath(String path) {
		try {
			return new URI(null, path, null).getPath();
		}
		catch (Exception e) {
			return path;
		}
	}

	public static Locale extractLocale(String path, String baseName) {
		String tag = stripExtension(path);
		tag = tag.substring(tag.lastIndexOf(baseName) + baseName.length());
		tag = tag.replace("_", "-");

		if (tag.startsWith("-")) {
			tag = tag.substring(1);
		}

		return Locale.forLanguageTag(tag);
	}

	/**
	 * Creates the directory named by this abstract pathname, including any
	 * necessary but nonexistent parent directories. Note that if this operation
	 * fails it may have succeeded in creating necessary parent directories.
	 *
	 * @return {@code true} if and only if the directory was created, along with
	 * all necessary parent directories, otherwise {@code false}.
	 */
	public static boolean create(String path) {
		File file = new File(path);
		boolean created = false;
		
		if (!file.exists()) {
			created = file.mkdirs();
		}
		
		return created;
	}

	public static void copyJarResource(String jarPath, String source, String destDir, List<String> skipList) throws IOException {
		final JarFile jarFile = new JarFile(jarPath);
		boolean foundExactMatch = false;

		for (final Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
			JarEntry entry = e.nextElement();
			String entryName = entry.getName();
			File file = new File(entryName);

			if (!file.toPath().normalize().startsWith(source)) {
				continue;
			}

			if (entryName.equals(source)) {
				entryName = getFileName(entryName);

				foundExactMatch = true;
			}
			else {
				entryName = entryName.substring(source.length());
				if (entryName.endsWith(File.separator)) {
					entryName = entryName.substring(0, entryName.length() - 1);
				}
			}

			File dest = new File(destDir, entryName);

			if (entry.isDirectory()) {
				dest.mkdir();
			}
			else {
				if (nonNull(skipList) && skipList.contains(FileUtils.getExtension(entryName))) {
					continue;
				}

				// Copy file.
				InputStream inputStream = jarFile.getInputStream(entry);
				Files.copy(inputStream, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
				inputStream.close();

				if (foundExactMatch) {
					break;
				}
			}
		}

		jarFile.close();
	}

	public static String getFileName(String filePath) {
		int index = filePath.lastIndexOf("\\");

		if (index == -1) {
			index = filePath.lastIndexOf("/");
		}
		if (index != -1) {
			filePath = filePath.substring(index + 1);
		}

		return filePath;
	}

	public static String[] getResourceListing(String path, Predicate<String> predicate) throws Exception {
		URL dirURL = FileUtils.class.getResource(path);

		if (dirURL == null) {
			dirURL = ClassLoader.getSystemResource(path);
		}
		if (dirURL == null) {
			return new String[0];
		}

		if (dirURL.getProtocol().equals("file")) {
			List<String> result = new ArrayList<>();
			Path srcPath = Paths.get(dirURL.toURI());
			String srcPathName = srcPath.toString();
			
			Files.walkFileTree(srcPath, new SimpleFileVisitor<>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					String filePath = file.toString();
					
					if (!attrs.isDirectory() && predicate.test(filePath)) {
						String relativePath = filePath.substring(srcPathName.length());
						relativePath = relativePath.replace("\\", "/");
						
						if (relativePath.startsWith("/")) {
							relativePath = relativePath.substring(1);
						}
						
						result.add(path + "/" + relativePath);
					}
					
					return FileVisitResult.CONTINUE;
				}
			});
			
			return result.toArray(new String[0]);
		}
		else if (dirURL.getProtocol().equals("jar")) {
			String searchPath = path;
			
			if (searchPath.startsWith("/")) {
				searchPath = searchPath.substring(1);
			}

			String urlPath = dirURL.getPath();
			urlPath = urlPath.substring(0, urlPath.indexOf("!"));

			File jarFile = Paths.get(new URL(urlPath).toURI()).toFile();

			JarFile jar = new JarFile(jarFile);
			Enumeration<JarEntry> entries = jar.entries();
			Set<String> result = new HashSet<>();

			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				File file = new File(name);

				if (file.toPath().normalize().startsWith(searchPath) && predicate.test(name)) {
					String relativePath = name.substring(searchPath.length() + 1);
					
					if (!relativePath.isEmpty()) {
						result.add(path + "/" + relativePath);
					}
				}
			}

			jar.close();
			
			return result.toArray(new String[0]);
		}
		
		throw new IOException("Cannot list files for path: " + path);
	}

	public static byte[] getByteArray(File file) throws IOException {
		Path path = Paths.get(file.toURI());
		return Files.readAllBytes(path);
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (String child : children) {
				boolean success = deleteDir(new File(dir, child));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	public static String shortenPath(String path, int limit) {
		if (path.length() <= limit) {
			return path;
		}

		String SHORTENER_ELLIPSE = " ... ";

		char[] shortPathArray = new char[limit];
		char[] pathArray = path.toCharArray();
		char[] ellipseArray = SHORTENER_ELLIPSE.toCharArray();

		int pathindex = pathArray.length - 1;
		int shortpathindex = limit - 1;

		// fill the array from the end
		int i = 0;
		for (; i < limit; i++) {
			if (pathArray[pathindex - i] != '/' && pathArray[pathindex - i] != '\\') {
				shortPathArray[shortpathindex - i] = pathArray[pathindex - i];
			}
			else {
				break;
			}
		}
		// check how much space is left
		int free = limit - i;

		if (free < SHORTENER_ELLIPSE.length()) {
			// fill the beginning with ellipse
			System.arraycopy(ellipseArray, 0, shortPathArray, 0, ellipseArray.length);
		}
		else {
			// fill the beginning with path and leave room for the ellipse
			int j = 0;
			for (; j + ellipseArray.length < free; j++) {
				shortPathArray[j] = pathArray[j];
			}
			// ... add the ellipse
			for (int k = 0; j + k < free; k++) {
				shortPathArray[j + k] = ellipseArray[k];
			}
		}
		return new String(shortPathArray);
	}

	public static void zipFile(File inZipFile, File outZipFile) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(outZipFile);
				ZipOutputStream zipOut = new ZipOutputStream(fos)) {
			zipFile(inZipFile, inZipFile.getName(), zipOut);
		}
	}

	private static void zipFile(File inZipFile, String fileName, ZipOutputStream zipOut) throws IOException {
		if (inZipFile.isHidden()) {
			return;
		}
		if (inZipFile.isDirectory()) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			}
			else {
				zipOut.putNextEntry(new ZipEntry(fileName + "/"));
				zipOut.closeEntry();
			}

			File[] children = inZipFile.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}

			return;
		}

		FileInputStream fis = new FileInputStream(inZipFile);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}
}
