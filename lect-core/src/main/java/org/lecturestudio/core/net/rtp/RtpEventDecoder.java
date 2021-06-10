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

import java.nio.ByteBuffer;

import org.lecturestudio.core.recording.action.ActionFactory;
import org.lecturestudio.core.recording.action.PlaybackAction;

/**
 * The {@link RtpEventDecoder} decodes {@link RtpPacket}s that contain event data.
 * 
 * @author Alex Andres
 */
public class RtpEventDecoder implements RtpPacketDecoder {

	@Override
	public PlaybackAction decodeRtpPacket(RtpPacket packet) throws RtpDecodeException {
		ByteBuffer buffer = ByteBuffer.wrap(packet.getPayload());
		// extract header
		int length = buffer.getInt();
		int type = buffer.get();
		int timestamp = buffer.getInt();

		byte[] actionData = null;
		int dataLength = length - 5;
		if (dataLength > 0) {
			actionData = new byte[dataLength];
			buffer.get(actionData);
		}
		
		PlaybackAction action;
		try {
			action = ActionFactory.createAction(type, timestamp, actionData);
		}
		catch (Exception e) {
			throw new RtpDecodeException("Decode event failed.", e);
		}

		return action;
	}

}
