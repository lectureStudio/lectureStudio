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

package org.lecturestudio.core.model;

import org.lecturestudio.core.model.listener.DocumentListChangeListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class DocumentList {

	private final List<Document> documents = new ArrayList<>();

	private final transient List<DocumentListChangeListener> listeners = new ArrayList<>();


	public boolean add(Document doc) {
		if (documents.contains(doc)) {
			return false;
		}

		boolean inserted = documents.add(doc);

		if (inserted) {
			notifyInserted(doc);
		}

		return inserted;
	}

	public boolean remove(Document doc) {
		boolean removed = documents.remove(doc);

		if (removed) {
			notifyRemoved(doc);
		}

		return removed;
	}

	public boolean replace(Document oldDoc, Document newDoc) {
		int index = documents.indexOf(oldDoc);

		if (index > -1) {
			documents.set(index, newDoc);
			notifyReplaced(oldDoc, newDoc);
			return true;
		}

		return false;
	}

	public boolean select(Document doc) {
		if (isNull(doc)) {
			return false;
		}

		Document prev = getSelectedDocument();

		// Move new document at the tail in the list.
		if (documents.remove(doc) && documents.add(doc)) {
			notifySelected(prev, doc);
			return true;
		}

		return false;
	}

	public boolean contains(Document doc) {
		return documents.contains(doc);
	}

	public Document getSelectedDocument() {
		if (documents.isEmpty()) {
			return null;
		}

		return documents.get(documents.size() - 1);
	}

	public List<Document> asList() {
		return new ArrayList<>(documents);
	}

	public Document getFirstWhiteboard() {
		return documents.stream().filter(Document::isWhiteboard).findFirst().orElse(null);
	}

	public Document getLastWhiteboard() {
		return documents.stream().filter(Document::isWhiteboard).reduce((first, second) -> second).orElse(null);
	}

	public Document getLastNonWhiteboard() {
		return documents.stream().filter(Document::isPDF).reduce((first, second) -> second).orElse(null);
	}

	public Document getFirstScreenCapture() {
		return documents.stream().filter(Document::isScreenCapture).findFirst().orElse(null);
	}

	public Document getLastNonScreenCapture() {
		Document lastDocument = getLastWhiteboard();

		// Try get last non whiteboard if no whiteboard exists
		if (isNull(lastDocument)) {
			lastDocument = getLastNonWhiteboard();
		}
		return lastDocument;
	}

	public List<Document> getPdfDocuments() {
		return documents.stream().filter(Document::isPDF).collect(Collectors.toList());
	}

	public int getWhiteboardCount() {
		long count = documents.stream().filter(doc -> doc.getType() == DocumentType.WHITEBOARD).count();

		return (int) count;
	}

	public int getScreenCaptureCount() {
		return (int) documents.stream().filter(Document::isScreenCapture).count();
	}

	public Optional<Document> getDocumentByFile(File file) {
		return documents.stream().filter(doc -> file.equals(doc.getFile())).findFirst();
	}

	public Optional<Document> getScreenCaptureById(long sourceId) {
		return documents.stream().filter(doc -> doc.isScreenCapture() && doc.getScreenCaptureDocument().getId() == sourceId).findFirst();
	}

	public int size() {
		return documents.size();
	}

	/**
	 * Adds a listener to be notified when changes to the list occur.
	 * 
	 * @param listener The listener to be notified on list changes.
	 */
	public void addListener(DocumentListChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a previously added listener.
	 * 
	 * @param listener The listener to remove.
	 */
	public void removeListener(DocumentListChangeListener listener) {
		listeners.remove(listener);
	}

	private void notifyInserted(Document doc) {
		for (DocumentListChangeListener listener : listeners) {
			listener.documentInserted(doc);
		}
	}

	private void notifyRemoved(Document doc) {
		for (DocumentListChangeListener listener : listeners) {
			listener.documentRemoved(doc);
		}
	}

	private void notifySelected(Document prevDoc, Document newDoc) {
		for (DocumentListChangeListener listener : listeners) {
			listener.documentSelected(prevDoc, newDoc);
		}
	}

	private void notifyReplaced(Document prevDoc, Document newDoc) {
		for (DocumentListChangeListener listener : listeners) {
			listener.documentReplaced(prevDoc, newDoc);
		}
	}
}
