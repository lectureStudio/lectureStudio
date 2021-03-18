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

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class FontFileLocator {

	public static void find(Consumer<Path> callback, String extension) {
		String[] fontDirs = getFontDirectories();

		if (fontDirs == null) {
			// Nothing to look for.
			return;
		}

		for (String fontDir : fontDirs) {
			Path path = Paths.get(fontDir);

			listFiles(path, callback, extension);
		}
	}

	private static void listFiles(Path path, Consumer<Path> callback, String extension) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					listFiles(entry, callback, extension);
					continue;
				}

				if (entry.toString().endsWith(extension)) {
					callback.accept(entry);
				}
			}
		}
		catch (Exception e) {
			// Ignore.
		}
	}

	private static String[] getFontDirectories() {
		if (OsInfo.isLinux()) {
			return new String[] {
				System.getProperty("user.home") + "/.fonts",
				"/usr/local/fonts",
				"/usr/local/share/fonts",
				"/usr/share/fonts",
				"/usr/X11R6/lib/X11/fonts",
				"/usr/share/X11/fonts/Type1",
				"/usr/share/X11/fonts/TTF"
			};
		}
		else if (OsInfo.isMac()) {
			return new String[] {
				System.getProperty("user.home") + "/Library/Fonts/",
				"/Library/Fonts/",
				"/System/Library/Fonts/",
				"/Network/Library/Fonts/"
			};
		}
		else if (OsInfo.isWindows()) {
			return new String[] {
				System.getenv("SystemRoot") + "/Fonts"
			};
		}

		return null;
	}

}
