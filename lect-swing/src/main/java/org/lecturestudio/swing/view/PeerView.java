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

package org.lecturestudio.swing.view;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.web.api.janus.JanusParticipantContext;

/**
 * The {@code PeerView} interface defines methods for updating the visual representation
 * of a peer during a WebRTC session. It allows setting the peer's name, image, and
 * actions to be performed when the peer's audio or video is muted, or when the peer is
 * kicked.
 *
 * @author Alex Andres
 */
public interface PeerView {

	/**
	 * Sets the executable state for the peer.
	 *
	 * @param state The new state to set for the executable.
	 */
	void setState(ExecutableState state);

	/**
	 * Sets the participant context for the peer.
	 *
	 * @param context The participant context containing streaming session-related information.
	 */
	void setParticipantContext(JanusParticipantContext context);

	/**
	 * Sets the action to be performed when the peer is kicked.
	 *
	 * @param action The action to be performed on kick.
	 */
	void setOnKick(Action action);

}
