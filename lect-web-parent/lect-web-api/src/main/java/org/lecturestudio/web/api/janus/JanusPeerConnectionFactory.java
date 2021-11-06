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

package org.lecturestudio.web.api.janus;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.media.audio.AudioDevice;
import dev.onvoid.webrtc.media.audio.AudioDeviceModule;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lecturestudio.web.api.stream.StreamAudioContext;
import org.lecturestudio.web.api.stream.StreamContext;

public class JanusPeerConnectionFactory {

	private final static Logger LOGGER = System.getLogger(JanusPeerConnectionFactory.class.getName());

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final StreamContext context;

	private final PeerConnectionFactory factory;

	private final AudioDeviceModule audioModule;


	public JanusPeerConnectionFactory(StreamContext context) {
		this.context = context;

		StreamAudioContext audioContext = context.getAudioContext();
		AudioDevice recordingDevice = audioContext.getRecordingDevice();
		AudioDevice playbackDevice = audioContext.getPlaybackDevice();

		audioModule = executeAndGet(AudioDeviceModule::new);

		executeAndWait(() -> {
			setPlaybackDevice(playbackDevice, false);
			setRecordingDevice(recordingDevice, false);
		});

		factory = executeAndGet(() -> {
			return new PeerConnectionFactory(audioModule);
		});

		audioContext.playbackDeviceProperty().addListener((observable, oldValue, newValue) -> {
			executeAndWait(() -> {
				setPlaybackDevice(newValue, true);
			});
		});
		audioContext.recordingDeviceProperty().addListener((observable, oldValue, newValue) -> {
			executeAndWait(() -> {
				setRecordingDevice(newValue, true);
			});
		});
	}

	public StreamContext getStreamContext() {
		return context;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public PeerConnectionFactory getFactory() {
		return factory;
	}

	public RTCPeerConnection createPeerConnection(PeerConnectionObserver observer) {
		return executeAndGet(() -> {
			return factory.createPeerConnection(context.getRTCConfig(), observer);
		});
	}

	public void dispose() {
		executeAndWait(() -> {
			if (nonNull(audioModule)) {
				audioModule.dispose();
			}
			if (nonNull(factory)) {
				factory.dispose();
			}
		});
	}

	private void setPlaybackDevice(AudioDevice device, boolean start) {
		if (isNull(device) || isNull(audioModule)) {
			return;
		}

		LOGGER.log(Level.INFO, "Audio playback device: " + device);

		audioModule.stopPlayout();
		audioModule.setPlayoutDevice(device);
		audioModule.initPlayout();

		if (start) {
			audioModule.startPlayout();
		}
	}

	private void setRecordingDevice(AudioDevice device, boolean start) {
		if (isNull(device) || isNull(audioModule)) {
			return;
		}

		LOGGER.log(Level.INFO, "Audio recording device: " + device);

		audioModule.stopRecording();
		audioModule.setRecordingDevice(device);
		audioModule.initRecording();

		if (start) {
			audioModule.startRecording();
		}
	}

	private <T> T executeAndGet(Callable<T> callable) {
		try {
			return executor.submit(callable).get();
		}
		catch (Exception e) {
			LOGGER.log(Level.ERROR, "Execute task failed", e);
		}

		return null;
	}

	private void executeAndWait(Runnable runnable) {
		try {
			executor.submit(runnable).get();
		}
		catch (Exception e) {
			LOGGER.log(Level.ERROR, "Execute task failed", e);
		}
	}
}
