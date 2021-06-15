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

package org.lecturestudio.web.api.janus;

import org.lecturestudio.web.api.janus.message.JanusMessage;

/**
 * There are different ways to interact with a Janus WebRTC server. Supported
 * transport protocols are:
 * <ul>
 *     <li>Plain HTTP REST Interface</li>
 *     <li>WebSockets</li>
 *     <li>RabbitMQ</li>
 *     <li>MQTT</li>
 *     <li>Nanomsg</li>
 *     <li>UnixSockets</li>
 * </ul>
 * <p>
 * This interface defines an abstraction layer for the various transport
 * protocols. It's up to the specific implementation how the messages are
 * received and transmitted.
 *
 * @author Alex Andres
 */
public interface JanusMessageTransmitter {

	/**
	 * Send the provided message to the Janus WebRTC server.
	 *
	 * @param message The message to send.
	 */
	void sendMessage(JanusMessage message);

}
