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

import java.util.List;

/**
 * Common interface to provide a consistent mechanism to form media data into
 * RTP packets according to the RTP protocol.
 *
 * @author Alex Andres
 *
 * @see RtpPacket
 */
public interface RtpPacketizer {

	/**
	 * Form the payload data into one or more {@code RtpPacket}'s.
	 *
	 * @param payload       The payload data to pack.
	 * @param payloadLength The length of the payload data.
	 * @param timestamp     The RTP timestamp.
	 *
	 * @return a list of RTP packets that contain the packed payload data.
	 */
	List<RtpPacket> processPacket(byte[] payload, int payloadLength, long timestamp);

}
