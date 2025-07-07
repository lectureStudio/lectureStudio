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

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.view.PresentationView;

/**
 * Abstract base presenter class for presentation-related functionality.
 * Extends the generic Presenter class with presentation-specific capabilities.
 *
 * @param <T> the type of presentation view this presenter controls must extend PresentationView.
 *
 * @author Alex Andres
 */
public abstract class PresentationPresenter<T extends PresentationView> extends Presenter<T> {

	/**
	 * Creates a new presentation presenter with the specified context and view.
	 *
	 * @param context the application context.
	 * @param view    the presentation view this presenter will control.
	 */
	public PresentationPresenter(ApplicationContext context, T view) {
		super(context, view);
	}

	/**
	 * Closes the associated presentation view.
	 */
	public void close() {
		view.close();
	}

	/**
	 * Returns the presentation view associated with this presenter.
	 *
	 * @return the presentation view.
	 */
	public PresentationView getPresentationView() {
		return view;
	}
}
