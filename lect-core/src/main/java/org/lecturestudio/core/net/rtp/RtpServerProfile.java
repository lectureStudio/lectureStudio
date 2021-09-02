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

package org.lecturestudio.core.net.rtp;

public class RtpServerProfile {

	/**
	 * The default value for {@link #maxSendQueueSize}.
	 */
	public final static int MAX_SEND_QUEUE_SIZE = 1000;

	/**
	 * The default value for {@link #maxPayloadSize}.
	 */
	public final static int MAX_PAYLOAD_SIZE = 1200;

	/**
	 * The default value for {@link #retransmissionTimeout}.
	 */
	public final static int RETRANSMISSION_TIMEOUT = 10;

	/**
	 * The number of RTP packets in the queue which are retransmitted after the timeout.
	 */
	private final int maxSendQueueSize;

	/**
	 * The maximum payload size in bytes of a RTP packet.
	 */
	private final int maxPayloadSize;

	/**
	 * The RTP packet retransmission timeout in milliseconds.
	 */
	private final int retransmissionTimeout;


	/**
	 * Creates a profile with default parameter values.
	 */
	public RtpServerProfile() {
		this(MAX_SEND_QUEUE_SIZE, MAX_PAYLOAD_SIZE, RETRANSMISSION_TIMEOUT);
	}

	/**
	 * Creates a profile with the specified parameter values.
	 * 
	 * @param maxSendQueueSize The maximum number of packets in the send buffer.
	 * @param maxPayloadSize The maximum payload size in bytes of a packet.
	 * @param retransmissionTimeout The packet retransmission timeout.
	 */
	public RtpServerProfile(int maxSendQueueSize, int maxPayloadSize, int retransmissionTimeout) {
		this.maxSendQueueSize = maxSendQueueSize;
		this.maxPayloadSize = maxPayloadSize;
		this.retransmissionTimeout = retransmissionTimeout;
	}

	/**
	 * Get the maximum number of packets in the send buffer.
	 *
	 * @return The maximum number of packets in the send buffer.
	 */
	public int getMaxSendQueueSize() {
		return maxSendQueueSize;
	}

	/**
	 * Get the maximum payload size in bytes of a packet.
	 *
	 * @return The maximum payload size in bytes of a packet.
	 */
	public int getMaxPayloadSize() {
		return maxPayloadSize;
	}

	/**
	 * Get the packet retransmission timeout.
	 *
	 * @return The packet retransmission timeout.
	 */
	public int getRetransmissionTimeout() {
		return retransmissionTimeout;
	}

}
