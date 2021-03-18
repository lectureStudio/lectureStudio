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

package org.lecturestudio.editor.api.presenter;

import static java.util.Objects.nonNull;

import java.io.File;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.QuitSaveRecordingView;

public class QuitSaveRecordingPresenter extends Presenter<QuitSaveRecordingView> {

	private final ViewContextFactory viewFactory;

	private final RecordingFileService recordingService;


	@Inject
	QuitSaveRecordingPresenter(ApplicationContext context, QuitSaveRecordingView view,
			ViewContextFactory viewFactory,
			RecordingFileService recordingService) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		view.setTitle(context.getDictionary().get("quit.save.recording.question"));
		view.setOnDiscardRecording(this::close);
		view.setOnSaveRecording(this::saveRecording);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	private void saveRecording() {
		String title = recordingService.getSelectedRecording().getRecordedDocument().getDocument().getTitle();
		String fileName = title + "-edit";

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.addExtensionFilter("Lecture Recordings", "presenter");
		fileChooser.setInitialFileName(fileName);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		File selectedFile = fileChooser.showSaveFile(view);

		if (nonNull(selectedFile)) {
			setCloseable(false);

			view.setTitle(context.getDictionary().get("save.recording"));
			view.showProgress();
			view.setSavePath(selectedFile.getAbsolutePath());

			recordingService.saveRecording(selectedFile, view::setProgress)
				.thenRun(() -> {
					view.setTitle(context.getDictionary().get("save.recording.success"));
					view.setDone();

					setCloseable(true);
				})
				.exceptionally(throwable -> {
					handleException(throwable, "Save recording failed", "save.recording.error", selectedFile.getPath());

					view.setError(context.getDictionary().get("save.recording.error"));
					return null;
				});
		}
		else {
			close();
		}
	}
}