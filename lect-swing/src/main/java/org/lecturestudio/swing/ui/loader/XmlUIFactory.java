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

package org.lecturestudio.swing.ui.loader;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.io.ResourceLoader;
import org.lecturestudio.swing.ui.CMenuBar;
import org.lecturestudio.swing.window.MainWindow;

public abstract class XmlUIFactory {

	public static CMenuBar createMenuBar(String file, MainWindow window, Dictionary dict) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);

		SAXParser parser = factory.newSAXParser();
		MenuDocumentHandler handler = new MenuDocumentHandler(window, dict);

		File menuFile = new File(ResourceLoader.getResourceURL(file).getPath());

		if (menuFile.exists()) {
			parser.parse(menuFile, handler);
		}
		else {
			parser.parse(ResourceLoader.getResourceAsStream(file), handler);
		}

		return handler.getMenuBar();
	}

}
