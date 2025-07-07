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

package org.lecturestudio.presenter.api.view;

import java.util.function.Predicate;

import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewLayer;

/**
 * Main view interface for the presenter application. Defines the behavior and capabilities
 * of the application's main window.
 *
 * @author Alex Andres
 */
public interface MainView extends View {

	/**
	 * Gets the bounds of the view in the display space.
	 *
	 * @return the bounds of the view as a Rectangle2D.
	 */
	Rectangle2D getViewBounds();

	/**
	 * Closes the view and releases associated resources.
	 */
	void closeView();

	/**
	 * Hides the view without closing it.
	 */
	void hideView();

	/**
	 * Removes a specific view from the specified layer.
	 *
	 * @param view  the view to remove.
	 * @param layer the layer from which to remove the view.
	 */
	void removeView(View view, ViewLayer layer);

	/**
	 * Shows the specified view on the specified layer.
	 *
	 * @param view  the view to show.
	 * @param layer the layer on which to show the view.
	 */
	void showView(View view, ViewLayer layer);

	/**
	 * Sets the fullscreen mode of the view.
	 *
	 * @param fullscreen true to enable fullscreen mode, false to disable it.
	 */
	void setFullscreen(boolean fullscreen);

	/**
	 * Sets the visibility of the menu.
	 *
	 * @param visible true to make the menu visible, false to hide it.
	 */
	void setMenuVisible(boolean visible);

	/**
	 * Sets the action to be performed when a key event occurs.
	 *
	 * @param action the predicate that handles the key event.
	 */
	void setOnKeyEvent(Predicate<KeyEvent> action);

	/**
	 * Sets the action to be performed when the bounds of the view change.
	 *
	 * @param action the consumer action that handles the new bounds.
	 */
	void setOnBounds(ConsumerAction<Rectangle2D> action);

	/**
	 * Sets the action to be performed when the focus state of the view changes.
	 *
	 * @param action the consumer action that handles the focus state change.
	 */
	void setOnFocus(ConsumerAction<Boolean> action);

	/**
	 * Sets the action to be performed when the view is shown.
	 *
	 * @param action the action to execute when the view is shown.
	 */
	void setOnShown(Action action);

	/**
	 * Sets the action to be performed when the view is about to close.
	 *
	 * @param action the action to execute when the view is about to close.
	 */
	void setOnClose(Action action);

}
