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

package org.lecturestudio.core.net.protocol.srp;

import java.util.Stack;

import org.lecturestudio.core.net.protocol.srp.message.SrpMessage;

/**
 * The abstract class {@link SrpNetworkTool} provides basic functionalities for {@link SrpMessage} handling.
 * This class enables the storage and retrieval of {@link SrpMessage}s for further processing.
 * 
 * @author Alex Andres
 */
public abstract class SrpNetworkTool implements SrpMessageHandler {

	/**
	 * Checks if the incoming message suits to the current state.
	 * 
	 * @param message The incoming message.
	 * 
	 * @return {@code true} if the message is received in the right state, otherwise {@code false}.
	 */
	abstract protected boolean isValidState(SrpMessage message);

	
	/** The messages to be sent. */
	private final Stack<SrpMessage> outgoingMessages;


	/**
	 * Creates an instance of {@link SrpNetworkTool}.
	 */
	public SrpNetworkTool() {
		this.outgoingMessages = new Stack<>();
	}

	@Override
	public SrpMessage getNextOutgoingMessage() {
		if (outgoingMessages.isEmpty()) {
			return null;
		}

		return outgoingMessages.pop();
	}

	/**
	 * Puts a {@link SrpMessage} onto the message stack.
	 * The messages are retrieved in the order in which they were added.
	 *
	 * @param message The new SrpMessage.
	 */
	protected void newOutgoingMessage(SrpMessage message) {
		outgoingMessages.push(message);
	}

}
