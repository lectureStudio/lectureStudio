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

package org.lecturestudio.core.controller;

import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.ControllerEvent;
import org.lecturestudio.core.bus.event.ControllerEventException;
import org.lecturestudio.core.bus.event.EventErrorHandler;
import org.lecturestudio.core.bus.event.EventProcessedHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A CommandController processes controller specific events in receiving order.
 *
 * @param <T> The type of the controller event.
 *
 * @author Alex Andres
 */
public abstract class CommandController<T extends ControllerEvent> extends Controller {

	private static final Logger LOG = LogManager.getLogger(CommandController.class);



	/**
	 * A command to be registered in the CommandController that will be executed
	 * on the receipt of the corresponding controller event.
	 */
	@FunctionalInterface
	protected interface Command {

		/**
		 * Performs an operation.
		 */
		void execute() throws Exception;

	}



	/**
	 * A typed command to be registered in the CommandController that will be
	 * executed with an input argument on the receipt of the corresponding
	 * controller event.
	 *
	 * @param <T> The type of the value to accept.
	 */
	@FunctionalInterface
	protected interface TypedCommand<T> {

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param t The input argument.
		 */
		void execute(T t) throws Exception;

	}


	/** Mapping of event classes to commands. */
	private Map<Class<? extends T>, Command> commands = new HashMap<>();

	/** Mapping of event classes to typed commands. */
	private Map<Class<? extends T>, TypedCommand<?>> commandsTyped = new HashMap<>();


	/**
	 * Create a CommandController with the specified application context.
	 *
	 * @param context The application context.
	 */
	protected CommandController(ApplicationContext context) {
		super(context);
	}

	/**
	 * Register a typed command for execution when an event with the specified
	 * event class is received.
	 *
	 * @param eventClass The class of an event.
	 * @param cmd        The bound command for execution.
	 * @param dataClass  The class of the input argument the command will
	 *                   receive.
	 * @param <S>        The type of the input argument.
	 */
	protected final <S> void registerCommand(Class<? extends T> eventClass, TypedCommand<S> cmd, Class<S> dataClass) {
		commandsTyped.put(eventClass, cmd);
	}

	/**
	 * Register a command for execution when an event with the specified event
	 * class is received.
	 *
	 * @param eventClass The class of an event.
	 * @param cmd        The bound command for execution.
	 */
	protected final void registerCommand(Class<? extends T> eventClass, Command cmd) {
		commands.put(eventClass, cmd);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		super.initInternal();

		ApplicationBus.register(this);
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		super.destroyInternal();

		ApplicationBus.unregister(this);
	}

	@SuppressWarnings("unchecked")
	protected void processEvent(T event) {
		try {
			Command command = commands.get(event.getClass());

			if (nonNull(command)) {
				command.execute();

				handleSuccess(event);
			}
			else {
				TypedCommand<Object> typedCommand = (TypedCommand<Object>) commandsTyped.get(event.getClass());

				if (nonNull(typedCommand)) {
					typedCommand.execute(event.getData());

					handleSuccess(event);
				}
			}
		}
		catch (ControllerEventException e) {
			handleError(event, e.getMessage());
		}
		catch (Exception e) {
			handleError(event, getDictionary().get("error.contact.administrator"));

			LOG.error("Execute controller command failed.", e);
		}
	}

	private void handleSuccess(T event) {
		EventProcessedHandler handler = event.getEventProcessedHandler();

		if (nonNull(handler)) {
			handler.onEventProcessed();
		}
	}

	private void handleError(T event, String message) {
		EventErrorHandler handler = event.getEventErrorHandler();

		if (nonNull(handler)) {
			handler.onEventError(message);
		}
	}

}
