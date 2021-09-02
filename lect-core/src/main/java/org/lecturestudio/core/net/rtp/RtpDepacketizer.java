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
 * Common interface to provide a consistent mechanism to extract media data from
 * RTP packets according to the RTP protocol.
 *
 * @author Alex Andres
 *
 * @see RtpPacket
 */
public interface RtpDepacketizer {

	/**
	 * Extract the payload of the provided {@link RtpPacket}.
	 *
	 * @param packet The incoming RTP packet to process.
	 *
	 * @return The payload data of the packet.
	 *
	 * @throws Exception If the packet could not be read because it was malformed.
	 */
	byte[] processPacket(RtpPacket packet) throws Exception;

}
