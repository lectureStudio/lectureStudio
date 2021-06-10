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

package org.lecturestudio.core.net.rtp.header;

/**
 * Specific vorbis/theora header. Total length is 4 bytes.
 * 
 * @author Alex Andres
 *
 */
public class VorbisHeader implements RtpPayloadHeader {

	/*
	 * 0 1 2 3
	 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * | Ident | F |VDT|# pkts.|
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 */

	/**
	 * Ident field is used to associate the Vorbis data to a decoding Configuration.
	 * It is stored as a network byte order integer.
	 */
	public int cbident; // 24 bits

	/**
	 * Fragment type is set according to the following list:
	 * <ul>
	 * <li>0 = Not Fragmented</li>
	 * <li>1 = Start Fragment</li>
	 * <li>2 = Continuation Fragment</li>
	 * <li>3 = End Fragment</li>
	 * </ul>
	 */
	public int fragType; // 2 bits

	/**
	 * Vorbis Data Type specifies the kind of Vorbis data stored in this RTP packet.
	 * There are currently three different types of Vorbis payloads.
	 * Each packet MUST contain only a single type of Vorbis packet.
	 * <ul>
	 * <li>0 = Raw Vorbis payload</li>
	 * <li>1 = Vorbis Packed Configuration payload</li>
	 * <li>2 = Legacy Vorbis Comment payload</li>
	 * <li>3 = Reserved</li>
	 * </ul>
	 * 
	 * The packets with a VDT of value 3 MUST be ignored.
	 */
	public int dataType; // 2 bits

	/**
	 * The number of complete packets in the current payload.
	 * This provides for a maximum number of 15 Vorbis packets in the payload.
	 * If the payload contains fragmented data, the number of packets <b>MUST</b> be set to 0.
	 */
	public int packets; // 4 bits


	/**
	 * Creates a new {@link VorbisHeader}.
	 */
	public VorbisHeader() {

	}

	/**
	 * Creates a new {@link VorbisHeader} with specified data.
	 * 
	 * @param data The field data
	 */
	public VorbisHeader(byte[] data) {
		if (data.length < 4) {
			// TODO
		}

		cbident = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
		fragType = (data[3] & 0xc0) >> 6;
		dataType = (data[3] & 0x30) >> 4;
		packets = (data[3] & 0x0f);
	}

	@Override
	public byte[] toByteArray() {
		byte[] header = new byte[4];

		header[0] = (byte) ((cbident >>> 16) & 0xff);
		header[1] = (byte) ((cbident >>> 8) & 0xff);
		header[2] = (byte) (cbident & 0xff);

		header[3] = 0;
		header[3] |= fragType << 6;
		header[3] |= dataType << 4;
		header[3] |= packets & 0xf;

		return header;
	}

	@Override
	public int getLength() {
		return 4;
	}

}
