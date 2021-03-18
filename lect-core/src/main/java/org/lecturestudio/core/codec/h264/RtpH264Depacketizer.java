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

import org.lecturestudio.core.net.rtp.RtpPacket;

/**
 * RTP Depacketizer implementation for H.264 Video based on RFC 6184. The
 * specification defines three packetization modes: Single NAL unit mode,
 * Non-interleaved mode and Interleaved mode. The current implementation
 * supports the first two modes. If a incoming packet is a single NAL unit
 * packet, the payload contained in the packet is passed directly to the
 * decoder. STAP-A NAL units of incoming packets are passed to the decoder in
 * the order in which they are encapsulated in the packet. Fragmentation FU-A
 * packets are buffered in order to collect all NAL fragments and then pass the
 * concatenated NAL unit to the decoder. If a fragmentation unit is lost, the
 * following fragmentation units corresponding to the same fragmented NAL unit
 * are dropped.
 *
 * @author Alex Andres
 */
public class RtpH264Depacketizer {

	public byte[] processPacket(RtpPacket packet) {
		byte[] payload = packet.getPayload();

		// extract the NAL unit header
		NalUnitHeader nalUnitHeader = NalUnitHeader.parse(payload);

		if (nalUnitHeader.isSingleNalUnit()) {
			return payload;
		}
		if (nalUnitHeader.isFragmentationUnit()) {
			handleFragmentation();
			// TODO
		}
		if (nalUnitHeader.isAggregationUnit()) {
			throw new IllegalStateException("Aggregation packetization is not implemented.");
		}

		return null;
	}

	private void handleFragmentation() {
		// TODO
	}

}
