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

package org.lecturestudio.core.audio.codec.ffmpeg;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lecturestudio.core.net.rtp.RtpPacket;
import org.lecturestudio.core.net.rtp.RtpPacketizer;

/**
 * FFmpeg RTP packetizer implementation.
 *
 * @author Alex Andres
 */
public class FFmpegRtpPacketizer implements RtpPacketizer {

	/** The RTP packet which should be sent. */
	private RtpPacket rtpPacket;


	/**
	 * Create a new FFmpegRtpPacketizer instance and set the default RTP packet
	 * header values.
	 */
	public FFmpegRtpPacketizer() {
		Random rand = new Random();

		rtpPacket = new RtpPacket();
		rtpPacket.setVersion(2);
		rtpPacket.setPadding(0);
		rtpPacket.setExtension(0);

		rtpPacket.setMarker(0);
		rtpPacket.setPayloadType(97); // dynamic

		rtpPacket.setSeqNumber(rand.nextInt());
		rtpPacket.setTimestamp(rand.nextInt());
		rtpPacket.setSsrc(rand.nextInt());
	}

	@Override
	public List<RtpPacket> processPacket(byte[] payload, int payloadLength, long timestamp) {
		List<RtpPacket> packets = new ArrayList<>();

		updateRtpPacket(timestamp);
		packPacket(payload, payloadLength);

		packets.add(rtpPacket.clone());

		return packets;
	}

	private void packPacket(byte[] packetBytes, int payloadSize) {
		byte[] payload = new byte[payloadSize];

		System.arraycopy(packetBytes, 0, payload, 0, payloadSize);

		rtpPacket.setPayload(payload);
	}

	private void updateRtpPacket(long timestamp) {
		/* Increment RTP header flags */
		rtpPacket.setSeqNumber(rtpPacket.getSeqNumber() + 1);
		rtpPacket.setTimestamp(timestamp);
	}

}
