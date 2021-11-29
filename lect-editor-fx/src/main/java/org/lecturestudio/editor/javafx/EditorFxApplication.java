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

import dev.onvoid.webrtc.logging.Logging;

import org.lecturestudio.core.app.ApplicationFactory;
import org.lecturestudio.javafx.app.JavaFxApplication;

public class EditorFxApplication extends JavaFxApplication {

	/**
	 * The entry point of the application. This method calls the static
	 * {@link #launch(String[], Class)} method to fire up the application.
	 *
	 * @param args the main method's arguments.
	 */
	public static void main(String[] args) {
		Logging.logThreads(true);

		// Start with pre-loader.
		EditorFxApplication.launch(args, EditorFxPreloader.class);
	}

	@Override
	public ApplicationFactory createApplicationFactory() {
		return new EditorFxFactory();
	}
}
