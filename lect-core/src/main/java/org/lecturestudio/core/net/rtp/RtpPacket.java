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

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.net.packet.Packet;
import org.lecturestudio.core.net.rtp.header.RtpPayloadHeader;

/**
 * This class represents a RTP packet. The {@link RtpPacket} holds all RTP header fields that can be set or accessed.
 * 
 * @author Alex Andres
 * 
 */
public class RtpPacket implements Packet, Cloneable {

	/*
	 * RTP data header
	 */

	/** Protocol version */
	private int version = 2; // 2 bits

	/** Padding flag */
	private int padding; // 1 bit

	/** Header extension flag */
	private int extension = 0; // 1 bit

	/** Marker bit */
	private int marker = 0; // 1 bit

	/** Payload type */
	private int payloadType; // 7 bits

	/** Sequence number */
	private int seqNumber; // 16 bits

	/** Timestamp */
	private long timestamp; // 32 bits

	/** Synchronization source */
	private long ssrc; // 32 bits

	/** Optional CSRC list */
	private long[] csrc = null; // 32 x n bits, n < 16

	/**
	 * RTP payload data
	 */

	private final List<RtpPayloadHeader> headers = new ArrayList<>();

	/** Actual data, without header */
	private byte[] payload = null;


	/**
	 * Creates a new {@link RtpPacket}.
	 */
	public RtpPacket() {

	}

	/**
	 * Creates a new {@link RtpPacket} with provided payload.
	 * 
	 * @param data
	 * @param dataLength
	 */
	public RtpPacket(byte[] data, int dataLength) {
		if (dataLength < 12) {
			// TODO
			return;
		}

		/* Parse RTP header */
		version = (data[0] & 0xc0) >>> 6;
		padding = (data[0] & 0x20) >>> 5;
		extension = (data[0] & 0x10) >>> 4;
		int CC = (data[0] & 0x0f);

		marker = (data[1] & 0x80) >> 7;
		payloadType = (data[1] & 0x7F);
		seqNumber = ((data[2] & 0xff) << 8) | (data[3] & 0xff);

		timestamp = ((data[4] & 0xff) << 24) | ((data[5] & 0xff) << 16) | ((data[6] & 0xff) << 8) + (data[7] & 0xff);
		timestamp &= 0xFFFFFFFFL;

		ssrc = ((data[8] & 0xff) << 24) | ((data[9] & 0xff) << 16) + ((data[10] & 0xff) << 8) + (data[11] & 0xff);

		if (CC > 0) {
			csrc = new long[CC];
			for (int i = 0; i < CC; i++) {
				int offset = 12 + i * 4;
				csrc[i] = (data[offset] << 24) + (data[offset + 1] << 16) + (data[offset + 2] << 8) + data[offset + 3];
			}
		}

		payload = new byte[dataLength - 12 - 4 * CC];

		System.arraycopy(data, 12 + 4 * CC, payload, 0, payload.length);
	}

	@Override
	public byte[] toByteArray() {
		int packetLength = 12 + getCsrcCount() * 4;

		for (RtpPayloadHeader header : headers) {
			packetLength += header.getLength();
		}

		if (payload != null) {
			packetLength += payload.length;
		}

		byte[] data = new byte[packetLength];

		data[0] = (byte) ((version << 6) | (padding << 5) | (extension << 4) | getCsrcCount());
		data[1] = (byte) ((marker << 7) | payloadType);
		data[2] = (byte) ((seqNumber >> 8) & 0xff);
		data[3] = (byte) (seqNumber & 0xff);

		for (int i = 0; i < 4; i++) {
			data[7 - i] = (byte) ((timestamp >>> (8 * i)) & 0xff);
		}

		for (int i = 0; i < 4; i++) {
			data[11 - i] = (byte) ((ssrc >>> (8 * i)) & 0xff);
		}

		for (int i = 0; i < getCsrcCount(); i++) {
			int offset = i * 4;
			for (int j = 0; j < 4; j++) {
				data[15 - j + offset] = (byte) (csrc[i] >>> (8 * j));
			}
		}

		int offset = 12 + getCsrcCount() * 4;

		for (RtpPayloadHeader header : headers) {
			int length = header.getLength();
			System.arraycopy(header.toByteArray(), 0, data, offset, length);
			offset += length;
		}

		System.arraycopy(payload, 0, data, offset, payload.length);

		return data;
	}

	/**
	 * Get the RTP version.
	 * 
	 * @return The version.
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Set the RTP version.
	 * 
	 * @param version The version.
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * Get the padding flag.
	 *
	 * @return The padding flag.
	 */
	public int getPadding() {
		return padding;
	}

	/**
	 * Set the padding flag.
	 *
	 * @param padding The padding flag.
	 */
	public void setPadding(int padding) {
		this.padding = padding;
	}

	/**
	 * Get the header extension flag.
	 *
	 * @return The header extension flag.
	 */
	public int getExtension() {
		return extension;
	}

