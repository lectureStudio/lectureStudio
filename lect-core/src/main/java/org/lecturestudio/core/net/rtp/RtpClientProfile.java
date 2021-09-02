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

public final class RtpClientProfile {

	/**
	 * The default maximum size of the receive buffer.
	 */
	public final static int MAX_RECEIVE_BUFFER_SIZE = 1000;

	/**
	 * The default timeout in milliseconds after which the receive buffer gets flushed.
	 */
	public final static int RECEIVE_BUFFER_TIMEOUT = 20;

	/**
	 * Number of decoded RTP packets in the receive queue.
	 */
	private final int maxReceiveBufferSize;

	/**
	 * Timeout in milliseconds after which the receive buffer gets flushed.
	 */
	private final int receiveBufferTimeout;


	/**
	 * Creates a profile with default parameter values.
	 */
	public RtpClientProfile() {
		this(MAX_RECEIVE_BUFFER_SIZE, RECEIVE_BUFFER_TIMEOUT);
	}

	/**
	 * Creates a profile with the specified timeout in milliseconds after which the receive buffer gets flushed and
	 * the default maximum size of the receive buffer.
	 *
	 * @param receiveBufferTimeout The timeout in milliseconds after which the receive buffer gets flushed.
	 */
	public RtpClientProfile(int receiveBufferTimeout) {
		this(MAX_RECEIVE_BUFFER_SIZE, receiveBufferTimeout);
	}

	/**
	 * Creates a profile with the specified parameter values.
	 * 
	 * @param maxReceiveBufferSize The maximum size of the receive buffer.
	 * @param receiveBufferTimeout The timeout in milliseconds after which the receive buffer gets flushed.
	 */
	public RtpClientProfile(int maxReceiveBufferSize, int receiveBufferTimeout) {
		this.maxReceiveBufferSize = maxReceiveBufferSize;
		this.receiveBufferTimeout = receiveBufferTimeout;
	}

	/**
	 * Get the {@link #maxReceiveBufferSize}.
	 *
	 * @return The {@link #maxReceiveBufferSize}.
	 */
	public int getMaxReceiveBufferSize() {
		return maxReceiveBufferSize;
	}

	/**
	 * Get the {@link #receiveBufferTimeout}.
	 *
	 * @return The {@link #receiveBufferTimeout}.
	 */
	public int getReceiveBufferTimeout() {
		return receiveBufferTimeout;
	}

}
