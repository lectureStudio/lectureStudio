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

package org.lecturestudio.javafx.event;

import javafx.event.Event;
import javafx.event.EventType;

import org.lecturestudio.core.view.Screen;

/**
 * An {@link ScreenActionEvent} represents a mouse click event on a screen
 * rectangle in the {@code ScreenView}.
 *
 * @author Alex Andres
 */
public class ScreenActionEvent extends Event {

	/** The only valid EventType for this event. */
	private static final EventType<ScreenActionEvent> ACTION = new EventType<>(Event.ANY, "SCREEN_ACTION");

	/** The selected screen. */
	private final Screen screen;


	/**
	 * Creates a new {@code ScreenActionEvent} with an event type of {@code ACTION}.
	 * The source and target of the event is set to {@code null}.
	 *
	 * @param screen The selected screen.
	 */
	public ScreenActionEvent(Screen screen) {
		super(ACTION);

		this.screen = screen;
	}

	/**
	 * @return The selected screen.
	 */
	public Screen getScreen() {
		return screen;
	}
}
