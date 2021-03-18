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

package org.lecturestudio.core.net.protocol.ptip;

import java.nio.ByteBuffer;

import org.lecturestudio.core.net.protocol.DecodeMessageException;
import org.lecturestudio.core.net.protocol.ptip.message.PTIPAuthRequest;
import org.lecturestudio.core.net.protocol.ptip.message.PTIPChangeDocument;
import org.lecturestudio.core.net.protocol.ptip.message.PTIPDescriptionMessage;
import org.lecturestudio.core.net.protocol.ptip.message.PTIPFileChunk;
import org.lecturestudio.core.net.protocol.ptip.message.PTIPPage;

/**
 * This class decodes incoming packets to {@link PTIPMessage}s.
 * 
 * @author Alex Andres
 *
 */
public class PTIPMessageDecoder {

	/**
	 * Decode incoming packet to {@link PTIPMessage}.
	 * 
	 * @param data the packet represented by byte array
	 * @return the decoded {@link PTIPMessage}
	 * 
	 * @throws DecodeMessageException
	 */
	public PTIPMessage decode(byte[] data) throws DecodeMessageException {
		if (data.length < 4) {
			throw new DecodeMessageException("Invalid protocol message received. Message too short.");
		}

		ByteBuffer buffer = ByteBuffer.wrap(data);

		int headerId = buffer.get() & 0xFF;

		switch (headerId) {
			case 1:
				return decodeSessionDescription(buffer);

			case 2:
				return decodeFileChunk(buffer);

			case 3:
				return decodeChangeDocument(buffer);

			case 4:
				return decodeAuthRequest(buffer);
				
			case 5:
				return decodePage(buffer);

			default:
				throw new DecodeMessageException("Protocol message could not be identified.");
		}
	}
	
	protected PTIPMessage decodePage(ByteBuffer buffer) throws DecodeMessageException {
		if (buffer.remaining() < 3) {
			throw new DecodeMessageException("Corrupt protocol message received.");
		}

		buffer.get();
		buffer.getShort();
		int type = buffer.get() & 0xFF;
		int documentId = buffer.getInt();
		int pageNumber = buffer.getInt();
		int length = buffer.getInt();
		byte[] pageData = null;
		
		if (length > 0) {
			pageData = new byte[length];
			buffer.get(pageData);
		}

		return new PTIPPage(documentId, type, pageNumber, pageData);
	}

	/**
	 * Decode incoming packet to {@link PTIPDescriptionMessage}.
	 * 
	 * @param buffer the {@link ByteBuffer} that holds the packet.
	 * @return the decoded {@link PTIPDescriptionMessage}
	 * 
	 * @throws DecodeMessageException
	 */
	private PTIPMessage decodeSessionDescription(ByteBuffer buffer) throws DecodeMessageException {
		if (buffer.remaining() < 12) {
			throw new DecodeMessageException("Corrupt protocol message received.");
		}

		buffer.get(); // read empty byte
		int remaining = buffer.getShort() - 4;

		if (remaining != buffer.remaining()) {
			throw new DecodeMessageException("Incomplete message received.");
		}

		byte[] desc = new byte[remaining];
		buffer.get(desc);

		return new PTIPDescriptionMessage(new String(desc));
	}

	/**
	 * Decode incoming packet to {@link PTIPFileChunk}.
	 * 
	 * @param buffer the {@link ByteBuffer} that holds the packet.
	 * @return the decoded {@link PTIPFileChunk}
	 * 
	 * @throws DecodeMessageException
	 */
	private PTIPMessage decodeFileChunk(ByteBuffer buffer) throws DecodeMessageException {
		if (buffer.remaining() < 11) {
			throw new DecodeMessageException("Corrupt protocol message received.");
		}

		buffer.get(); // read empty byte

		int remaining = buffer.getShort() - 4;

		if (remaining != buffer.remaining()) {
			throw new DecodeMessageException("Incomplete message received.");
		}

		int totalBytes = buffer.getInt();
		int chunkLength = buffer.getInt();

		byte[] chunk = null;

		if (chunkLength > 0) {
			chunk = new byte[chunkLength];
			buffer.get(chunk);
		}

		return new PTIPFileChunk(chunk, totalBytes);
	}

	/**
	 * Decode incoming packet to {@link PTIPChangeDocument}.
	 * 
	 * @param buffer the {@link ByteBuffer} that holds the packet.
	 * @return the decoded {@link PTIPChangeDocument}
	 * 
	 * @throws DecodeMessageException
	 */
	private PTIPMessage decodeChangeDocument(ByteBuffer buffer) throws DecodeMessageException {
		if (buffer.remaining() < 3) {
			throw new DecodeMessageException("Corrupt protocol message received.");
		}

		buffer.get();
		buffer.getShort();
		int type = buffer.get() & 0xFF;
		int documentId = buffer.getInt();
		int documentSize = buffer.getInt();
		int titleSize = buffer.getInt();
		
		byte[] str = new byte[titleSize];
		buffer.get(str);

		return new PTIPChangeDocument(new String(str), documentId, documentSize, type);
	}

	/**
	 * Decode incoming packet to {@link PTIPAuthRequest}.
	 * 
	 * @param buffer the {@link ByteBuffer} that holds the packet.
	 * @return the decoded {@link PTIPAuthRequest}
	 * 
	 * @throws DecodeMessageException
	 */
	private PTIPMessage decodeAuthRequest(ByteBuffer buffer) throws DecodeMessageException {
		if (buffer.remaining() < 3) {
			throw new DecodeMessageException("Corrupt protocol message received.");
		}

		return new PTIPAuthRequest();
	}

}
