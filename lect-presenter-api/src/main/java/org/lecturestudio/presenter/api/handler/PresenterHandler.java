/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.handler;

import static java.util.Objects.requireNonNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.presenter.api.context.PresenterContext;

/**
 * Abstract base class for handlers that manage specific tasks within the presenter application.
 *
 * <p>Handlers are responsible for encapsulating and managing specific functionality
 * to subdivide many and/or complex control flows in the presenter application.
 * Subclasses must implement the {@link #initialize()} method to set up their
 * specific control flows and event listeners.</p>
 *
 * <p>Each handler has access to the {@link PresenterContext} which provides
 * application-wide services and state.</p>
 *
 * @author Alex Andres
 */
public abstract class PresenterHandler {

	private static final Logger LOG = LogManager.getLogger(PresenterHandler.class);

	/**
	 * The presenter application context that provides access to application-wide
	 * services and state. This context is passed during handler initialization
	 * and remains constant throughout the handler's lifecycle.
	 */
	protected final PresenterContext context;


	/**
	 * Create a new {@code PresenterHandler} with the given context.
	 *
	 * @param context The presenter application context.
	 */
	public PresenterHandler(PresenterContext context) {
		this.context = context;
	}

	/**
	 * Initializes the handler by setting up specific control flows and event listeners.
	 * <p>
	 * This method must be implemented by all subclasses to establish their
	 * unique functionality within the presenter application. Implementations
	 * should configure any necessary event handlers, register listeners,
	 * and establish the control flows specific to their handling domain.
	 * </p>
	 */
	public abstract void initialize();

	/**
	 * Handles an exception by logging it and showing an error dialog to the user.
	 *
	 * @param throwable    The exception to handle.
	 * @param throwMessage The message to log with the exception.
	 * @param title        The title for the error dialog.
	 */
	protected void handleException(Throwable throwable, String throwMessage, String title) {
		handleException(throwable, throwMessage, title, null);
	}

	/**
	 * Handles an exception by logging it and showing an error dialog to the user.
	 * This method logs the exception with the provided throwMessage and displays
	 * an error dialog with the given title and message.
	 *
	 * @param throwable    The exception to handle.
	 * @param throwMessage The message to log with the exception.
	 * @param title        The title for the error dialog.
	 * @param message      The detailed message for the error dialog, can be null.
	 */
	protected final void handleException(Throwable throwable, String throwMessage, String title, String message) {
		logException(throwable, throwMessage);

		context.showError(title, message);
	}

	/**
	 * Logs an exception with the specified message using the class logger.
	 * This method validates that neither parameter is null before logging.
	 *
	 * @param throwable    The exception to log, must not be null.
	 * @param throwMessage The message to log with the exception, must not be null.
	 * @throws NullPointerException if either parameter is null.
	 */
	protected final void logException(Throwable throwable, String throwMessage) {
		requireNonNull(throwable);
		requireNonNull(throwMessage);

		LOG.error(throwMessage, throwable);
	}
}
