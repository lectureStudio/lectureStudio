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

import java.text.MessageFormat;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.presenter.command.NotificationCommand;
import org.lecturestudio.core.presenter.command.NotificationPopupCommand;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewLayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 *
 * @author Alex Andres
 *
 * @param <T> The type of the view.
 */
public abstract class Presenter<T extends View> {

	private final static Logger LOG = LogManager.getLogger(Presenter.class);

	protected final ApplicationContext context;

	protected final T view;

	/** The action that is executed when the user clicks the 'close' button. */
	protected Action closeAction;

	private boolean closeable;


	public Presenter(ApplicationContext context, T view) {
		requireNonNull(context);
		requireNonNull(view);

		this.context = context;
		this.view = view;
		this.closeable = true;
	}

	public T getView() {
		return view;
	}

	public ViewLayer getViewLayer() {
		return ViewLayer.Content;
	}

	public boolean cache() {
		return false;
	}

	public void close() {
		if (nonNull(closeAction) && isCloseable()) {
			closeAction.execute();
		}
	}

	public void initialize() throws Exception {

	}

	public void destroy() {

	}

	public void setOnClose(Action action) {
		closeAction = Action.concatenate(closeAction, action);
	}

	protected boolean isCloseable() {
		return closeable;
	}

	protected void setCloseable(boolean closeable) {
		this.closeable = closeable;
	}

	protected void handleException(Throwable throwable, String throwMessage, String title) {
		handleException(throwable, throwMessage, title, null);
	}

	final protected void handleException(Throwable throwable, String throwMessage, String title, String message) {
		logException(throwable, throwMessage);

		showError(title, message);
	}

	final protected void logMessage(String message, Object... messageParams) {
		LOG.debug(MessageFormat.format(message, messageParams));
	}

	final protected void logException(Throwable throwable, String throwMessage) {
		requireNonNull(throwable);
		requireNonNull(throwMessage);

		LOG.error(throwMessage, throwable);
	}

	final protected void showError(String title, String message) {
		requireNonNull(title);

		showNotification(NotificationType.ERROR, title, message);
	}

	final protected void showError(String title, String message, Object... messageParams) {
		showNotification(NotificationType.ERROR, title, message, messageParams);
	}

	final protected void showNotification(NotificationType type, String title, String message) {
		if (context.getDictionary().contains(title)) {
			title = context.getDictionary().get(title);
		}
		if (context.getDictionary().contains(message)) {
			message = context.getDictionary().get(message);
		}

		context.getEventBus().post(new NotificationCommand(type, title, message));
	}

	final protected void showNotification(NotificationType type, String title, String message, Object... messageParams) {
		if (context.getDictionary().contains(message)) {
			message = context.getDictionary().get(message);
		}

		message = MessageFormat.format(message, messageParams);

		showNotification(type, title, message);
	}

	final protected void showNotificationPopup(String title) {
		showNotificationPopup(title, null);
	}

	final protected void showNotificationPopup(String title, String message) {
		if (context.getDictionary().contains(title)) {
			title = context.getDictionary().get(title);
		}
		if (context.getDictionary().contains(message)) {
			message = context.getDictionary().get(message);
		}

		context.getEventBus().post(new NotificationPopupCommand(Position.TOP_RIGHT, title, message));
	}

}
