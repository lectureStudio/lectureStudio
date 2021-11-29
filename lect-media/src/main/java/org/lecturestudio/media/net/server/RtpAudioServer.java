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

package org.lecturestudio.media.net.server;

import com.google.common.eventbus.Subscribe;

import java.util.List;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.AudioRecorder;
import org.lecturestudio.core.audio.RingBuffer;
import org.lecturestudio.core.audio.bus.AudioBus;
import org.lecturestudio.core.audio.bus.event.AudioVolumeEvent;
import org.lecturestudio.core.audio.codec.AudioCodecLoader;
import org.lecturestudio.core.audio.codec.AudioCodecProvider;
import org.lecturestudio.core.audio.codec.AudioEncoder;
import org.lecturestudio.core.audio.codec.AudioEncoderListener;
import org.lecturestudio.core.net.Sync;
import org.lecturestudio.core.net.rtp.RtpPacket;
import org.lecturestudio.core.net.rtp.RtpPacketizer;
import org.lecturestudio.media.webrtc.WebRtcAudioRecorder;
import org.lecturestudio.media.config.AudioStreamConfig;
import org.lecturestudio.web.api.connector.client.ClientConnector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@code RtpAudioServer} distributes the audio stream to a group of
 * participants. This class represents a standalone audio server that is able to
 * record, encode and transmit audio.
 * 
 * @author Alex Andres
 */
public class RtpAudioServer extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(RtpAudioServer.class);

	private final ClientConnector connector;

	/** The buffer for recorded audio. */
	private RingBuffer ringBuffer;

	/** The audio encoder. */
	private AudioEncoder encoder;

	/** The audio reader reads recorded audio from buffer and calls the encoder. */
	private AudioReader audioReader;
	
	private AudioRecorder audioRecorder;

	private final AudioStreamConfig audioStreamConfig;


	/**
	 * Creates a new {@link RtpAudioServer} with provided parameters.
	 * 
	 * @param audioStreamConfig The audio stream configuration.
	 * @param connector The connector for data transmission.
	 */
	public RtpAudioServer(AudioStreamConfig audioStreamConfig, ClientConnector connector) {
		this.audioStreamConfig = audioStreamConfig;
		this.connector = connector;
	}
	
	@Subscribe
	public void onEvent(final AudioVolumeEvent event) {
		if (audioRecorder != null) {
			audioRecorder.setAudioVolume(event.getVolume());
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		connector.addStateListener((oldState, newState) -> {
			if (started() && newState == ExecutableState.Error) {
				try {
					setState(ExecutableState.Error);

					stop();
					destroy();
				}
				catch (ExecutableException e) {
					LOG.error("Handle connector error failed.", e);
				}
			}
		});
		connector.init();

		AudioCodecProvider codecProvider = AudioCodecLoader.getInstance().getProvider(audioStreamConfig.codec);
		AudioFormat audioFormat = audioStreamConfig.format;

		ringBuffer = new RingBuffer(1024 * 1024);

		audioRecorder = new WebRtcAudioRecorder();
		audioRecorder.setAudioDeviceName(audioStreamConfig.captureDeviceName);
		audioRecorder.setAudioVolume(1);
		audioRecorder.setAudioSink(ringBuffer);

		encoder = codecProvider.getAudioEncoder();
		encoder.addListener(new EncoderListener(codecProvider.getRtpPacketizer()));
		encoder.setFormat(audioFormat);
		encoder.init();

		audioReader = new AudioReader(audioFormat);
		audioReader.init();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		connector.start();
		encoder.start();
		audioReader.start();
		audioRecorder.start();
		
		AudioBus.register(this);
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		AudioBus.unregister(this);

		audioReader.stop();
		audioRecorder.stop();
		encoder.stop();
		connector.stop();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		audioReader.destroy();
		encoder.destroy();
		connector.destroy();
	}

    
    
	/**
	 * The listener used to packetize audio and transmit it to the participants.
	 */
	private class EncoderListener implements AudioEncoderListener {

		/** The RTP packetizer of the used audio codec. */
		private RtpPacketizer rtpPacketizer;


		EncoderListener(RtpPacketizer packetizer) {
			this.rtpPacketizer = packetizer;
		}

		@Override
		public void audioEncoded(byte[] data, int length, long timestamp) {
			List<RtpPacket> packets = rtpPacketizer.processPacket(data, length, timestamp);

			try {
				for (RtpPacket packet : packets) {
					connector.send(packet.toByteArray());
				}
			}
			catch (Exception e) {
				LOG.error("Transmit audio packet failed.", e);
			}
		}

	}

	/**
	 * The audio reader reads recorded audio from buffer and calls the encoder.
	 */
	private class AudioReader extends ExecutableBase {

		private final AudioFormat audioFormat;

		private Thread thread;

		private volatile boolean run = true;
		

		AudioReader(AudioFormat format) {
			this.audioFormat = format;
		}

		@Override
		protected void initInternal() throws ExecutableException {
			thread = new Thread(this::run);
			thread.setName("RTP-Audio-Reader");
		}

		@Override
		protected void startInternal() throws ExecutableException {
			thread.start();
		}

		@Override
		protected void stopInternal() throws ExecutableException {
			run = false;
		}

		@Override
		protected void destroyInternal() throws ExecutableException {

		}

		private void run() {
			Sync.setStreamStart(System.currentTimeMillis());

			// Create 20 milliseconds audio buffer.
			int bufferSize = (20 * AudioUtils.getBytesPerSecond(audioFormat)) / 1000;
			byte[] buffer = new byte[bufferSize];

			try {
				while (run) {
					int bytes = ringBuffer.read(buffer, 0, bufferSize);

					if (bytes > 0) {
						encoder.process(buffer, bytes, Sync.getTimestamp());
					}

					Thread.sleep(1);
				}
			}
			catch (Exception e) {
				LOG.error("Encode audio samples failed.", e);
			}
		}
	}
	
}