	/**
	 * Set the header extension flag.
	 *
	 * @param extension The header extension flag.
	 */
	public void setExtension(int extension) {
		this.extension = extension;
	}

	/**
	 * Get the marker bit.
	 *
	 * @return The marker bit.
	 */
	public int getMarker() {
		return marker;
	}

	/**
	 * Set the marker bit.
	 *
	 * @param marker The marker bit.
	 */
	public void setMarker(int marker) {
		this.marker = marker;
	}

	/**
	 * Get the payload type.
	 *
	 * @return The payload type.
	 */
	public int getPayloadType() {
		return payloadType;
	}

	/**
	 * Set the payload type.
	 *
	 * @param payloadType The payload type.
	 */
	public void setPayloadType(int payloadType) {
		this.payloadType = payloadType;
	}

	/**
	 * Get the sequence number.
	 *
	 * @return The sequence number.
	 */
	public int getSeqNumber() {
		return seqNumber;
	}

	/**
	 * Set the sequence number.
	 *
	 * @param seqNumber The sequence number.
	 */
	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber & 0xFFFF;
	}

	/**
	 * Get the timestamp.
	 *
	 * @return The timestamp.
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Set the timestamp.
	 *
	 * @param timestamp The timestamp.
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Get the synchronization source.
	 *
	 * @return The synchronization source.
	 */
	public long getSsrc() {
		return ssrc;
	}

	/**
	 * Set the synchronization source.
	 *
	 * @param ssrc The synchronization source.
	 */
	public void setSsrc(int ssrc) {
		this.ssrc = ssrc & 0xFFFFFFFFL;
	}

	/**
	 * Get the CSRC list.
	 *
	 * @return The CSRC list.
	 */
	public long[] getCsrcArray() {
		return csrc;
	}

	/**
	 * Get the length of {@link #csrc}.
	 *
	 * @return The length of {@link #csrc} or {@code 0} if {@link #csrc} equals {@code null}.
	 */
	public int getCsrcCount() {
		return (csrc == null) ? 0 : csrc.length;
	}

	/**
	 * Set the CSRC list.
	 *
	 * @param csrcArray The CSRC list.
	 */
	public void setCsrcArray(long[] csrcArray) {
		this.csrc = csrcArray;
	}

	/**
	 * Get the payload (Actual data, without header).
	 *
	 * @return The payload.
	 */
	public byte[] getPayload() {
		return payload;
	}

	/**
	 * Get the length of {@link #payload}.
	 *
	 * @return The length of {@link #payload}.
	 */
	public int getPayloadLength() {
		return payload.length;
	}

	/**
	 * Get the length of the header.
	 *
	 * @return The length of the header.
	 */
	public int getHeaderLength() {
		return 12 + getCsrcCount() * 4;
	}

	/**
	 * Set the payload (Actual data, without header).
	 *
	 * @param payload The payload.
	 */
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	/**
	 * Add a {@link RtpPayloadHeader} to {@link #headers}.
	 *
	 * @param header The {@link RtpPayloadHeader} to add.
	 */
	public void addPayloadHeader(RtpPayloadHeader header) {
		headers.add(header);
	}

	/**
	 * Removes all headers from {@link #headers}.
	 */
	public void removeHeaders() {
		headers.clear();
	}

	/**
	 * Append a byte array to the end of the packet.
	 * 
	 * @param data Byte array to append.
	 */
	public void append(byte[] data, int length) {
		if (data == null || length == 0) {
			return;
		}

		// re-allocate internal buffer
		int oldLength = payload.length;
		byte[] newPayload = new byte[oldLength + length];
		System.arraycopy(payload, 0, newPayload, 0, oldLength);

		this.payload = newPayload;

		// append data
		System.arraycopy(data, 0, payload, oldLength, length);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public RtpPacket clone() {
		RtpPacket packet = new RtpPacket();
		packet.setCsrcArray(csrc);
		packet.setExtension(extension);
		packet.setMarker(marker);
		packet.setPadding(padding);
		packet.setPayload(payload);
		packet.setPayloadType(payloadType);
		packet.setSeqNumber(seqNumber);
		packet.setSsrc((int) ssrc);
		packet.setTimestamp(timestamp);
		packet.setVersion(version);

		for (RtpPayloadHeader header : headers)
			packet.addPayloadHeader(header);

		return packet;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		int csrcLength = csrc == null ? 0 : csrc.length;

		StringBuilder str = new StringBuilder("[RTP Packet] \n");
		str.append("V: ").append(version).append(" P: ").append(padding).append(" X: ").append(extension).append(" CC: ").append(csrcLength);
		str.append(" M: ").append(marker).append(" PT: ").append(payloadType).append(" Seq: ").append(seqNumber).append("\n");
		str.append("Timestamp: ").append(timestamp).append("\n");
		str.append("SSRC: ").append(ssrc).append("\n");

		for (int i = 0; i < csrcLength; i++) {
			str.append("CSRC: ").append(csrc[i]).append("\n");
		}

		str.append("Data length: ").append(payload.length).append("\n");

		return str.toString();
	}

}
