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

package org.lecturestudio.editor.javafx;

import javafx.scene.Node;
import javafx.scene.Scene;

import org.lecturestudio.core.model.VersionInfo;
import org.lecturestudio.javafx.app.JavaFxPreloader;
import org.lecturestudio.javafx.control.Splash;

public class EditorFxPreloader extends JavaFxPreloader {

	@Override
	protected Node createView(final Scene scene) {
		scene.getStylesheets().add("resources/css/splash.css");

		Splash splash = new Splash();
		splash.setVersion(VersionInfo.getAppVersion());

		return splash;
	}

}
