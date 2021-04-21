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

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.ReplacePageView;

public class ReplacePagePresenter extends Presenter<ReplacePageView> {

	private final RecordingFileService recordingService;

	private Document newDoc;


	@Inject
	ReplacePagePresenter(ApplicationContext context, ReplacePageView view,
			RecordingFileService recordingService) {
		super(context, view);

		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		Document doc = recordingService.getSelectedRecording()
				.getRecordedDocument().getDocument();

		view.setCurrentPage(doc.getCurrentPage());
		view.setOnPageNumber(this::selectPage);
		view.setOnCancel(this::close);
		view.setOnReplace(this::replace);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setNewDocument(Document doc) {
		newDoc = doc;

		view.setNewPage(doc.getCurrentPage());
		view.setOnPreviousPage(() -> {
			int currentPage = doc.getCurrentPageNumber();

			selectPage(currentPage - 1);
		});
		view.setOnNextPage(() -> {
			int currentPage = doc.getCurrentPageNumber();

			selectPage(currentPage + 1);
		});
	}

	private void selectPage(int pageNumber) {
		if (newDoc.selectPage(pageNumber)) {
			view.setNewPage(newDoc.getCurrentPage());
		}
	}

	private void replace() {
		recordingService.replacePage(newDoc)
				.thenRun(this::close)
				.exceptionally(throwable -> {
					handleException(throwable, "Replace page failed",
							"replace.page.error");
					return null;
				});
	}
}
