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

public class StreamPageChangeEvent extends ApplicationControllerEvent {

	/** The type of the document. */
	private DocumentType type;

	/** The id of the document. */
	private int documentId;

	/** The page number. */
	private int pageNumber;

	/** The page data. */
	private Object data;

	/**
	 * Create the {@link StreamPageChangeEvent} with specified command, type, document id, page number and page data.
	 *
	 * @param command
	 * @param type The type of the document.
	 * @param documentId The id of the document.
	 * @param pageNumber The page number.
	 * @param data The page data.
	 */
	public StreamPageChangeEvent(String command, DocumentType type, int documentId, int pageNumber, Object data) {
		super(command);
		
		this.type = type;
		this.documentId = documentId;
		this.pageNumber = pageNumber;
		this.data = data;
	}

	/**
	 * Get the type of the document.
	 *
	 * @return The type of the document.
	 */
	public DocumentType getDocumentType() {
		return type;
	}

	/**
	 * Get the id of the document.
	 *
	 * @return The id of the document.
	 */
	public int getDocumentID() {
		return documentId;
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
	public Object getPageData() {
		return data;
	}

}
