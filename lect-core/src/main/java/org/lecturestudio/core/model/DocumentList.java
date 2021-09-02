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

import static java.util.Objects.isNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.lecturestudio.core.model.listener.DocumentListChangeListener;

public class DocumentList {

	/** The list of documents. */
	private final List<Document> documents = new ArrayList<>();

	private final transient List<DocumentListChangeListener> listeners = new ArrayList<>();


	/**
	 * Appends the specified document to the tail of {@link #documents},
	 * except {@link #documents} already contains the document.
	 *
	 * @param doc The document to add.
	 *
	 * @return {@code true} if the document has been added successfully,
	 * {@code false} if {@link #documents} already contained the document.
	 */
	public boolean add(Document doc) {
		if (documents.contains(doc)) {
			return false;
		}

		documents.add(doc);

		notifyInserted(doc);

		return true;
	}

	/**
	 * Removes the specified document from the {@link #documents}.
	 *
	 * @param doc The document to remove.
	 *
	 * @return {@code true} if the document has been removed successfully, otherwise {@code false}.
	 */
	public boolean remove(Document doc) {
		boolean removed = documents.remove(doc);

		if (removed) {
			notifyRemoved(doc);
		}

		return removed;
	}

	/**
	 * Replaces the specified document with the new one.
	 *
	 * @param oldDoc The document to be replaced.
	 * @param newDoc The new document.
	 *
	 * @return {@code false} if the document to be replaced is not part of {@link #documents}, otherwise {@code true}.
	 */
	public boolean replace(Document oldDoc, Document newDoc) {
		int index = documents.indexOf(oldDoc);

		if (index > -1) {
			documents.set(index, newDoc);
			notifyReplaced(oldDoc, newDoc);
			return true;
		}

		return false;
	}

	/**
	 * Selects the specified document by moving it to the tail of {@link #documents}.
	 *
	 * @param doc The document that should be selected.
	 *
	 * @return {@code false} if the document is null or is not part of {@link #documents}, otherwise {@code true}.
	 */
	public boolean select(Document doc) {
		if (isNull(doc)) {
			return false;
		}

		Document prev = getSelectedDocument();

		// Move new document to the tail of the list.
		if (documents.remove(doc)) {
			documents.add(doc);
			notifySelected(prev, doc);
			return true;
		}

		return false;
	}

	/**
	 * Specifies whether {@link #documents} contains the specified document.
	 *
	 * @param doc The document.
	 *
	 * @return {@code true} if {@link #documents} contains the specified document, otherwise {@code false}.
	 */
	public boolean contains(Document doc) {
		return documents.contains(doc);
	}

	/**
	 * Get the selected document (last document in {@link #documents}).
	 *
	 * @return The selected document or {@code null} if there are no documents.
	 */
	public Document getSelectedDocument() {
		if (documents.isEmpty()) {
			return null;
		}

		return documents.get(documents.size() - 1);
	}

	/**
	 * Returns {@link #documents} as a new {@link List}.
	 *
	 * @return {@link #documents} as a new {@link List}.
	 */
	public List<Document> asList() {
		return new ArrayList<>(documents);
	}

	/**
	 * Get the first document in {@link #documents} that is a whiteboard.
	 *
	 * @return The first document in {@link #documents} that is a whiteboard or
	 * {@code null} if no such document was found.
	 */
	public Document getFirstWhiteboard() {
		return documents.stream().filter(Document::isWhiteboard).findFirst().orElse(null);
	}

	/**
	 * Get the last document in {@link #documents} that is a PDF document.
	 *
	 * @return the last document in {@link #documents} that is a PDF document or
	 * {@code null} if no such document was found.
	 */
	public Document getLastNonWhiteboard() {
		return documents.stream().filter(Document::isPDF).reduce((first, second) -> second).orElse(null);
	}

	/**
	 * Get a list of all the documents in {@link #documents} that are PDF documents.
	 *
	 * @return a list of all the documents in {@link #documents} that are PDF documents.
	 */
	public List<Document> getPdfDocuments() {
		return documents.stream().filter(Document::isPDF).collect(Collectors.toList());
	}

	/**
	 * Get the number of documents in {@link #documents} that are whiteboards.
	 *
	 * @return the number of documents in {@link #documents} that are whiteboards.
	 */
	public int getWhiteboardCount() {
		long count = documents.stream().filter(Document::isWhiteboard).count();

		return (int) count;
	}

	/**
	 * Get the first document whose file equals the specified file.
	 *
	 * @param file The file.
	 *
	 * @return The first document whose file equals the specified file.
	 */
	public Optional<Document> getDocumentByFile(File file) {
		return documents.stream().filter(doc -> file.equals(doc.getFile())).findFirst();
	}

	/**
	 * Get the size of {@link #documents}.
	 *
	 * @return The size of {@link #documents}.
	 */
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
