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

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.ExecutableStateListener;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.source.AudioSource;

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



	private static class DummyPlayer implements AudioPlayer {

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
		public void setAudioSource(AudioSource source) {

		}

		@Override
		public void setAudioVolume(double volume) {

		}

		@Override
		public void setAudioProgressListener(AudioPlaybackProgressListener listener) {

		}

		@Override
		public void seek(int timeMs) {

		}

		@Override
		public void addStateListener(ExecutableStateListener listener) {

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
