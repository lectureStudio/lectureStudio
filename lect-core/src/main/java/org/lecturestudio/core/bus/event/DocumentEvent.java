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

	public enum Type { CREATED, CLOSED, SELECTED, REPLACED }

	private final Document oldDocument;

	private final Document document;
	
	private final Type type;
	

	public DocumentEvent(Document document, Type type) {
		this(null, document, type);
	}

	public DocumentEvent(Document oldDocument, Document document, Type type) {
		this.oldDocument = oldDocument;
		this.document = document;
		this.type = type;
	}

	public Document getOldDocument() {
		return oldDocument;
	}

	public Document getDocument() {
		return document;
	}
	
	public Type getType() {
		return type;
	}
	
	public boolean created() {
		return type == Type.CREATED;
	}
	
	public boolean closed() {
		return type == Type.CLOSED;
	}
	
	public boolean selected() {
		return type == Type.SELECTED;
	}
}
