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

package org.lecturestudio.core.presenter;

import java.io.File;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.view.View;

/**
 * Abstract base class for main presenters in the application.
 * <p>
 * This class provides the foundation for presenters that manage the main view
 * of the application. It handles common functionality while requiring subclasses
 * to implement specific file handling and argument processing.
 *
 * @param <T> The type of View this presenter manages, must extend the View interface.
 *
 * @author Alex Andres
 */
public abstract class MainPresenter<T extends View> extends Presenter<T> {

	/**
	 * Constructs a new MainPresenter with the specified application context and view.
	 *
	 * @param context The application context providing access to application-wide services.
	 * @param view    The view managed by this presenter.
	 */
	public MainPresenter(ApplicationContext context, T view) {
		super(context, view);
	}

	/**
	 * Opens and processes the specified file.
	 * Implementing classes should handle the file according to the application's requirements.
	 *
	 * @param file The file to be opened and processed.
	 */
	abstract public void openFile(final File file);

	/**
	 * Sets the command line arguments passed to the application.
	 * Implementing classes should process these arguments as needed.
	 *
	 * @param args An array of command line arguments.
	 */
	abstract public void setArgs(String[] args);

}
