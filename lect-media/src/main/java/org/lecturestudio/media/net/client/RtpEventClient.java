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

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.core.net.rtp.RtpClientProfile;
import org.lecturestudio.core.net.rtp.RtpDecodeException;
import org.lecturestudio.core.net.rtp.RtpEventDecoder;
import org.lecturestudio.core.net.rtp.RtpPacket;
import org.lecturestudio.core.net.rtp.RtpReceiveBuffer;
import org.lecturestudio.core.net.rtp.RtpReceiveBufferNode;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.media.playback.StreamEventExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@code RtpEventClient} receives the event stream from a server. Once a
 * packet is decoded by the {@link RtpEventDecoder} it is executed by the
 * {@link StreamEventExecutor}.
 * 
 * @author Alex Andres
 */
public class RtpEventClient extends ExecutableBase implements MediaStreamClient<byte[]> {

	private static final Logger LOG = LogManager.getLogger(RtpEventClient.class);
	
    /** The scheduled executor that flushes the receive buffer. */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /** The event decoder. */
    private final RtpEventDecoder eventDecoder;

    /** The event executor. */
    private final StreamEventExecutor executor;

    /** The receive buffer to re-order packets. */
    private final RtpReceiveBuffer receiveBuffer;

    /** The clients profile. */
    private final RtpClientProfile profile;


    /**
     * Creates a new {@link RtpEventClient} with specified parameters.
     *
     * @param executor The event executor.
     */
    public RtpEventClient(StreamEventExecutor executor) {
		this.executor = executor;
		this.eventDecoder = new RtpEventDecoder();
		this.profile = new RtpClientProfile();
		this.receiveBuffer = new RtpReceiveBuffer(profile.getMaxReceiveBufferSize());
	}
    
	@Override
	public void register(MediaStreamReceiver streamReceiver) throws Exception {
		streamReceiver.addConnectorListener(MediaType.Event, this);
	}

	@Override
	public void unregister(MediaStreamReceiver streamReceiver) throws Exception {
		streamReceiver.removeConnectorListener(MediaType.Event);
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
		executor.init();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		executor.start();
		
		int flushTimeout = profile.getReceiveBufferTimeout();
		scheduler.scheduleAtFixedRate(new FlushReceiveBufferTask(), 100, flushTimeout, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		executor.stop();
		scheduler.shutdown();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		executor.destroy();
	}

	

	/**
	 * This class flushes the receive buffer, calls the event decoder and then
	 * calls the event executor.
	 */
	private class FlushReceiveBufferTask implements Runnable {

		public void run() {
			List<RtpReceiveBufferNode> nodes = receiveBuffer.flush();
			
			if (nodes == null) {
				return;
			}

			for (RtpReceiveBufferNode node : nodes) {
				RtpPacket packet = node.getPacket();
				PlaybackAction action = null;
				
				try {
					action = eventDecoder.decodeRtpPacket(packet);
				}
				catch (RtpDecodeException e) {
					LOG.error("Decode RTP packet failed.", e);
				}

				if (action == null) {
					break;
				}

				executor.addAction(action);
			}
		}

	}

}
