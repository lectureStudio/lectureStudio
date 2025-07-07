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

package org.lecturestudio.core.view;

import org.lecturestudio.core.graphics.Color;

/**
 * Interface for a presentation view that displays slides and provides presentation functionality.
 * Extends the base View interface with presentation-specific capabilities.
 *
 * @author Alex Andres
 */
public interface PresentationView extends View {

	/**
	 * Closes the presentation view.
	 */
	void close();

	/**
	 * Sets the background color of the presentation view.
	 *
	 * @param color The color to set as a background.
	 */
	void setBackgroundColor(Color color);

	/**
	 * Sets the visibility state of the presentation view.
	 *
	 * @param visible true to make the view visible, false to hide it.
	 */
	void setVisible(boolean visible);

	/**
	 * Checks whether the presentation view is currently visible.
	 *
	 * @return true if the view is visible, false otherwise.
	 */
	boolean isVisible();

	/**
	 * Sets an action to be executed when the view becomes visible.
	 *
	 * @param action The action to execute on visibility change.
	 */
	void setOnVisible(Action action);

	/**
	 * Adds an overlay to the slide view.
	 *
	 * @param overlay The overlay to be added.
	 */
	void addOverlay(SlideViewOverlay overlay);

	/**
	 * Removes an overlay from the slide view.
	 *
	 * @param overlay The overlay to be removed.
	 */
	void removeOverlay(SlideViewOverlay overlay);

	/**
	 * Sets the presentation view context containing presentation-specific configuration.
	 *
	 * @param context The context to be applied to this presentation view.
	 */
	void setPresentationViewContext(PresentationViewContext context);

}
