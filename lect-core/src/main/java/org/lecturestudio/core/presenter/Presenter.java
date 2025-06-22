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

package org.lecturestudio.core.presenter;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewLayer;

/**
 * An abstract base class for presenters in the Model-View-Presenter pattern.
 * This class handles common presenter operations such as view management,
 * lifecycle methods, and exception handling.
 * <p>
 * Presenters connect the model layer with views and contain the presentation
 * logic for the application. They control the view's behavior and handle user
 * interaction events forwarded by the view.
 *
 * @param <T> The type of the view.
 *
 * @author Alex Andres
 */
public abstract class Presenter<T extends View> {

	private static final Logger LOG = LogManager.getLogger(Presenter.class);

	/** The application context providing access to application-wide services and configurations. */
	protected final ApplicationContext context;

	/** The view component that this presenter controls and updates. */
	protected final T view;

	/** The action that is executed when the user clicks the 'close' button. */
	protected Action closeAction;

	/** Determines whether this presenter can be closed. Default value is true. */
	private boolean closeable;


	/**
	 * Creates a new presenter with the given application context and view.
	 *
	 * @param context The application context providing access to services and configurations.
	 * @param view    The view component that this presenter will control and update.
	 *
	 * @throws NullPointerException If either context or view is null.
	 */
	protected Presenter(ApplicationContext context, T view) {
		requireNonNull(context);
		requireNonNull(view);

		this.context = context;
		this.view = view;
		this.closeable = true;
	}

	/**
	 * Returns the application context associated with this presenter.
	 *
	 * @return The application context providing access to application-wide services and configurations.
	 */
	public ApplicationContext getContext() {
		return context;
	}

	/**
	 * Returns the view component controlled by this presenter.
	 *
	 * @return The view instance.
	 */
	public T getView() {
		return view;
	}

	/**
	 * Returns the layer in which the view should be displayed.
	 *
	 * @return The view layer, by default ViewLayer.Content.
	 */
	public ViewLayer getViewLayer() {
		return ViewLayer.Content;
	}

	/**
	 * Indicates whether this presenter should be cached.
	 *
	 * @return false by default, indicating no caching.
	 */
	public boolean cache() {
		return false;
	}

	/**
	 * Closes the presenter by executing the registered close action
	 * if it exists and if the presenter is closeable.
	 */
	public void close() {
		if (nonNull(closeAction) && isCloseable()) {
			closeAction.execute();
		}
	}

	/**
	 * Initializes the presenter. This method is called after the presenter
	 * is created and before it is used.
	 *
	 * @throws Exception If initialization fails.
	 */
	public void initialize() throws Exception {

	}

	/**
	 * Cleans up resources used by this presenter.
	 * This method is called when the presenter is no longer needed.
	 */
	public void destroy() {

	}

	/**
	 * Sets or adds an action to execute when the presenter is closed.
	 * The new action is concatenated with any existing close action.
	 *
	 * @param action The action to execute on close.
	 */
	public void setOnClose(Action action) {
		closeAction = Action.concatenate(closeAction, action);
	}

	/**
	 * Determines whether this presenter can be closed.
	 *
	 * @return true if the presenter is closeable, false otherwise.
	 */
	protected boolean isCloseable() {
		return closeable;
	}

	/**
	 * Sets whether this presenter can be closed.
	 *
	 * @param closeable true to allow closing, false to prevent closing.
	 */
	protected void setCloseable(boolean closeable) {
		this.closeable = closeable;
	}

	/**
	 * Handles an exception by logging it and showing an error dialog.
	 *
	 * @param throwable    The exception that occurred.
	 * @param throwMessage The message to log with the exception.
	 * @param title        The title for the error dialog.
	 */
	protected void handleException(Throwable throwable, String throwMessage, String title) {
		handleException(throwable, throwMessage, title, null);
	}

	/**
	 * Handles an exception by logging it and showing an error dialog.
	 *
	 * @param throwable    The exception that occurred.
	 * @param throwMessage The message to log with the exception.
	 * @param title        The title for the error dialog.
	 * @param message      The message to show in the error dialog.
	 */
	protected final void handleException(Throwable throwable, String throwMessage, String title, String message) {
		logException(throwable, throwMessage);

		context.showError(title, message);
	}

	/**
	 * Logs a debug message with optional parameters.
	 *
	 * @param message       The message to log.
	 * @param messageParams Optional parameters to format the message.
	 */
	protected final void logMessage(String message, Object... messageParams) {
		LOG.debug(message, messageParams);
	}

	/**
	 * Logs an error message along with the associated exception.
	 *
	 * @param throwable    The exception to log.
	 * @param throwMessage The message to log with the exception.
	 *
	 * @throws NullPointerException If throwable or throwMessage is null.
	 */
	protected final void logException(Throwable throwable, String throwMessage) {
		requireNonNull(throwable);
		requireNonNull(throwMessage);

		LOG.error(throwMessage, throwable);
	}
}
