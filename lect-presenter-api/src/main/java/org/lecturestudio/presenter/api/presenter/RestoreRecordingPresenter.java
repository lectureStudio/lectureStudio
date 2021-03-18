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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.recording.RecordingBackup;
import org.lecturestudio.presenter.api.view.RestoreRecordingView;

public class RestoreRecordingPresenter extends Presenter<RestoreRecordingView> {

	private final ViewContextFactory viewFactory;

	/** The action that is called when the user has decided to discardRecording the recording. */
	private Action discardRecordingAction;

	/** The action that is called when the recording has been saved. */
	private Action recordingSavedAction;

	private boolean warned = false;


	@Inject
	RestoreRecordingPresenter(ApplicationContext context, RestoreRecordingView view, ViewContextFactory viewFactory) {
		super(context, view);

		this.viewFactory = viewFactory;
	}

	@Override
	public void initialize() {
		setCloseable(false);

		setOnDiscardRecording(this::close);
		setOnRecordingSaved(this::close);

		view.setOnDiscard(this::discardRecording);
		view.setOnSave(this::saveRecording);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setOnDiscardRecording(Action action) {
		discardRecordingAction = Action.concatenate(discardRecordingAction, action);
	}

	public void setOnRecordingSaved(Action action) {
		recordingSavedAction = Action.concatenate(recordingSavedAction, action);
	}

	private void discardRecording() {
		if (warned) {
			PresenterContext presenterContext = (PresenterContext) context;
			RecordingBackup backup;

			try {
				backup = new RecordingBackup(presenterContext.getRecordingDirectory());
				backup.clean();

				if (nonNull(discardRecordingAction)) {
					setCloseable(true);
					discardRecordingAction.execute();
				}
			}
			catch (IOException e) {
				handleException(e, "Discard recording backup failed", "recording.discard.error");
			}
		}
		else {
			warned = true;

			view.showWarning();
		}
	}

	private void saveRecording() {
		PresenterContext presenterContext = (PresenterContext) context;
		RecordingBackup backup;

		try {
			backup = new RecordingBackup(presenterContext.getRecordingDirectory());
		}
		catch (IOException e) {
			logException(e, "Open recording backup failed");
			view.setError(context.getDictionary().get("recording.restore.missing.backup"));
			setCloseable(true);
			return;
		}

		String recordingName = backup.getCheckpoint();

		if (isNull(recordingName)) {
			view.setError(context.getDictionary().get("recording.restore.missing.backup"));
			setCloseable(true);
			return;
		}

		File initFile = new File(recordingName + ".presenter");

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.addExtensionFilter("Presenter Recordings", "presenter");
		fileChooser.setInitialFileName(initFile.getName());
		fileChooser.setInitialDirectory(initFile.getParentFile());

		File selectedFile = fileChooser.showSaveFile(view);

		if (nonNull(selectedFile)) {
			File recFile = FileUtils.ensureExtension(selectedFile, ".presenter");

			view.setSavePath(recFile.getPath());
			view.showProgress();

			CompletableFuture.runAsync(() -> {
				try {
					backup.writeCheckpoint(recFile.getAbsolutePath(), view::setProgress);
				}
				catch (Exception e) {
					throw new CompletionException(e);
				}
			})
			.thenRun(backup::clean)
			.thenRun(view::setSuccess)
			.thenRun(() -> {
				if (nonNull(recordingSavedAction)) {
					setCloseable(true);
					recordingSavedAction.execute();
				}
			})
			.exceptionally(throwable -> {
				logException(throwable, "Save recording backup failed");
				view.setError(context.getDictionary().get("recording.save.error"));
				setCloseable(true);
				return null;
			});
		}
	}
}