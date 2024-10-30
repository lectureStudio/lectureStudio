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
 * Handles context specific tasks to subdivide many and/or complex control
 * flows.
 *
 * @author Alex Andres
 */
public abstract class PresenterHandler {

	private static final Logger LOG = LogManager.getLogger(PresenterHandler.class);

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
	 * Initializes the handler, e.g., to set up context-specific control flows.
	 */
	abstract public void initialize();

	protected void handleException(Throwable throwable, String throwMessage,
			String title) {
		handleException(throwable, throwMessage, title, null);
	}

	protected final void handleException(Throwable throwable,
			String throwMessage, String title, String message) {
		logException(throwable, throwMessage);

		context.showError(title, message);
	}

	protected final void logException(Throwable throwable,
			String throwMessage) {
		requireNonNull(throwable);
		requireNonNull(throwMessage);

		LOG.error(throwMessage, throwable);
	}
}
