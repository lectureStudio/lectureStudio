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

package org.lecturestudio.swing.service;

import static java.util.Objects.nonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.lecturestudio.core.app.view.Screens;
import org.lecturestudio.core.service.DisplayService;
import org.lecturestudio.core.util.ImmutableObservableList;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.Screen;

/**
 * The @{code AwtDisplayService} implements the {@link DisplayService} using
 * the Java AWT {@link java.awt.GraphicsEnvironment} API to monitor connected
 * screens.
 *
 * @author Alex Andres
 */
public class AwtDisplayService implements DisplayService {

	/** The frequency in milliseconds to check for new connected screens. */
	private static final int IDLE = 1000;

	/** The list that maintains the current state of connected screens. */
	private final ObservableList<Screen> screenList = new ObservableArrayList<>();

	/** The read-only list that is returned on request. */
	private final ObservableList<Screen> roScreenList = new ImmutableObservableList<>(screenList);

	/** The timer that periodically runs the task that listens for screen changes. */
	private final Timer timer = new Timer();

	/** The task that listens for screen changes. */
	private TimerTask timerTask;


	/**
	 * Creates a new @{code AwtDisplayService}.
	 */
	public AwtDisplayService() {
		init();
	}

	@Override
	public ObservableList<Screen> getScreens() {
		return roScreenList;
	}

	public void stop() {
		// Stop listening for new screens.
		if (nonNull(timerTask)) {
			timerTask.cancel();
		}

		timer.purge();
	}

	private void init() {
		Screen[] screens = Screens.getAllScreens();

		screenList.addAll(Arrays.asList(screens));

		// Start listening for new connected screens.
		timerTask = new TimerTask() {

			@Override
			public void run() {
				listenForScreens();
			}
		};

		timer.schedule(timerTask, 1000, IDLE);
	}

	private void listenForScreens() {
		Screen[] screens = Screens.getConnectedScreens();

		int count = screens.length;
		if (count == screenList.size()) {
			return;
		}

		List<Screen> connectedScreens = Arrays.asList(screens);

		// Remove disconnected screens.
		screenList.removeIf(screen -> !connectedScreens.contains(screen));

		// Add connected screens.
		for (Screen screen : connectedScreens) {
			if (!screenList.contains(screen)) {
				screenList.add(screen);
			}
		}
	}
}
