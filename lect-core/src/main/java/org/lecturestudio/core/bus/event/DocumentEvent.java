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

import org.lecturestudio.core.model.Document;

public class DocumentEvent extends BusEvent {

	/** Enum with the {@link DocumentEvent} types. */
	public enum Type { CREATED, CLOSED, SELECTED, REPLACED }

	/** the old {@link Document}. */
	private final Document oldDocument;

	/** the {@link Document}. */
	private final Document document;

	/** the {@link Type} of the {@link DocumentEvent}. */
	private final Type type;

	/**
	 * Create the {@link DocumentEvent} with specified document and type ({@link #oldDocument} will be {@code null}).
	 *
	 * @param document The document.
	 * @param type The type of the {@link DocumentEvent}.
	 */
	public DocumentEvent(Document document, Type type) {
		this(null, document, type);
	}

	/**
	 *  Create the {@link DocumentEvent} with specified old document, document and type.
	 *
	 * @param oldDocument The old document.
	 * @param document The document.
	 * @param type The type of the {@link DocumentEvent}.
	 */
	public DocumentEvent(Document oldDocument, Document document, Type type) {
		this.oldDocument = oldDocument;
		this.document = document;
		this.type = type;
	}

	/**
	 * Get the old document.
	 *
	 * @return The old document.
	 */
	public Document getOldDocument() {
		return oldDocument;
	}

	/**
	 * Get the document.
	 *
	 * @return The document.
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Get the type of the {@link DocumentEvent}.
	 *
	 * @return The type of the {@link DocumentEvent}.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Indicates whether {@link DocumentEvent} is created.
	 *
	 * @return {@code true} if the {@link #type} equals {@code Type.CREATED}, otherwise {@code false}.
	 */
	public boolean created() {
		return type == Type.CREATED;
	}

	/**
	 * Indicates whether {@link DocumentEvent} is closed.
	 *
	 * @return {@code true} if the {@link #type} equals {@code Type.CLOSED}, otherwise {@code false}.
	 */
	public boolean closed() {
		return type == Type.CLOSED;
	}

	/**
	 * Indicates whether {@link DocumentEvent} is selected.
	 *
	 * @return {@code true} if the {@link #type} equals {@code Type.SELECTED}, otherwise {@code false}.
	 */
	public boolean selected() {
		return type == Type.SELECTED;
	}
}
