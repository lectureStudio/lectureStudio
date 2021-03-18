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

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

public class DesktopUtils {

	public static void browseURI(URI uri) throws Exception {
		String scheme = uri.getScheme();
		String path = uri.getPath();

		if (nonNull(scheme) && scheme.equals("file")) {
			openFile(new File(path));
			return;
		}

		// For compatibility reasons.
		if (isNull(scheme) || !scheme.startsWith("http")) {
			path = "http://" + path;
			uri = URI.create(path);
		}

		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(uri);
		}
		else {
			if (OsInfo.isLinux()) {
				Command.execute(new String[] { "xdg-open", path });
			}
			else if (OsInfo.isMac()) {
				Command.execute(new String[] { "open", path });
			}
		}
	}

	public static void openFile(File file) throws Exception {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
			Desktop desktop = Desktop.getDesktop();
			desktop.open(file);
		}
		else {
			String filePath = file.getPath();

			if (OsInfo.isWindows()) {
				if (filePath.startsWith("/") || filePath.startsWith("\\")) {
					filePath = filePath.substring(1);
				}
				Command.execute(filePath);
			}
			else if (OsInfo.isLinux()) {
				Command.execute(new String[] { "xdg-open", filePath });
			}
			else if (OsInfo.isMac()) {
				Command.execute(new String[] { "open", filePath });
			}
		}
	}

}
