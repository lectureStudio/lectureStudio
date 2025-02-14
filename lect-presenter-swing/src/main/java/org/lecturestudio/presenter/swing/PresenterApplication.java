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

package org.lecturestudio.presenter.swing;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationFactory;
import org.lecturestudio.swing.app.LectSwingPreloader;
import org.lecturestudio.swing.app.SwingApplication;

public class PresenterApplication extends SwingApplication {

	/**
	 * The entry point of the application. This method calls the static {@link
	 * #launch(String[], Class)} method to fire up the application.
	 *
	 * @param args the main method's arguments.
	 */
	public static void main(String[] args) {
		// Start with preloader.
		PresenterApplication.launch(args, LectSwingPreloader.class);
	}

	@Override
	public ApplicationFactory createApplicationFactory() {
		return new PresenterFactory();
	}

	@Override
	protected void initInternal(String[] args) throws ExecutableException {
		// JavaFX should be initialized prior to the app itself,
		// e.g.,by creating a fake JFXPanel instance.
		javafx.embed.swing.JFXPanel dummy = new javafx.embed.swing.JFXPanel();

		// Ensure that JavaFX platform keeps running.
		javafx.application.Platform.setImplicitExit(false);

		super.initInternal(args);
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		// When shutting down the application, ensure that the JavaFX threads
		// don't prevent JVM exit.
		javafx.application.Platform.exit();

		super.stopInternal();
	}
}
