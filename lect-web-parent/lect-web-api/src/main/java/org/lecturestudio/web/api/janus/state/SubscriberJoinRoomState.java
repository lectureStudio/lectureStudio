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

import static java.util.Objects.requireNonNull;

import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;

import java.math.BigInteger;
import java.util.UUID;

import org.lecturestudio.web.api.janus.JanusParticipantType;
import org.lecturestudio.web.api.janus.JanusStateHandler;
import org.lecturestudio.web.api.janus.message.JanusJsepMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusPluginDataMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomJoinRequest;

/**
 * This state joins a previously created feed in a video-room published on the
 * Janus WebRTC server. This state joins the room as a subscriber which makes
 * you an receive-only participant.
 *
 * @author Alex Andres
 */
public class SubscriberJoinRoomState implements JanusState {

	private final BigInteger publisherId;

	private JanusPluginDataMessage joinRequest;


	public SubscriberJoinRoomState(BigInteger publisherId) {
		requireNonNull(publisherId);

		this.publisherId = publisherId;
	}

	@Override
	public void initialize(JanusStateHandler handler) {
		JanusRoomJoinRequest request = new JanusRoomJoinRequest();
		request.setParticipantType(JanusParticipantType.SUBSCRIBER);
		request.setRoomId(handler.getRoomId());
		request.setPublisherId(publisherId);

		joinRequest = new JanusPluginDataMessage(handler.getSessionId(),
				handler.getPluginId());
		joinRequest.setTransaction(UUID.randomUUID().toString());
		joinRequest.setBody(request);

		handler.sendMessage(joinRequest);
	}

	@Override
	public void handleMessage(JanusStateHandler handler, JanusMessage message) {
		checkTransaction(joinRequest, message);

		if (message instanceof JanusJsepMessage) {
			JanusJsepMessage jsepMessage = (JanusJsepMessage) message;
			String sdp = jsepMessage.getSdp();
			RTCSessionDescription offer = new RTCSessionDescription(RTCSdpType.OFFER, sdp);

			handler.setState(new SubscriberJoinedRoomState(offer));
		}
	}
}
