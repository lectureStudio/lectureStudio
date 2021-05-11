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
package org.lecturestudio.core.bus.event;

import org.lecturestudio.core.model.DocumentType;

public abstract class StreamDocumentEvent extends ApplicationControllerEvent {

	/** The type of the document. */
	private DocumentType documentType;

	/** The title of the document. */
	private String documentTitle;

	/** The file name of the document. */
	private String documentFileName;

	/** The document checksum. */
	private String documentChecksum;

	/**
	 *  Create the {@link StreamDocumentEvent} with specified type and document title.
	 *
	 * @param type The type of the {@link StreamDocumentEvent}.
	 * @param documentTitle The document title.
	 */
	public StreamDocumentEvent(DocumentType type, String documentTitle) {
		this.documentType = type;
		this.documentTitle = documentTitle;
		
		setSynchronous(true);
	}

	/**
	 * Get the type of the document.
	 *
	 * @return The type of the document.
	 */
	public DocumentType getDocumentType() {
		return documentType;
	}

	/**
	 * Get the file name of the document.
	 *
	 * @return The file name of the document.
	 */
	public String getDocumentFileName() {
		return documentFileName;
	}

	/**
	 * Set the {@link #documentFileName}.
	 *
	 * @param documentFileName The new file name.
	 */
	public void setDocumentFileName(String documentFileName) {
		this.documentFileName = documentFileName;
	}

	/**
	 * Get the document checksum.
	 *
	 * @return The document checksum.
	 */
	public String getDocumentChecksum() {
		return documentChecksum;
	}

	/**
	 * Set the {@link #documentChecksum}.
	 *
	 * @param documentChecksum The new checksum.
	 */
	public void setDocumentChecksum(String documentChecksum) {
		this.documentChecksum = documentChecksum;
	}

	/**
	 * Get the document title.
	 *
	 * @return The document title.
	 */
	public String getDocumentTitle() {
		return documentTitle;
	}

	/**
	 * Set the {@link #documentTitle}.
	 *
	 * @param documentTitle The new title.
	 */
	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	@Override
	public Object getData() {
		return this;
	}

}
