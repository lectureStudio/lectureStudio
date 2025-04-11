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

import static java.util.Objects.nonNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.RecordSettingsView;

public class RecordSettingsPresenter extends Presenter<RecordSettingsView> {

	private final AudioConfiguration audioConfig;

	private final ViewContextFactory viewFactory;


	@Inject
	RecordSettingsPresenter(ApplicationContext context, RecordSettingsView view, ViewContextFactory viewFactory) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.audioConfig = context.getConfiguration().getAudioConfig();
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		view.setAutostartRecording(config.autostartRecordingProperty());
		view.setNotifyToRecord(config.notifyToRecordProperty());
		view.setConfirmStopRecording(config.confirmStopRecordingProperty());
		view.setMixAudioStreams(audioConfig.mixAudioStreamsProperty());
		view.setPageRecordingTimeout(config.pageRecordingTimeoutProperty());
		view.setRecordingAudioFormats(AudioUtils.getAudioFormats());
		view.setRecordingAudioFormat(audioConfig.recordingFormatProperty());
		view.setRecordingPath(audioConfig.recordingPathProperty());
		view.setOnSelectRecordingPath(() -> CompletableFuture.runAsync(this::selectRecordingPath));
		view.setOnReset(this::reset);
	}

	private void selectRecordingPath() {
		File initDirectory = new File(audioConfig.getRecordingPath());

		DirectoryChooserView dirChooser = viewFactory.createDirectoryChooserView();
		dirChooser.setInitialDirectory(initDirectory);

		File selectedFile = dirChooser.show(view);

		if (nonNull(selectedFile)) {
			audioConfig.setRecordingPath(selectedFile.getAbsolutePath());
		}
	}

	private void reset() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		config.setAutostartRecording(defaultConfig.getAutostartRecording());
		config.setNotifyToRecord(defaultConfig.getNotifyToRecord());
		config.setConfirmStopRecording(defaultConfig.getConfirmStopRecording());
		config.setPageRecordingTimeout(defaultConfig.getPageRecordingTimeout());
		audioConfig.setRecordingFormat(defaultConfig.getAudioConfig().getRecordingFormat());
		audioConfig.setRecordingPath(defaultConfig.getAudioConfig().getRecordingPath());
		audioConfig.setMixAudioStreams(defaultConfig.getAudioConfig().getMixAudioStreams());
	}
}