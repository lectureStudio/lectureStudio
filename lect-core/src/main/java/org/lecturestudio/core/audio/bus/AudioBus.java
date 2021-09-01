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

package org.lecturestudio.core.audio.bus;

import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.BusEvent;

/**
 * The AudioBus implements the publish-subscribe paradigm. It dispatches audio
 * events to the subscribers.
 *
 * @author Alex Andres
 */
public class AudioBus {

	/** The singleton instance. */
	private static AudioBus instance = null;

	/** The event bus instance. */
	private final EventBus bus = new EventBus();


	/**
	 * Meant to be private as for singleton purposes.
	 */
	private AudioBus() {

	}

	/**
	 * Get the {@link AudioBus} singleton instance.
	 *
	 * @return the {@link AudioBus} instance.
	 */
	private static AudioBus getInstance() {
		if (instance == null) {
			instance = new AudioBus();
		}
		return instance;
	}

	/**
	 * Register all subscriber methods on the subscriber to receive audio
	 * events.
	 *
	 * @param subscriber The subscriber to register.
	 */
	public static void register(final Object subscriber) {
		getInstance().bus.register(subscriber);
	}

	/**
	 * Unregister all subscriber methods on the registered subscriber.
	 *
	 * @param subscriber The subscriber to unregister.
	 */
	public static void unregister(final Object subscriber) {
		getInstance().bus.unregister(subscriber);
	}

	/**
	 * Publish an audio event to all registered subscribers.
	 *
	 * @param event The event to publish.
	 */
	public static void post(final BusEvent event) {
		getInstance().bus.post(event);
	}

	/**
	 * Get the {@link EventBus} instance used by this {@link AudioBus}.
	 *
	 * @return the {@link EventBus} instance.
	 */
	public static EventBus get() {
		return getInstance().bus;
	}

}
