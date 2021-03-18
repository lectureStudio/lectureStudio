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

package org.lecturestudio.media.playback;

import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.bus.EventBus;

public abstract class PlaybackContext extends ApplicationContext {

	private final BooleanProperty seeking = new BooleanProperty();

	private final DoubleProperty primarySelection = new DoubleProperty();


	/**
	 * Create a new ApplicationContext instance with the given parameters.
	 *
	 * @param dataLocator The application resource data locator.
	 * @param config      The application configuration.
	 * @param dict        The application dictionary.
	 * @param eventBus    The application event data bus.
	 * @param audioBus    The audio event bus.
	 */
	public PlaybackContext(AppDataLocator dataLocator, Configuration config,
			Dictionary dict, EventBus eventBus, EventBus audioBus) {
		super(dataLocator, config, dict, eventBus, audioBus);
	}

	public boolean isSeeking() {
		return seeking.get();
	}

	public void setSeeking(boolean enable) {
		seeking.set(enable);
	}

	public BooleanProperty seekingProperty() {
		return seeking;
	}

	public double getPrimarySelection() {
		return primarySelection.get();
	}

	public void setPrimarySelection(double value) {
		primarySelection.set(value);
	}

	public DoubleProperty primarySelectionProperty() {
		return primarySelection;
	}
}
