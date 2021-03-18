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

package org.lecturestudio.core.bus;

import com.google.common.eventbus.SubscriberExceptionHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The EventBus implements the publish-subscribe paradigm. It dispatches any
 * kind of events to the subscribers.
 *
 * @author Alex Andres
 */
public class EventBus {

	private static final Logger LOG = LogManager.getLogger(EventBus.class);

	/** The handler for subscriber exceptions. */
	private final SubscriberExceptionHandler exceptionHandler = (exception, context) -> {
		if (LOG.isErrorEnabled()) {
			LOG.error("Could not dispatch event to " + context.getSubscriberMethod(), exception);
		}
	};

	/** The internal event bus. */
	private final com.google.common.eventbus.EventBus bus;


	/**
	 * Create an EventBus.
	 */
	public EventBus() {
		bus = new com.google.common.eventbus.EventBus(exceptionHandler);
	}

	/**
	 * Register all subscriber methods on the subscriber to receive events
	 * published on this event bus.
	 *
	 * @param subscriber The subscriber to register.
	 */
	public void register(final Object subscriber) {
		bus.register(subscriber);
	}

	/**
	 * Unregister all subscriber methods on the registered subscriber.
	 *
	 * @param subscriber The subscriber to unregister.
	 */
	public void unregister(final Object subscriber) {
		bus.unregister(subscriber);
	}

	/**
	 * Publish an event to all registered subscribers.
	 *
	 * @param event The event to publish.
	 */
	public void post(Object event) {
		bus.post(event);
	}

}
