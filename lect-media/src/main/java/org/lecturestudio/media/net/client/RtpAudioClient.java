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

package org.lecturestudio.media.net.client;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioPlayerExt;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.Player;
import org.lecturestudio.core.audio.codec.AudioCodecProvider;
import org.lecturestudio.core.audio.codec.AudioDecoder;
import org.lecturestudio.core.audio.codec.AudioDecoderListener;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.audio.io.AudioPlaybackBuffer;
import org.lecturestudio.core.io.PlaybackData;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.core.net.rtp.RtpDepacketizer;
import org.lecturestudio.core.net.rtp.RtpPacket;
import org.lecturestudio.core.net.rtp.RtpReceiveBuffer;
import org.lecturestudio.core.net.rtp.RtpReceiveBufferNode;
import org.lecturestudio.media.avdev.AVdevAudioOutputDevice;
import org.lecturestudio.media.avdev.AvdevAudioPlayerExt;

/**
 * The {@code RtpAudioClient} receives the audio stream from a server. This
 * class represents a standalone audio client that is able to receive, decode
 * and play audio.
 * 
 * @author Alex Andres
 */
public class RtpAudioClient extends ExecutableBase implements MediaStreamClient<byte[]> {

	private static final Logger LOG = LogManager.getLogger(RtpAudioClient.class);
	
	/** The scheduled executor that flushes the receive buffer. */
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	/** The playback audio format. */
	private final AudioFormat audioFormat;

	/** The audio configuration. */
	private final AudioConfiguration audioConfig;

	/** The audio decoder. */
	private final AudioDecoder audioDecoder;

	/** The codec specific packet depacketizer. */
	private final RtpDepacketizer depacketizer;

	/** The audio player. */
	private Player audioPlayer;

	/** The receive buffer to re-order packets. */
	private RtpReceiveBuffer receiveBuffer;

	/** The buffer that holds decoded PCM audio. */
	private AudioPlaybackBuffer playbackBuffer;


	/**
	 * Creates a new {@link RtpAudioClient} with specified parameters.
	 * 
	 * @param audioConfig The audio configuration.
	 * @param provider The audio codec provider.
	 * @param audioFormat The audio format.
	 */
	public RtpAudioClient(AudioConfiguration audioConfig, AudioCodecProvider provider, AudioFormat audioFormat) {
		this.audioConfig = audioConfig;
		this.audioFormat = audioFormat;
		this.audioDecoder = provider.getAudioDecoder();
		this.depacketizer = provider.getRtpDepacketizer();
	}
	
	public void setVolume(float volume) {
		if (volume < 0 || volume > 1) {
			return;
		}
		
		audioPlayer.setVolume(volume);
	}
	
	@Override
	public void register(MediaStreamReceiver streamReceiver) throws Exception {
    	streamReceiver.addConnectorListener(MediaType.Audio, this);
	}
	
	@Override
	public void unregister(MediaStreamReceiver streamReceiver) throws Exception {
		streamReceiver.removeConnectorListener(MediaType.Audio);
	}
	
	@Override
	public void onConnectorRead(byte[] data) {
		if (!started()) {
			return;
		}
		
		RtpPacket rtpPacket = new RtpPacket(data, data.length);
		
		receiveBuffer.addNode(new RtpReceiveBufferNode(rtpPacket));
	}
	
	@Override
	protected void initInternal() throws ExecutableException {
		receiveBuffer = new RtpReceiveBuffer(1000);

		audioDecoder.setFormat(audioFormat);
		audioDecoder.addListener(new DecoderListener());
		audioDecoder.init();

		initAudioPlayer();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		audioPlayer.start();
		audioDecoder.start();
		
		executor.scheduleAtFixedRate(new FlushReceiveBufferTask(), 0, 5, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		audioPlayer.stop();
		audioDecoder.stop();
		
		executor.shutdown();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		audioPlayer.destroy();
		audioDecoder.destroy();
		
		playbackBuffer = null;
	}

	private void initAudioPlayer() throws ExecutableException {
		String providerName = audioConfig.getSoundSystem();
		String outputDeviceName = audioConfig.getOutputDeviceName();

		AudioOutputDevice outputDevice = AudioUtils.getAudioOutputDevice(providerName, outputDeviceName);

		playbackBuffer = new AudioPlaybackBuffer();
		playbackBuffer.setAudioFormat(audioFormat);
		
		try {
			if (providerName.equals("AVdev")) {
				audioPlayer = new AvdevAudioPlayerExt((AVdevAudioOutputDevice) outputDevice, playbackBuffer);
			}
			else {
				audioPlayer = new AudioPlayerExt(outputDevice, playbackBuffer);
			}
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}



	/**
	 * This class flushes the receive buffer, calls the depacketizer and then
	 * calls the audio decoder.
	 */
	private class FlushReceiveBufferTask implements Runnable {

		public void run() {
			List<RtpReceiveBufferNode> nodes = receiveBuffer.flush();

			if (nodes == null)
				return;

			byte[] payload;

			try {
				for (RtpReceiveBufferNode node : nodes) {
					RtpPacket packet = node.getPacket();
					payload = depacketizer.processPacket(packet);
					
					audioDecoder.process(payload, payload.length, packet.getTimestamp());
				}
			}
			catch (Exception e) {
				LOG.error(e);
			}
		}

	}

	/**
	 * This class is used to detect decoded audio and write it to the audio
	 * playback buffer.
	 */
	private class DecoderListener implements AudioDecoderListener {

		@Override
		public void audioDecoded(byte[] data, int length, long timestamp) {
			playbackBuffer.put(new PlaybackData<>(data, timestamp));
		}

	}

}
