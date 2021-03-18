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
import java.io.IOException;
import java.nio.file.Files;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.effect.DenoiseEffectRunner;
import org.lecturestudio.core.audio.effect.NoiseReductionParameters;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.sink.WavFileSink;
import org.lecturestudio.core.audio.source.RandomAccessAudioSource;
import org.lecturestudio.core.bus.event.ProgressEvent;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.edit.ReplaceAudioAction;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.NoiseReductionProgressView;

public class NoiseReductionProgressPresenter extends Presenter<NoiseReductionProgressView> {

	private final RecordingFileService recordingService;

	private NoiseReductionParameters params;

	private DenoiseEffectRunner effectRunner;


	@Inject
	NoiseReductionProgressPresenter(ApplicationContext context, NoiseReductionProgressView view,
			RecordingFileService recordingService) {
		super(context, view);

		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		view.setOnCancel(this::cancel);
		view.setOnClose(this::close);

		EditorContext editorContext = (EditorContext) context;
		Recording recording = recordingService.getSelectedRecording();
		RandomAccessAudioStream stream = recording.getRecordedAudio().getAudioStream().clone();
		RandomAccessAudioSource source = new RandomAccessAudioSource(stream);

		// Create audio effect output file.
		AudioSink sink;
		File effectOutput;

		try {
			effectOutput = Files.createTempFile(editorContext.getTempDirectory().toPath(), "effect", ".wav").toFile();

			if (effectOutput.exists() && !effectOutput.delete()) {
				throw new IOException("Delete file failed");
			}

			effectOutput.deleteOnExit();

			sink = new WavFileSink(effectOutput);
			sink.setAudioFormat(stream.getAudioFormat());
		}
		catch (IOException e) {
			handleException(e, "Create file for noise reduced audio stream failed",
					"noise.reduction.error");
			close();
			return;
		}

		// Set input stream parameters.
		if (params.getTimeInterval() != null) {
			stream.addExclusiveMillis(params.getTimeInterval());
		}

		effectRunner = new DenoiseEffectRunner(params, source, sink);
		effectRunner.addProgressListener(event -> {
			onNoiseReductionProgress(event, effectOutput);
		});
		effectRunner.addStateListener((oldState, newState) -> {
			onNoiseReductionState(newState);
		});

		try {
			effectRunner.start();
		}
		catch (ExecutableException e) {
			handleException(e, "Render noise reduction failed",
					"noise.reduction.error");
			close();
		}
	}

	@Override
	public void destroy() {
		cancel();
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setNoiseReductionParameters(NoiseReductionParameters params) {
		this.params = params;
	}

	private void cancel() {
		if (nonNull(effectRunner)) {
			try {
				effectRunner.destroy();
				effectRunner = null;

				view.setTitle(context.getDictionary().get("noise.reduction.canceled"));
				view.setCanceled();
			}
			catch (ExecutableException e) {
				handleException(e, "Cancel noise reduction failed", "noise.reduction.cancel.error");
			}
		}
	}

	private void onNoiseReductionState(ExecutableState state) {
		if (state == ExecutableState.Started) {
			setCloseable(false);
		}
		else if (state == ExecutableState.Stopped) {
			setCloseable(true);
		}
	}

	private void onNoiseReductionProgress(ProgressEvent event, File effectOutput) {
		if (event.running()) {
			view.setProgress(event.getProgress());
		}
		else if (event.finished()) {
			Recording recording = recordingService.getSelectedRecording();
			RecordedAudio audio = recording.getRecordedAudio();
			RandomAccessAudioStream newStream;

			try {
				newStream = new RandomAccessAudioStream(effectOutput);

				ReplaceAudioAction editAction = new ReplaceAudioAction(audio, newStream);
				editAction.execute();
				audio.addEditAction(editAction);

				recording.setRecordedAudio(audio);

				view.setTitle(context.getDictionary().get("noise.reduction.finished"));
				view.setProgress(1);
				view.setFinished();

				setCloseable(true);
			}
			catch (Exception e) {
				handleException(e, "Apply noise reduced audio stream failed",
						"noise.reduction.error");
				close();
			}
		}
	}
}
