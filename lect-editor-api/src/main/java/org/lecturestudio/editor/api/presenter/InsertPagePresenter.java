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

package org.lecturestudio.editor.api.presenter;

import javax.inject.Inject;

import java.io.IOException;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.RecordedDocument;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.InsertPageView;

public class InsertPagePresenter extends Presenter<InsertPageView> {

	private final RecordingFileService recordingService;

	private Document newDoc;

	private Document currentDoc;


	@Inject
	InsertPagePresenter(ApplicationContext context, InsertPageView view,
						RecordingFileService recordingService) {
		super(context, view);

		RecordedDocument recordedDocument = recordingService.getSelectedRecording().getRecordedDocument();

		try {
			// Cloning Document to have a working copy, all edits are done exclusively on this copy
			currentDoc = new Document(recordedDocument.toByteArray());
			currentDoc.selectPage(recordedDocument.getDocument().getCurrentPageNumber());
		}
		catch (IOException exc) {
			handleException(exc, "Replace page failed", "replace.page.error");
		}

		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		view.setPageCurrentDoc(currentDoc.getCurrentPage());
		view.setOnPageNumberNewDoc(this::selectPageNewDoc);
		view.setOnInsert(this::insert);
		view.setOnAbort(() -> {
			closeDocuments();
			close();
		});
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	/**
	 * Sets the {@code Document} of which to insert a selected page.
	 *
	 * @param doc The {@code Document} from which to select a page that should be inserted.
	 */
	public void setNewDocument(Document doc) {
		newDoc = doc;

		view.setPageNewDoc(doc.getCurrentPage());

//		if (newDoc.getPageCount() >= currentDoc.getCurrentPageNumber()) {
//			selectPageNewDoc(currentDoc.getCurrentPageNumber());
//		}

		view.setOnPreviousPageNewDoc(() -> {
			int currentPage = doc.getCurrentPageNumber();

			selectPageNewDoc(currentPage - 1);
		});
		view.setOnNextPageNewDoc(() -> {
			int currentPage = doc.getCurrentPageNumber();

			selectPageNewDoc(currentPage + 1);
		});

		view.setTotalPagesNewDocLabel(doc.getPageCount());
	}

	private void selectPageNewDoc(int pageNumber) {
		if (newDoc.selectPage(pageNumber)) {
			view.setPageNewDoc(newDoc.getCurrentPage());
		}
	}

	private void insert() {
		recordingService.insertPage(newDoc)
				.whenComplete((result, throwable) -> {
					if (throwable != null) {
						handleException(throwable, "Insert page failed",
								"insert.page.error");
					}
					// Do not close the loaded document here. It is required for undo/redo actions.
					close();
				});
	}

	private void closeDocuments() {
		newDoc.close();
	}

}
