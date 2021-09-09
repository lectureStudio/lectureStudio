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

import org.lecturestudio.web.api.janus.JanusStateHandler;
import org.lecturestudio.web.api.janus.message.JanusMessage;

/**
 * A Janus state describes a single isolated step during the session
 * establishment and message signaling process with the Janus WebRTC server. A
 * concrete state handles state specific messages asynchronously. Once this
 * state has finished its signaling and message handling, it can transition to
 * another state by invoking {@link JanusStateHandler#setState(JanusState)}.
 * Transitioning to the next state will make this state inactive and it will no
 * longer receive messages.
 *
 * @author Alex Andres
 */
public interface JanusState {

	/**
	 * Shared state logger.
	 */
	Logger LOG = LogManager.getLogger(JanusState.class);


	/**
	 * Initialize this state with the provided {@code JanusHandler}. Usually
	 * this is the point where the state sends specific messages to the Janus
	 * WebRTC server. The response messages from the Janus server will be
	 * provided to the {@link #handleMessage(JanusStateHandler, JanusMessage)}
	 * method.
	 *
	 * @param handler The {@code JanusStateHandler}.
	 */
	void initialize(JanusStateHandler handler);

	/**
	 * Process state specific messages received from the Janus WebRTC server.
	 * Once this state has finished its message handling, it can transition to
	 * another state by invoking {@link JanusStateHandler#setState(JanusState)}.
	 *
	 * @param handler The {@code JanusStateHandler}.
	 * @param message The message to process.
	 */
	void handleMessage(JanusStateHandler handler, JanusMessage message)
			throws Exception;

	/**
	 * Check whether the transaction IDs of the messages match. Any response
	 * message MUST have the same transaction ID as the request message sent to
	 * the server.
	 *
	 * @param sent     The previously sent message.
	 * @param received The currently received message.
	 *
	 * @throws IllegalStateException If the transaction IDs do not match.
	 */
	default void checkTransaction(JanusMessage sent, JanusMessage received) {
		if (!sent.getTransaction().equals(received.getTransaction())) {
			throw new IllegalStateException("Transactions do not match "
				+ sent.getTransaction() + " != " + received.getTransaction());
		}
	}

	/**
	 * Log an error occurred within this state.
	 *
	 * @param throwable The cause of the error.
	 * @param message   A concise message describing the error.
	 */
	default void logError(Throwable throwable, String message) {
		LOG.error(message, throwable);
	}

	/**
	 * Log a parameterized debugging message.
	 *
	 * @param message A concise message describing the debugging step.
	 * @param args    Arguments specified in the formatted message.
	 */
	default void logDebug(String message, Object... args) {
		LOG.debug(String.format(message, args));
	}
}
