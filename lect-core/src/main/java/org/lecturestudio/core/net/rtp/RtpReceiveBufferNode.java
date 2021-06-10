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

/**
 * A {@link RtpReceiveBufferNode} holds a {@link RtpPacket} and is placed into the {@link RtpReceiveBuffer}.
 * This class implements methods to compare this node to another nodes to determine the order.
 * 
 * @author Alex Andres
 * 
 */
public class RtpReceiveBufferNode implements Comparable<RtpReceiveBufferNode> {

	/**
	 * The RTP packet.
	 */
	private final RtpPacket packet;


	/**
	 * Creates a new {@link RtpReceiveBufferNode} with provided {@link RtpPacket}.
	 * 
	 * @param packet The RTP packet.
	 */
	public RtpReceiveBufferNode(RtpPacket packet) {
		this.packet = packet;
	}

	/**
	 * Returns the RTP packet.
	 * 
	 * @return The RTP packet.
	 */
	public RtpPacket getPacket() {
		return packet;
	}

	@Override
	public int compareTo(RtpReceiveBufferNode o) {
		int a = packet.getSeqNumber();
		int b = o.getPacket().getSeqNumber();
		int dist = Math.abs(a - b);

		if (dist > 32767) {
			if (a > 32768) {
				a -= 65536;
			}
			if (b > 32768) {
				b -= 65536;
			}
		}

		return Integer.compare(a, b);
	}

	@Override
	public boolean equals(Object o) {
		try {
			RtpReceiveBufferNode node = (RtpReceiveBufferNode) o;
			return node.getPacket().getSeqNumber() == packet.getSeqNumber();
		}
		catch (Exception e) {
			return false;
		}
	}

}
