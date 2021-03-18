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

package org.lecturestudio.core.app;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.bus.event.ControllerEvent;
import org.lecturestudio.core.controller.CommandController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Asynchronous {@link CommandController} that executes ControllerEvents in
 * receiving order.
 *
 * @param <T> The type of the controller event.
 *
 * @author Alex Andres
 */
public abstract class AsyncCommandController<T extends ControllerEvent> extends CommandController<T> {

	private static final Logger LOG = LogManager.getLogger(AsyncCommandController.class);

	/** A thread safe event queue. */
	private final BlockingDeque<Callable<Boolean>> eventQueue = new LinkedBlockingDeque<>();

	/** The event queue worker used by a thread. */
	private final Runnable worker = () -> {
		try {
			processEvents();
		}
		catch (Exception e) {
			LOG.error("Could not process controller events.", e);
		}
	};

	/** The thread that is responsible for event processing. */
	private final Thread thread = new Thread(worker, getClass().getSimpleName());


	/**
	 * Create a new AsyncCommandController with the specified ApplicationContext.
	 *
	 * @param context The ApplicationContext.
	 */
	protected AsyncCommandController(ApplicationContext context) {
		super(context);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		super.startInternal();

		thread.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		super.stopInternal();

		// Use poison pill to stop the event-queue thread.
		Callable<Boolean> task = () -> {
			return false;
		};

		addTask(task);
	}

	@Override
	protected void processEvent(T event) {
		if (event.isSynchronous()) {
			try {
				super.processEvent(event);
			}
			catch (Exception e) {
				LOG.error("Could not process controller event.", e);
			}
			return;
		}

		addEvent(event);
	}

	/**
	 * Adds an event to the end of the event queue for later processing.
	 *
	 * @param event The controller event.
	 */
	protected void addEvent(final T event) {
		Callable<Boolean> task = getDefaultTask(event);
		addTask(task);
	}

	/**
	 * Adds an event to the beginning of the event queue for priority
	 * processing.
	 *
	 * @param event The controller event.
	 */
	protected void addUrgentEvent(final T event) {
		Callable<Boolean> task = getDefaultTask(event);
		addUrgentTask(task);
	}

	/**
	 * Adds a {@code Runnable} to the end of the event queue for later
	 * processing.
	 *
	 * @param task The callable task.
	 */
	protected void addTask(Callable<Boolean> task) {
		eventQueue.addLast(task);
	}

	/**
	 * Adds a {@code Runnable} to the beginning of the event queue for priority
	 * processing.
	 *
	 * @param task The callable task.
	 */
	protected void addUrgentTask(Callable<Boolean> task) {
		eventQueue.addFirst(task);
	}

	/**
	 * Creates a default controller task as {@code Runnable}.
	 *
	 * @param event the {@code ControllerEvent}.
	 *
	 * @return a new callable task.
	 */
	private Callable<Boolean> getDefaultTask(final T event) {
		return () -> {
			try {
				super.processEvent(event);
			}
			catch (Exception e) {
				LOG.error("Could not process controller event", e);
			}
			return true;
		};
	}

	/**
	 * Not to be called directly. This method must be called in a separate
	 * thread.
	 * <p>
	 * This method takes tasks represented by {@code Runnable} from the queue
	 * and invokes them. If the task is based on a {@code ControllerEvent}
	 * {@link #processEvent} is called for further processing.
	 *
	 * @throws Exception If an event can't be executed.
	 *
	 * @see Thread#run()
	 * @see Thread#start()
	 */
	private void processEvents() throws Exception {
		boolean result;

		while (true) {
			result = eventQueue.takeFirst().call();

			if (!result) {
				break;
			}
		}
	}

}
