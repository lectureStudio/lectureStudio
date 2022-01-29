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

package org.lecturestudio.web.api.event;

import java.math.BigInteger;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.event.ExecutableEvent;

public class PeerStateEvent extends ExecutableEvent {

	private final BigInteger peerId;

	private final String peerName;

	private final Long requestId;


	/**
	 * Create the {@link PeerStateEvent} with the specified state.
	 *
	 * @param requestId   The unique request ID of the peer.
	 * @param peerName The display-name of the peer.
	 * @param state    The state.
	 */
	public PeerStateEvent(Long requestId, String peerName, ExecutableState state) {
		this(null, requestId, peerName, state);
	}

	/**
	 * Create the {@link PeerStateEvent} with the specified state.
	 *
	 * @param peerId   The unique ID of the peer.
	 * @param peerName The display-name of the peer.
	 * @param state    The state.
	 */
	public PeerStateEvent(BigInteger peerId, String peerName, ExecutableState state) {
		this(peerId, null, peerName, state);
	}

	/**
	 * Create the {@link PeerStateEvent} with the specified state.
	 *
	 * @param peerId   The unique ID of the peer.
	 * @param peerName The display-name of the peer.
	 * @param state    The state.
	 */
	public PeerStateEvent(BigInteger peerId, Long requestId, String peerName, ExecutableState state) {
		super(state);

		this.peerId = peerId;
		this.requestId = requestId;
		this.peerName = peerName;
	}

	/**
	 * @return The unique ID of the peer.
	 */
	public BigInteger getPeerId() {
		return peerId;
	}

	/**
	 * @return The display-name of the peer.
	 */
	public String getPeerName() {
		return peerName;
	}

	/**
	 * @return The unique request ID of the peer.
	 */
	public Long getRequestId() {
		return requestId;
	}
}
