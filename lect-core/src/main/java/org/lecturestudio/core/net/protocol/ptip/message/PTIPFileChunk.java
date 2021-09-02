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

package org.lecturestudio.core.net.protocol.ptip.message;

import java.nio.ByteBuffer;

import org.lecturestudio.core.net.protocol.ptip.PTIPMessage;
import org.lecturestudio.core.net.protocol.ptip.PTIPMessageCode;

/**
 * The {@link PTIPFileChunk} is used to transport pieces of loaded documents.
 * The packet is defined as followed:
 * 
 * <pre>
 * 0                   1                   2                   3 
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |     Code    |   Reserved    |             Length              | 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                          Document ID                          | 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                         Chunk Length                          | 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                         Chunk Data ...                        | 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author Alex Andres
 * 
 */
public class PTIPFileChunk implements PTIPMessage {

	/**
	 * The chunks data.
	 */
	private final byte[] chunkData;

	/**
	 * The documents unique id.
	 */
	private final int documentId;


	/**
	 * Creates a new {@link PTIPFileChunk} with specified parameters.
	 * 
	 * @param chunkData The data of the chunk.
	 * @param documentId The documents unique id.
	 */
	public PTIPFileChunk(byte[] chunkData, int documentId) {
		this.chunkData = chunkData;
		this.documentId = documentId;
	}

	/**
	 * Get the unique id of the document.
	 * 
	 * @return The unique id of the document.
	 */
	public int getDocumentId() {
		return documentId;
	}

	/**
	 * Get the length of the chunk.
	 * 
	 * @return The length of the chunk.
	 */
	public int getChunkDataLength() {
		return chunkData.length;
	}

	/**
	 * Get the chunks data.
	 * 
	 * @return The data of the chunk.
	 */
	public byte[] getChunkData() {
		return chunkData;
	}

	@Override
	public byte[] toByteArray() {
		byte headerId = (byte) PTIPMessageCode.FILE_CHUNK.getID();

		short length = (short) (12 + chunkData.length);

		ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.put(headerId);
		buffer.put((byte) 0);
		buffer.putShort(length);
		buffer.putInt(documentId);
		buffer.putInt(chunkData.length);
		buffer.put(chunkData);

		return buffer.array();
	}

	@Override
	public PTIPMessageCode getMessageCode() {
		return PTIPMessageCode.FILE_CHUNK;
	}

}
