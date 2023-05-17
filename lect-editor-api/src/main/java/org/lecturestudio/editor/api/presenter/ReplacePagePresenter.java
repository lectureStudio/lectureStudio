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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.RecordedDocument;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.util.ReplacePageType;
import org.lecturestudio.editor.api.view.ReplacePageView;

public class ReplacePagePresenter extends Presenter<ReplacePageView> {

	private final RecordingFileService recordingService;

	private int currentDocCurrentPageNumber;

	private Document newDoc;

	private Document currentDoc;

	private ReplacePageType replacePageType = ReplacePageType.REPLACE_SINGLE_PAGE;


	@Inject
	ReplacePagePresenter(ApplicationContext context, ReplacePageView view,
	                     RecordingFileService recordingService) {
		super(context, view);

		RecordedDocument recordedDocument = recordingService.getSelectedRecording().getRecordedDocument();

		try {
			// Cloning Document to have a working copy, all edits are done exclusively on this copy
			currentDoc = new Document(recordedDocument.toByteArray());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		currentDocCurrentPageNumber = currentDoc.getCurrentPageNumber();

		view.setPageCurrentDoc(currentDoc.getCurrentPage());
		view.setTotalPagesCurrentDocLabel(currentDoc.getPageCount());

		view.setOnPreviousPageCurrentDoc(() -> selectPageCurrentDoc(currentDocCurrentPageNumber - 1));
		view.setOnNextPageCurrentDoc(() -> selectPageCurrentDoc(currentDocCurrentPageNumber + 1));
		view.setOnPageNumberCurrentDoc(this::selectPageCurrentDoc);
		view.setOnPageNumberNewDoc(this::selectPageNewDoc);
		view.setOnAbort(() -> {
			closeDocuments();
			close();
		});
		view.setOnReplace(this::replace);
		view.setOnConfirm(this::confirm);
		view.setOnReplaceTypeChange(this::replaceTypeChanged);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	/**
	 * Sets the replacing {@code Document}. Is called first, before any of the other methods.
	 *
	 * @param doc The {@code Document} from which the {@code Page}s are taken from to replace in the current {@code Document}
	 */
	public void setNewDocument(Document doc) {
		newDoc = doc;

		view.setPageNewDoc(doc.getCurrentPage());
		view.setOnPreviousPageNewDoc(() -> {
			int currentPage = doc.getCurrentPageNumber();

			selectPageNewDoc(currentPage - 1);
		});
		view.setOnNextPageNewDoc(() -> {
			int currentPage = doc.getCurrentPageNumber();

			selectPageNewDoc(currentPage + 1);
		});

		view.setDisableAllPagesTypeRadio(newDoc.getPageCount() != currentDoc.getPageCount());

		view.setTotalPagesNewDocLabel(doc.getPageCount());
	}

	/**
	 * Flips the {@code Page} in the replacing {@code Document}.
	 *
	 * @param pageNumber The page to be shown.
	 * @return {@code true} if the page flip was successful.
	 */
	private boolean selectPageNewDoc(int pageNumber) {
		if (newDoc.selectPage(pageNumber)) {
			view.setPageNewDoc(newDoc.getCurrentPage());
			return true;
		}
		return false;
	}

	/**
	 * Flips the {@code Page} in the current {@code Document}.
	 *
	 * @param pageNumber The page to be shown.
	 * @return {@code true} if the page flip was successful.
	 */
	private boolean selectPageCurrentDoc(int pageNumber) {

		if (currentDoc.selectPage(pageNumber)) {
			currentDocCurrentPageNumber = pageNumber;
			view.setPageCurrentDoc(currentDoc.getCurrentPage());
			return true;
		}
		return false;
	}

	/**
	 * Replaces the selected {@code Page} from the current Document with the selected Page from the replacing {@code Document}.
	 * Closes the view in case there is no {@code Page} left in the current Document.
	 * Opens a pop-up with an error message, in case an error occurs.
	 */
	private void confirm() {
		recordingService.replaceAllPages(currentDoc)
				.whenComplete((result, throwable) -> {
					if (throwable != null) {
						handleException(throwable, "Replace page failed",
								"replace.page.error");
					}
					closeDocuments();
					ReplacePagePresenter.this.close();
				});
	}

	private void replace() {
		view.disableInput();
		CompletableFuture.runAsync(() -> {
			if (replacePageType.equals(ReplacePageType.REPLACE_ALL_PAGES)) {
				currentDoc.replaceAllPages(newDoc);
			}
			else if (replacePageType.equals(ReplacePageType.REPLACE_SINGLE_PAGE)) {
				currentDoc.replacePage(currentDoc.getCurrentPage(), newDoc.getCurrentPage());
			}

			try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
				currentDoc.toOutputStream(stream);
				currentDoc.close();

				currentDoc = new Document(stream.toByteArray());
			}
			catch (IOException e) {
				throw new CompletionException(e);
			}

			if (replacePageType.equals(ReplacePageType.REPLACE_ALL_PAGES)) {
				// reloads the page in the viewer, in order to show the replaced page
				view.setPageCurrentDoc(currentDoc.getCurrentPage());
			}
			else if (replacePageType.equals(ReplacePageType.REPLACE_SINGLE_PAGE)) {
				showNextPages();
			}

		}).whenComplete((result, throwable) -> {
			if (throwable != null) {
				handleException(throwable, "Replace page failed", "replace.page.error");
			}
			view.enableInput();
		});
	}

	/**
	 * Shows the next {@code Page} for both the replacing {@code Document} and the current {@code Document}.
	 * In case the replacing {@code Document} shows the last {@code Page}, no {@code Page} switch is going to happen for this one.
	 * In case the current {@code Document} already shows the last {@code Page}, the no page flip occurs and returns {@code false}.
	 * Returns {@code true} in any other case.
	 */
	private void showNextPages() {
		int newDocCurrentPageNumber = newDoc.getCurrentPageNumber();

		if (selectPageCurrentDoc(currentDocCurrentPageNumber + 1)) {

			selectPageNewDoc(newDocCurrentPageNumber + 1);
		}
		else {
			selectPageCurrentDoc(currentDocCurrentPageNumber);
		}
	}

	private void replaceTypeChanged(String typeID) {
		replacePageType = ReplacePageType.parse(typeID);
	}

	private void closeDocuments() {
		newDoc.close();
	}
}
