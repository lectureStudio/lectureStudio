/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.janus.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.web.api.janus.JanusHandler;
import org.lecturestudio.web.api.janus.message.JanusMessage;

public interface JanusState {

	Logger LOG = LogManager.getLogger(JanusState.class);


	void initialize(JanusHandler handler);

	void handleMessage(JanusHandler handler, JanusMessage message);

	default void checkTransaction(JanusMessage sent, JanusMessage received) {
		if (!sent.getTransaction().equals(received.getTransaction())) {
			throw new IllegalStateException("Transactions do not match");
		}
	}

	default void logError(Throwable throwable, String message) {
		LOG.error(message, throwable);
	}

	default void logDebug(String message, Object... args) {
		LOG.debug(String.format(message, args));
	}

	default void logDebug(Throwable throwable, String message) {
		LOG.debug(message, throwable);
	}
}
