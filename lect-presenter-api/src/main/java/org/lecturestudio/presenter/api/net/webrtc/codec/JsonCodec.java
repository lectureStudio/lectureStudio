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

package org.lecturestudio.presenter.api.net.webrtc.codec;

import static java.util.Objects.isNull;

import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

public class JsonCodec {

	public String encode(Object obj) {
		JsonObjectBuilder builder = null;

		if (obj instanceof RTCSessionDescription) {
			RTCSessionDescription desc = (RTCSessionDescription) obj;

			builder = Json.createObjectBuilder();
			builder.add("sdp", desc.sdp);
			builder.add("type", desc.sdpType.toString().toLowerCase());
		}
		else if (obj instanceof RTCIceCandidate) {
			RTCIceCandidate candidate = (RTCIceCandidate) obj;

			builder = Json.createObjectBuilder();
			builder.add("candidate", candidate.sdp);
			builder.add("sdpMid", candidate.sdpMid);
			builder.add("sdpMLineIndex", candidate.sdpMLineIndex);
		}

		if (isNull(builder)) {
			return null;
		}

		StringWriter stringWriter = new StringWriter();

		JsonWriter writer = Json.createWriter(stringWriter);
		writer.writeObject(builder.build());
		writer.close();

		return stringWriter.toString();
	}

	public Object decode(String json) throws IOException {
		JsonReader reader = Json.createReader(new StringReader(json));
		JsonObject jsonObject = reader.readObject();

		Object result = null;

		if (jsonObject.containsKey("sdp") && jsonObject.containsKey("type")) {
			String sdp = jsonObject.getString("sdp");
			String type = jsonObject.getString("type");

			RTCSdpType rtcType;

			if (type.equals("offer")) {
				rtcType = RTCSdpType.OFFER;
			}
			else if (type.equals("answer")) {
				rtcType = RTCSdpType.ANSWER;
			}
			else {
				throw new IOException("Invalid RTCSdpType");
			}

			result = new RTCSessionDescription(rtcType, sdp);
		}
		else if (jsonObject.containsKey("candidate")) {
			String candidate = jsonObject.getString("candidate");
			String sdpMid = jsonObject.getString("sdpMid");
			int sdpMLineIndex = jsonObject.getInt("sdpMLineIndex");

			result = new RTCIceCandidate(sdpMid, sdpMLineIndex, candidate);
		}

		reader.close();

		return result;
	}
}
