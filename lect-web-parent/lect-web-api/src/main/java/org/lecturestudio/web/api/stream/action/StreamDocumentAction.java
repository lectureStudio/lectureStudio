/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.stream.action;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentType;

public abstract class StreamDocumentAction extends StreamAction {

	private long documentId;

	private DocumentType documentType;

	private String documentTitle;

	private String documentFile;

	private String documentChecksum;


	public StreamDocumentAction(Document document) {
		setDocumentId(document.hashCode());
		setDocumentType(document.getType());
		setDocumentTitle(document.getName());
	}

	public StreamDocumentAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	public long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(long documentId) {
		this.documentId = documentId;
	}

	public DocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(DocumentType documentType) {
		this.documentType = documentType;
	}

	public String getDocumentFile() {
		return documentFile;
	}

	public void setDocumentFile(String documentName) {
		this.documentFile = documentName;
	}

	public String getDocumentChecksum() {
		return documentChecksum;
	}

	public void setDocumentChecksum(String documentChecksum) {
		this.documentChecksum = documentChecksum;
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		byte actionType = (byte) getType().ordinal();
		byte docType = (byte) documentType.ordinal();

		// Make sure that only the filename is used.
		byte[] titleBuffer = null;
		byte[] fileNameBuffer = null;
		byte[] checksumBuffer = null;

		int titleLength = 0;
		int fileNameLength = 0;
		int checksumLength = 0;
		int payloadLength = 8;

		if (documentTitle != null) {
			titleBuffer = documentTitle.getBytes();
			titleLength = titleBuffer.length;
			payloadLength += titleBuffer.length;
		}
		if (documentFile != null) {
			fileNameBuffer = documentFile.getBytes();
			fileNameLength = fileNameBuffer.length;
			payloadLength += fileNameBuffer.length;
		}
		if (documentChecksum != null) {
			checksumBuffer = documentChecksum.getBytes();
			checksumLength = checksumBuffer.length;
			payloadLength += checksumBuffer.length;
		}

		ByteBuffer buffer = ByteBuffer.allocate(22 + payloadLength);
		// write header
		buffer.putInt(14 + payloadLength);
		buffer.put(actionType);

		buffer.putLong(documentId);
		buffer.put(docType);
		buffer.putInt(titleLength);
		buffer.putInt(fileNameLength);
		buffer.putInt(checksumLength);

		if (titleBuffer != null) {
			buffer.put(titleBuffer);
		}
		if (fileNameBuffer != null) {
			buffer.put(fileNameBuffer);
		}
		if (checksumBuffer != null) {
			buffer.put(checksumBuffer);
		}

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(input);

		this.documentId = buffer.getLong();
		this.documentType = DocumentType.values()[buffer.get()];

		int titleLength = buffer.getInt();
		int nameLength = buffer.getInt();
		int checksumLength = buffer.getInt();

		if (titleLength > 0) {
			byte[] titleBuffer = new byte[titleLength];
			buffer.get(titleBuffer, 0, titleLength);

			this.documentTitle = new String(titleBuffer);
		}
		if (nameLength > 0) {
			byte[] nameBuffer = new byte[nameLength];
			buffer.get(nameBuffer, 0, nameLength);

			this.documentFile = new String(nameBuffer);
		}
		if (checksumLength > 0) {
			byte[] checksumBuffer = new byte[checksumLength];
			buffer.get(checksumBuffer, 0, checksumLength);

			this.documentChecksum = new String(checksumBuffer);
		}

		if (documentTitle == null) {
			throw new IOException("Missing title for a document.");
		}
	}

}
