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
 * The {@code PTIPChangeDocument} is used if the user opened a new document or
 * switched to a existing one. The packet is defined as followed:
 * 
 * <pre>
 * 0                   1                   2                   3 
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |     Code    |   Reserved    |             Length              | 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |  Doc. Type  |                   Document ID                   | 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                         Document Size                         | 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                      Document Title Size                      | 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                       Document Title ...                      | 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author Alex Andres
 * 
 */
public class PTIPChangeDocument implements PTIPMessage {

	/** The documents unique id. */
	private final int documentId;
	
	/** The file size of the document, 0 if whiteboard. */
	private final int documentSize;
	
	/** The documents title. */
	private final String documentTitle;

	/** The document type: 0 = PDF, 1 = Whiteboard, 2 = Quiz. */
	private final int type;


	/**
	 * Creates a new {@link PTIPChangeDocument} message with specified
	 * parameters.
	 * 
	 * @param documentId
	 *            the documents unique id
	 * @param documentSize
	 *            the file size of the document, 0 if whiteboard
	 * @param isWhiteboard
	 *            true if whiteboard, false otherwise
	 */
	public PTIPChangeDocument(String title, int documentId, int documentSize, int type) {
		this.documentTitle = title;
		this.documentId = documentId;
		this.documentSize = documentSize;
		this.type = type;
	}

	/**
	 * Returns the unique id of the document.
	 * 
	 * @return unique id of the document
	 */
	public int getDocumentId() {
		return documentId;
	}
	
	/**
	 * Returns the size of the document.
	 * 
	 * @return the size of the document.
	 */
	public int getDocumentSize() {
		return documentSize;
	}

	/**
	 * Returns the title of the document.
	 * 
	 * @return title of the document.
	 */
	public String getDocumentTitle() {
		return documentTitle;
	}
	
	/**
	 * Returns true if document is whiteboard, false otherwise.
	 * 
	 * @return true if whiteboard, false otherwise
	 */
	public int getDocumentType() {
		return type;
	}

	@Override
	public byte[] toByteArray() {
		byte headerId = (byte) PTIPMessageCode.CHANGE_DOCUMENT.getID();
		byte docType = (byte) type;
		byte[] title = documentTitle.getBytes();
		
		short length = (short) (17 + title.length);

		ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.put(headerId);
		buffer.put((byte) 0);
		buffer.putShort(length);
		buffer.put(docType);
		buffer.putInt(documentId);
		buffer.putInt(documentSize);
		buffer.putInt(title.length);
		buffer.put(title);

		return buffer.array();
	}

	@Override
	public PTIPMessageCode getMessageCode() {
		return PTIPMessageCode.CHANGE_DOCUMENT;
	}

}
