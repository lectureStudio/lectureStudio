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

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.camera.bus.event.CameraClientEvent;
import org.lecturestudio.core.camera.bus.event.CameraImageEvent;
import org.lecturestudio.core.codec.VideoDecoderCallback;
import org.lecturestudio.core.codec.h264.H264StreamDecoder;
import org.lecturestudio.core.codec.h264.RtpH264Depacketizer;
import org.lecturestudio.core.io.PlaybackBuffer;
import org.lecturestudio.core.io.PlaybackData;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.core.net.Synchronizer;
import org.lecturestudio.core.net.rtp.RtpPacket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CameraClient extends ExecutableBase implements MediaStreamClient<byte[]> {

	private static final Logger LOG = LogManager.getLogger(CameraClient.class);
	
	private final PlaybackBuffer<byte[]> playbackBuffer;
	
	private final RtpH264Depacketizer depacketizer;
	
	private PlaybackHandler playbackHandler;
	

	public CameraClient() {
		playbackBuffer = new PlaybackBuffer<>();
		depacketizer = new RtpH264Depacketizer();
	}
	
	@Override
	public void register(MediaStreamReceiver streamReceiver) throws Exception {
    	streamReceiver.addConnectorListener(MediaType.Camera, this);
	}
	
	@Override
	public void unregister(MediaStreamReceiver streamReceiver) throws Exception {
		streamReceiver.removeConnectorListener(MediaType.Camera);
	}
	
    @Override
	public void onConnectorRead(byte[] data) {
    	if (!started()) {
			return;
		}
    	
		RtpPacket rtpPacket = new RtpPacket(data, data.length);

		// Accept only video frames that are in time.
		if (Synchronizer.getAudioTime() > rtpPacket.getTimestamp()) {
			LOG.debug("Frame dropped: {} ms too late.", (Synchronizer.getAudioTime() - rtpPacket.getTimestamp()));
			return;
		}

		try {
			byte[] payload = depacketizer.processPacket(rtpPacket);
			if (payload != null) {
				playbackBuffer.put(new PlaybackData<byte[]>(payload, rtpPacket.getTimestamp()));
			}
		}
		catch (Exception e) {
			LOG.error("Decode camera video packet failed", e);
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		playbackHandler = new PlaybackHandler();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		playbackHandler.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		playbackHandler.shutdown();
		playbackBuffer.reset();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		
	}

	@Override
	protected void fireStateChanged() {
		ApplicationBus.post(new CameraClientEvent(getState()));
	}
	
	
	private class PlaybackHandler extends Thread implements VideoDecoderCallback {

		private volatile boolean run = true;
		
		private final H264StreamDecoder decoder;
		
		private final Map<String, Number> stats;
		
		private long lastFrame = 0;
		
		
		PlaybackHandler() {
			decoder = new H264StreamDecoder(this);
			stats = new HashMap<>();
		}
		
		@Override
		public void run() {
			try {
				while (run) {
					PlaybackData<byte[]> payload = playbackBuffer.peek();
					if (payload != null) {
						if (Synchronizer.getAudioTime() >= payload.getTimestamp()) {
							// Decode it.
							decoder.decode(ByteBuffer.wrap(payload.getData()));

							// Remove it.
							playbackBuffer.take();
						}
					}

					Thread.sleep(1);
				}
				
				decoder.dispose();
			}
			catch (Exception e) {
				LOG.error("Decode camera video frame failed", e);
			}
		}
		
		@Override
		public void frameDecoded(BufferedImage image) {
			stats.put("fps", decoder.getFPS());
			stats.put("kbps", decoder.getBitrate());
			stats.put("total", decoder.getTotalBytesReceived());
			stats.put("latency", System.currentTimeMillis() - lastFrame);

			lastFrame = System.currentTimeMillis();
			
			ApplicationBus.post(new CameraImageEvent(image, stats));
		}

		public void shutdown() {
			run = false;
		}
		
	}

}
