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

package org.lecturestudio.core.codec.h264;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lecturestudio.core.net.rtp.RtpPacket;

/**
 * RTP Packetizer implementation for H.264 Video based on RFC 6184. The
 * specification defines three packetization modes: Single NAL unit mode,
 * Non-interleaved mode and Interleaved mode. The current implementation
 * supports the first two modes. This packetizer is primarily intended for
 * low-delay applications.
 *
 * @author Alex Andres
 */
public class RtpH264Packetizer {

	public enum Mode {
		SINGLE_NAL,
		NON_INTERLEAVED,
		INTERLEAVED;
	}



	/** Packetization mode */
	private Mode mode;

	/** Maximum payload size */
	private int MTU = 1300;

	/** Random number generator */
	private Random rand = new Random();

	/** The RTP packet which should be sent */
	private RtpPacket rtpPacket;

	/*
	 * Creates a new RTP Packetizer implementation for H.264 Video with single NAL
	 * unit mode as default mode.
	 */
	public RtpH264Packetizer() {
		this(Mode.SINGLE_NAL);
	}

	/*
	 * Creates a new RTP Packetizer implementation for H.264 Video with specified
	 * packetization mode.
	 *
	 * @param mode one of three packetization modes.
	 */
	public RtpH264Packetizer(Mode mode) {
		if (mode == null) {
			throw new IllegalArgumentException("RTP mode is null.");
		}

		this.mode = mode;

		initRtp();
	}

	public List<RtpPacket> processPacket(ByteBuffer data, long timestamp) {
		List<RtpPacket> packets = new ArrayList<>();

		byte[] buffer = new byte[data.capacity()];
		data.get(buffer);

		/* Evaluate the payload format structure. */
		if (mode == Mode.SINGLE_NAL || MTU >= buffer.length + 1) {
			byte[] header = { 0b0110_0100 };
			byte[] payload = new byte[buffer.length + 1];
			System.arraycopy(header, 0, payload, 0, header.length);
			System.arraycopy(buffer, 0, payload, 1, buffer.length);

			packPacket(payload, payload.length);
			updatePacket(timestamp);
			packets.add(rtpPacket.clone());

			return packets;
		}
		if (mode == Mode.NON_INTERLEAVED) {
			/* Fragmentation (FU-A) packet */
			// The specification requires that the F bit is equal to 0.
			// The highest transport priority is 11.
			// FU-A is identified with the NAL unit type number 28.
			byte[] header = { 0b0111_1100, 0b0001_1100 };
			int fragmentSize = MTU - 2;
			int available = buffer.length;
			int bufferOffset = 0;
			byte[] fragment = new byte[fragmentSize];

			// set start bit
			header[1] |= 0x80;

			while (available > fragmentSize) {
				System.arraycopy(header, 0, fragment, 0, header.length);
				System.arraycopy(buffer, bufferOffset, fragment, 0,
						fragmentSize);
				packPacket(fragment, fragmentSize);
				packets.add(rtpPacket.clone());
				rtpPacket.setSeqNumber(rtpPacket.getSeqNumber() + 1);

				// remove start bit
				header[1] &= 0x7F;

				available -= fragmentSize;
				bufferOffset += fragmentSize;
			}
			// write last fragment
			// set end bit
			header[1] |= 0x40;

			fragment = new byte[available + 2];
			System.arraycopy(header, 0, fragment, 0, header.length);
			System.arraycopy(buffer, bufferOffset, fragment, 0, available);
			packPacket(fragment, fragment.length);
			packets.add(rtpPacket.clone());
			updatePacket(timestamp);

			return packets;
		}
		if (mode == Mode.INTERLEAVED) {
			// not supported
			return packets;
		}

		return packets;
	}

	private void initRtp() {
		/* Initialize RTP packet header */
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

	private void updatePacket(long timestamp) {
		/* Increment RTP header flags */
		rtpPacket.setSeqNumber(rtpPacket.getSeqNumber() + 1);
		rtpPacket.setTimestamp(timestamp);
	}

	private void packPacket(byte[] packetBytes, int payloadSize) {
		byte[] payload = new byte[payloadSize];

		System.arraycopy(packetBytes, 0, payload, 0, payloadSize);

		rtpPacket.setPayload(payload);
	}

}
