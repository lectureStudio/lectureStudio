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

public class PTIPPage implements PTIPMessage {

	/**
	 * The documents unique id.
	 */
	private final int documentId;

	/**
	 * The page number.
	 */
	private final int pageNumber;

	/**
	 * The document type: 0 = PDF, 1 = Whiteboard, 2 = Quiz
	 */
	private final int type;

	/**
	 * The page data.
	 */
	private final byte[] data;


	/**
	 * Create a new {@link PTIPPage} with the specified parameters.
	 *
	 * @param documentId The documents unique id.
	 * @param type The document type: 0 = PDF, 1 = Whiteboard, 2 = Quiz
	 * @param pageNumber The page number.
	 * @param data The page data.
	 */
	public PTIPPage(int documentId, int type, int pageNumber, byte[] data) {
		this.documentId = documentId;
		this.type = type;
		this.pageNumber = pageNumber;
		this.data = data;
	}

	/**
	 * Get the unique id of the document.
	 * 
	 * @return The unique id of the document.
	 */
	public int getDocumentID() {
		return documentId;
	}

	/**
	 * Get the type of the document.
	 *
	 * @return The type of the document.
	 */
	public int getDocumentType() {
		return type;
	}

	/**
	 * Get the page number.
	 *
	 * @return The page number.
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * Get the page data.
	 *
	 * @return The page data.
	 */
	public byte[] getPageData() {
		return data;
	}

	@Override
	public byte[] toByteArray() {
		byte headerId = (byte) PTIPMessageCode.PAGE.getID();
		byte docType = (byte) type;

		int dataLength = data != null ? data.length : 0;
		short length = (short) (17 + dataLength);

		ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.put(headerId);
		buffer.put((byte) 0);
		buffer.putShort(length);
		buffer.put(docType);
		buffer.putInt(documentId);
		buffer.putInt(pageNumber);
		buffer.putInt(dataLength);

		if (data != null) {
			buffer.put(data);
		}

		return buffer.array();
	}

	@Override
	public PTIPMessageCode getMessageCode() {
		return PTIPMessageCode.PAGE;
	}

}
