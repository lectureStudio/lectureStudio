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

package org.lecturestudio.presenter.api.net.webrtc;

import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCSessionDescription;

import org.lecturestudio.presenter.api.model.Contact;
import org.lecturestudio.presenter.api.model.RoomParameters;

/**
 * Callback interface for messages delivered on a signaling channel.
 *
 * @author Alex Andres
 */
public interface SignalingListener {

	void onRoomJoined(RoomParameters parameters);

	void onRoomLeft();

	void onRemoteSessionDescription(Contact contact,
			RTCSessionDescription description);

	void onRemoteIceCandidate(Contact contact, RTCIceCandidate candidate);

	void onRemoteIceCandidatesRemoved(Contact contact,
			RTCIceCandidate[] candidates);

	void onError(String message);

}
