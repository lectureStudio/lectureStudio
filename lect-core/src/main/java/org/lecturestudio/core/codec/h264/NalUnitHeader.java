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

/**
 * Network Abstraction Layer (NAL) unit implementation for H.264 Video based on
 * RFC 6184. A NAL unit consist of a single NAL unit type octet, which also
 * co-serves as the payload header of this RTP payload format. The header
 * consists of a forbidden zero bit, NAL reference id and the NAL unit type. The
 * forbidden zero bit declares, if set to 1, a syntax violation. For the H.264
 * Video packet format forbidden zero bit shall be 0. The NAL reference id
 * indicates the relative transport priority, as determined by the encoder. More
 * important NAL units have greater values, maximum value is 11 (binary).
 * Receivers need not manipulate this value. The NAL unit type specifies the
 * payload type.
 *
 * @author Alex Andres
 */
public class NalUnitHeader {

	/** Forbidden zero bit. */
	private boolean forbiddenZeroBit;

	/** NAL reference id. */
	private int nri;

	/** NAL unit type, 0 - 31. */
	private int type;


	/**
	 * Creates a new Network Abstraction Layer (NAL) unit header.
	 *
	 * @param forbiddenZeroBit The forbidden zero bit.
	 * @param NRI              The NAL reference id.
	 * @param type             The NAL unit type, 0 - 31.
	 */
	NalUnitHeader(boolean forbiddenZeroBit, int NRI, int type) {
		this.forbiddenZeroBit = forbiddenZeroBit;
		this.nri = NRI;
		this.type = type;
	}

	/**
	 * Extracts the NAL Unit header from the provided byte array.
	 *
	 * @param data The byte array from a packet payload.
	 *
	 * @return the extracted {@link NalUnitHeader}.
	 *
	 * @throws IllegalArgumentException If the packet data is null.
	 */
	public static NalUnitHeader parse(byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("Cannot extract NAL Unit header. Packet data is null.");
		}

		byte firstByte = data[0];
		boolean forbiddenZeroBit = ((firstByte & 0x80) >> 7) != 0;
		int nri = ((firstByte & 0x60) >> 5);
		int type = (firstByte & 0x1f);

		return new NalUnitHeader(forbiddenZeroBit, nri, type);
	}

	/**
	 * Returns the forbidden zero bit.
	 *
	 * @return 1 if it's set, 0 otherwise.
	 */
	public int getForbiddenZeroBit() {
		return forbiddenZeroBit ? 1 : 0;
	}

	/**
	 * Returns the NAL reference id.
	 *
	 * @return NAL reference id.
	 */
	public int getNalReferenceId() {
		return nri;
	}

	/**
	 * Returns the NAL unit type.
	 *
	 * @return NAL unit type.
	 */
	public NalUnitType getNalUnitType() {
		return NalUnitType.parse(type);
	}

	/**
	 * Verifies if the H.264 packet is a single NAL unit packet.
	 *
	 * @return {@code true} if the packet represents a single NAL unit, otherwise {@code false}.
	 */
	boolean isSingleNalUnit() {
		return type > 0 && type < 24;
	}

	/**
	 * Verifies if the H.264 packet is a aggregation unit packet.
	 *
	 * @return {@code true} if the packet represents a aggregation unit, otherwise {@code false}.
	 */
	boolean isAggregationUnit() {
		return type > 23 && type < 28;
	}

	/**
	 * Verifies if the H.264 packet is a fragmentation unit packet.
	 *
	 * @return {@code true} if the packet represents a fragmentation unit, otherwise {@code false}.
	 */
	boolean isFragmentationUnit() {
		return type > 27 && type < 30;
	}

}
