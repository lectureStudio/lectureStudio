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

package org.lecturestudio.core.recording.action;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.model.DocumentType;

public abstract class DocumentAction extends PlaybackAction {

	private DocumentType documentType;

	private String documentTitle;

	private String documentName;

	private String documentChecksum;


	public DocumentAction(DocumentType type, String documentTitle, String documentFileName) {
		this.documentType = type;
		this.documentTitle = documentTitle;
		this.documentName = documentFileName;
	}

	public DocumentAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	public DocumentType getDocumentType() {
		return documentType;
	}

	public String getDocumentFileName() {
		return documentName;
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
		int payloadLength = 0;

		if (documentTitle != null) {
			titleBuffer = documentTitle.getBytes();
			titleLength = titleBuffer.length;
			payloadLength += titleBuffer.length;
		}
		if (documentName != null) {
			String fileName = new File(documentName).getName();
			fileNameBuffer = fileName.getBytes();
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
		buffer.putInt(18 + payloadLength);
		buffer.put(actionType);
		buffer.putInt(getTimestamp());

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

			this.documentName = new String(nameBuffer);
		}
		if (checksumLength > 0) {
			byte[] checksumBuffer = new byte[checksumLength];
			buffer.get(checksumBuffer, 0, checksumLength);

			this.documentChecksum = new String(checksumBuffer);
		}

		if (documentTitle == null) {
			throw new IOException("Missing title for a document.");
		}

		if (documentType == DocumentType.PDF) {
			if (documentName == null) {
				throw new IOException("Missing filename for a PDF document.");
			}

			if (documentChecksum == null) {
				// TODO: check for doc-close-action
				//throw new IOException("Missing checksum for a PDF document.");
			}
		}
	}

}
