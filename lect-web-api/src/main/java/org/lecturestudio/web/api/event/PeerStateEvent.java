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

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.event.ExecutableEvent;
import org.lecturestudio.web.api.janus.JanusParticipantContext;

/**
 * An event that represents a change in the state of a peer in the WebRTC communication.
 * This event extends ExecutableEvent and carries information about a participant and
 * its execution state within the Janus WebRTC framework.
 *
 * @author Alex Andres
 */
public class PeerStateEvent extends ExecutableEvent {

	/** The context of the participant associated with this event. */
	private final JanusParticipantContext participantContext;


	/**
	 * Creates a new PeerStateEvent with the specified participant context and state.
	 *
	 * @param context The context of the participant associated with this event.
	 * @param state   The execution state of the event (e.g., STARTED, STOPPED).
	 */
	public PeerStateEvent(JanusParticipantContext context, ExecutableState state) {
		super(state);

		participantContext = context;
	}

	/**
	 * Gets the participant context associated with this event.
	 *
	 * @return The participant context containing information about the peer.
	 */
	public JanusParticipantContext getParticipantContext() {
		return participantContext;
	}
}
