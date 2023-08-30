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

package org.lecturestudio.core.audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.ExecutableStateListener;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.source.AudioSource;
import org.lecturestudio.core.model.Time;

public class DummyAudioSystemProvider implements AudioSystemProvider {

	@Override
	public AudioDevice getDefaultRecordingDevice() {
		return new AudioDevice("dummy");
	}

	@Override
	public AudioDevice getDefaultPlaybackDevice() {
		return new AudioDevice("dummy");
	}

	@Override
	public AudioDevice[] getRecordingDevices() {
		return new AudioDevice[] { new AudioDevice("dummy") };
	}

	@Override
	public AudioDevice[] getPlaybackDevices() {
		return new AudioDevice[] { new AudioDevice("dummy") };
	}

	@Override
	public AudioPlayer createAudioPlayer() {
		return new DummyPlayer();
	}

	@Override
	public AudioRecorder createAudioRecorder() {
		return new DummyRecorder();
	}

	@Override
	public String getProviderName() {
		return "dummy";
	}


	/**
	 * Player that enables starting and stopping the recording.
	 * Some functionalities depend on a running timer and can only be tested if the actual player is also running.
	 */
	private static class DummyPlayer implements AudioPlayer {

		private long progress;
		private long previousProgress;
		private long startTime;
		ScheduledExecutorService executor;
		private AudioPlaybackProgressListener listener;
		private AudioSource source;
		private long inputSize;
		private Time duration;
		private List<ExecutableStateListener> stateListeners = new ArrayList<>();

		private void resetDuration() {
			this.progress = 0L;
			this.previousProgress = 0L;
		}

		private void startTimer() {
			this.startTime = System.currentTimeMillis();
		}

		private void pauseTimer() {
			progress = getProgress();
			this.previousProgress = progress;
		}

		private long getProgress() {
			return System.currentTimeMillis() - startTime + previousProgress;
		}

		@Override
		public void init() {
			resetDuration();
			executor = Executors.newScheduledThreadPool(1);

			AudioFormat format = source.getAudioFormat();
			float bytesPerMs = AudioUtils.getBytesPerSecond(format) / 1000f;

			duration = new Time((long) (inputSize / bytesPerMs));
		}

		@Override
		public void start() {
			startTimer();
			executor.scheduleAtFixedRate(() -> {
				if (getProgress() < duration.getMillis()) {
					listener.onAudioProgress(new Time(getProgress()), duration);
				}
				else {
					stop();
				}
			}, 0, 10, TimeUnit.MILLISECONDS);
		}

		@Override
		public void stop() {
			pauseTimer();
			resetDuration();
			fireStop();
		}

		@Override
		public void suspend() {
			pauseTimer();
			executor.shutdown();
		}

		@Override
		public void destroy() {

		}

		@Override
		public ExecutableState getState() {
			return ExecutableState.Created;
		}

		protected void fireStop() {
			for (ExecutableStateListener listener : stateListeners) {
				listener.onExecutableStateChange(ExecutableState.Stopping, ExecutableState.Stopped);
			}
		}

		@Override
		public void setAudioDeviceName(String deviceName) {

		}

		@Override
		public void setAudioSource(AudioSource source) {
			this.source = source;

			try {
				this.inputSize = source.getInputSize();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setAudioVolume(double volume) {

		}

		@Override
		public void setAudioProgressListener(AudioPlaybackProgressListener listener) {
			this.listener = listener;
		}

		@Override
		public void seek(int timeMs) {
			resetDuration();
			previousProgress = timeMs;
		}

		@Override
		public void addStateListener(ExecutableStateListener listener) {
			stateListeners.add(listener);
		}

		@Override
		public void removeStateListener(ExecutableStateListener listener) {

		}
	}



	private static class DummyRecorder implements AudioRecorder {

		@Override
		public void init() {

		}

		@Override
		public void start() throws ExecutableException {

		}

		@Override
		public void stop() {

		}

		@Override
		public void suspend() {

		}

		@Override
		public void destroy() {

		}

		@Override
		public ExecutableState getState() {
			return ExecutableState.Created;
		}

		@Override
		public void setAudioDeviceName(String deviceName) {

		}

		@Override
		public void setAudioSink(AudioSink sink) {

		}

		@Override
		public void setAudioVolume(double volume) {

		}

		@Override
		public void setAudioProcessingSettings(AudioProcessingSettings settings) {

		}

		@Override
		public void addStateListener(ExecutableStateListener listener) {

		}

		@Override
		public void removeStateListener(ExecutableStateListener listener) {

		}

		@Override
		public AudioProcessingStats getAudioProcessingStats() {
			return null;
		}
	}
}
