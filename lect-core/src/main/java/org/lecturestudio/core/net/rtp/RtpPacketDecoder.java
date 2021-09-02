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
 * The interface {@link RtpPacketDecoder} is implemented by a specific class that should decode incoming data.
 * The method {@link #decodeRtpPacket(RtpPacket)} throws an {@link RtpDecodeException}
 * if the packet could not be decoded.
 * 
 * @author Alex Andres
 * 
 */
public interface RtpPacketDecoder {

	/**
	 * Decodes incoming {@link RtpPacket} to a specific object that represents the payload.
	 * 
	 * @param packet The RTP packet.
	 * 
	 * @return The specific object.
	 */
	Object decodeRtpPacket(RtpPacket packet) throws RtpDecodeException;

}
