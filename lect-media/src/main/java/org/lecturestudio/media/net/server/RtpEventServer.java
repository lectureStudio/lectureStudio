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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.net.Sync;
import org.lecturestudio.core.net.rtp.RtpPacket;
import org.lecturestudio.core.recording.LectureRecorderListener;
import org.lecturestudio.core.recording.action.DocumentAction;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.media.net.StreamEventRecorder;
import org.lecturestudio.web.api.connector.client.ClientConnector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@code RtpEventServer} distributes the event stream to a group of
 * participants.
 * 
 * @author Alex Andres
 */
public class RtpEventServer extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(RtpEventServer.class);
	
	private final ClientConnector connector;

	private final MediaStreamProvider streamProvider;

	/** The event recorder that records network related events. */
    private StreamEventRecorder eventRecorder;

    /** The RTP packet which should be sent. */
    private RtpPacket rtpPacket;
    
    /** The document file to checksum mapping of documents which were transmitted. */
    private Map<String, String> transmittedDocuments;
    

	/**
	 * Creates a new {@link RtpEventServer} with specified parameters.
	 * 
	 * @param connector The connector for data transmission.
	 */
	public RtpEventServer(ClientConnector connector, MediaStreamProvider streamProvider) {
		this.connector = connector;
		this.streamProvider = streamProvider;
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

		eventRecorder = new StreamEventRecorder();
		eventRecorder.setListener(new EventRecorderListener());
		
		transmittedDocuments = new HashMap<>();
		
		initRtp();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		connector.start();
		eventRecorder.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		connector.stop();
		eventRecorder.stop();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		connector.destroy();
		eventRecorder.destroy();

		transmittedDocuments.clear();
	}

	/**
	 * Initializes the RTP packet.
	 */
	private void initRtp() {
		Random rand = new Random();

		/* Initialize RTP packet header. */
		rtpPacket = new RtpPacket();
		rtpPacket.setVersion(1);
		rtpPacket.setPadding(0);
		rtpPacket.setExtension(0);

		rtpPacket.setMarker(0);
		rtpPacket.setPayloadType(99); // dynamic

		rtpPacket.setSeqNumber(rand.nextInt());
		rtpPacket.setTimestamp(rand.nextInt());
		rtpPacket.setSsrc(rand.nextInt());
	}
	
	/**
	 * Increases the RTP packets sequence number by one.
	 */
	private void updatePacket() {
		rtpPacket.setSeqNumber(rtpPacket.getSeqNumber() + 1);
		rtpPacket.setTimestamp(Sync.getTimestamp());
	}

	/**
	 * Sends an event data packet to the broadcaster.
	 * 
	 * @param packet The event data packet to send.
	 */
	private void transmitPacket(RtpPacket packet) {
		try {
			connector.send(packet.toByteArray());
		}
		catch (Exception e) {
			LOG.error("Transmit packet failed.", e);
		}
	}
	
	private void transmitDocument(DocumentAction action) throws Exception {
		if (action.getDocumentType() == DocumentType.WHITEBOARD) {
			// Whiteboards are created dynamically. No need to transmit.
			return;
		}

		String filePath = action.getDocumentFileName();
		String checksum = action.getDocumentChecksum();
		String foundChecksum = transmittedDocuments.get(filePath);

		if (foundChecksum != null && foundChecksum.equals(checksum)) {
			// Document already transmitted, abort.
			return;
		}

		streamProvider.addDocument(filePath);
		
		transmittedDocuments.put(filePath, checksum);
	}



	/**
	 * A listener that is used to transmit recorded events.
	 */
	private class EventRecorderListener implements LectureRecorderListener {

		@Override
		public void eventRecorded(PlaybackAction action) {
            try {
            	switch (action.getType()) {
    				case DOCUMENT_CREATED:
					case DOCUMENT_SELECTED:
    					transmitDocument((DocumentAction) action);
    					break;
    				
    				default:
    					break;
    			}
            	
                rtpPacket.setPayload(action.toByteArray());
            }
            catch (Exception e) {
            	LOG.error("Set RTP payload failed.", e);
            	return;
            }
            
			updatePacket();
			transmitPacket(rtpPacket);
		}

	}

}
