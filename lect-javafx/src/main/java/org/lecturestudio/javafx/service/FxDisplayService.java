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

package org.lecturestudio.javafx.service;

import javafx.collections.ListChangeListener;

import org.lecturestudio.core.service.DisplayService;
import org.lecturestudio.core.util.ImmutableObservableList;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.javafx.beans.converter.Rectangle2DConverter;

/**
 * The @{code FxDisplayService} implements the {@link DisplayService} using
 * the JavaFX {@link javafx.stage.Screen} API to monitor connected screens.
 *
 * @author Alex Andres
 */
public class FxDisplayService implements DisplayService {

	/** The list that maintains the current state of connected screens. */
	private final ObservableList<Screen> screenList = new ObservableArrayList<>();

	/** The read-only list that is returned on request. */
	private final ObservableList<Screen> roScreenList = new ImmutableObservableList<>(screenList);

	/** Internal 'javafx.stage.Screen.getScreens' change listener. */
	private final ListChangeListener<javafx.stage.Screen> changeListener = this::screensChanged;

	/**
	 * Creates a new @{code FxDisplayService}.
	 */
	public FxDisplayService() {
		init();
	}

	@Override
	public ObservableList<Screen> getScreens() {
		return roScreenList;
	}

	private void init() {
		javafx.collections.ObservableList<javafx.stage.Screen> screens = javafx.stage.Screen.getScreens();

		// Get connected screens.
		for (javafx.stage.Screen fxScreen : screens) {
			screenList.add(createScreen(fxScreen));
		}

		// Listen for changes.
		screens.addListener(changeListener);
	}

	private void screensChanged(ListChangeListener.Change<? extends javafx.stage.Screen> change) {
		while (change.next()) {
			if (change.wasAdded()) {
				for (javafx.stage.Screen fxScreen : change.getAddedSubList()) {
					screenList.add(createScreen(fxScreen));
				}
			}
			else if (change.wasRemoved()) {
				for (javafx.stage.Screen fxScreen : change.getRemoved()) {
					screenList.remove(createScreen(fxScreen));
				}
			}
		}
	}

	private Screen createScreen(javafx.stage.Screen fxScreen) {
		return new Screen(Rectangle2DConverter.INSTANCE.from(fxScreen.getBounds()));
	}
}
