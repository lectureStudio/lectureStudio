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

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.web.api.connector.ConnectorFactory;
import org.lecturestudio.web.api.connector.client.ClientConnector;
import org.lecturestudio.web.api.connector.client.ClientTcpConnectorHandler;
import org.lecturestudio.web.api.connector.client.ConnectorListener;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomDocument;
import org.lecturestudio.web.api.model.ClassroomService;
import org.lecturestudio.web.api.model.StreamDescription;
import org.lecturestudio.web.api.model.StreamService;
import org.lecturestudio.web.api.ws.ClassroomServiceClient;
import org.lecturestudio.web.api.ws.StreamServiceClient;

public class MediaStreamReceiver extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(MediaStreamReceiver.class);
	
	/** The associated Connectors with this Receiver. */
	private final Map<MediaType, ClientConnector> connectors = new ConcurrentHashMap<>();
	
	/** The ConnectorListeners for ClientConnectors which were not started. */
	private final Map<MediaType, ConnectorListener<byte[]>> listeners = new ConcurrentHashMap<>();

	private final ClassroomServiceClient classroomClient;

	private final StreamServiceClient streamServiceClient;
	
	private Classroom classroom;
	

	public MediaStreamReceiver(ClassroomServiceClient classroomClient, StreamServiceClient webService) {
		this.classroomClient = classroomClient;
		this.streamServiceClient = webService;
	}
	
	public List<Classroom> getClassrooms() throws Exception {
		return classroomClient.getClassrooms();
	}
	
	public void joinClassroom(Classroom classroom) throws Exception {
		for (ClassroomService service : classroom.getServices()) {
			if (service instanceof StreamService) {
				for (StreamDescription sd : service.getStreamDescriptions()) {
					ConnectorListener<byte[]> listener = listeners.remove(sd.getMediaType());

					// Create connectors only if a valid listener is available.
					if (listener == null) {
						continue;
					}

					ClientConnector connector = ConnectorFactory.createClientConnector(sd);
					connector.addChannelHandler(new ClientTcpConnectorHandler<>(listener));

					addConnector(sd.getMediaType(), connector);
				}
			}
		}

		// Start the connectors.
		start();
		
		this.classroom = classroom;
	}
	
	public void getDocument(ClassroomDocument doc, OutputStream stream) throws Exception {
		if (getState() != ExecutableState.Started) {
			throw new Exception("No classroom joined.");
		}
		
		streamServiceClient.getDocument(classroom.getShortName(), doc.getFileName(), stream);
	}
	
	public void addConnectorListener(MediaType type, ConnectorListener<byte[]> listener) throws Exception {
		if (started()) {
			// Hot-plug a new connector.
			ClientConnector connector = getConnector(type);
			
			if (connector == null) {
				Optional<StreamDescription> streamDesc = classroom.getServices()
						.stream()
						.filter(service -> (service instanceof StreamService))
						.flatMap(service -> service.getStreamDescriptions().stream())
						.filter(desc -> desc.getMediaType() == type)
						.findFirst();

				if (!streamDesc.isPresent()) {
					throw new ExecutableException("Media stream is not supported by this classroom.");
				}

				// Create a new connector and start it.
				connector = ConnectorFactory.createClientConnector(streamDesc.get());
				connector.addChannelHandler(new ClientTcpConnectorHandler<>(listener));

				addConnector(streamDesc.get().getMediaType(), connector);
			}
		}
		else {
			// Remember this listener and set it when the corresponding Connector is created.
			listeners.put(type, listener);
		}
	}
	
	public void removeConnectorListener(MediaType type) throws Exception {
		if (started()) {
			ClientConnector connector = getConnector(type);
			
			if (connector != null && connector.started()) {
				connector.stop();
				connector.destroy();
				
				removeConnector(type);
			}
		}
		else {
			listeners.remove(type);
		}
	}
	
	@Override
	protected void initInternal() throws ExecutableException {
		for (ClientConnector connector : connectors.values()) {
			try {
				connector.init();
			}
			catch (Exception e) {
				String message = "Failed to init connector " + connector;
				LOG.error(message, e);

				// Exit on failure.
				throw new ExecutableException(message);
			}
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		for (ClientConnector connector : connectors.values()) {
			try {
				connector.start();
			}
			catch (Exception e) {
				String message = "Failed to start connector " + connector;
				LOG.error(message, e);

				// Exit on failure.
				throw new ExecutableException(message);
			}
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		for (ClientConnector connector : connectors.values()) {
			// Stop only started Connectors.
			if (connector.getState() != ExecutableState.Started)
				continue;

			try {
				connector.stop();
			}
			catch (Exception e) {
				LOG.error("Failed to stop connector " + connector, e);
			}
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		for (ClientConnector connector : connectors.values()) {
			try {
				connector.destroy();
			}
			catch (Exception e) {
				LOG.error("Failed to destroy connector " + connector, e);
			}
		}

		connectors.clear();
		classroom = null;
	}
	
	private ClientConnector getConnector(MediaType type) {
		return connectors.get(type);
	}
	
	/**
	 * Add a new Connector to the set, and associate it with this Receiver.
	 * 
	 * @param connector The Connector to be added.
	 */
	private void addConnector(MediaType type, ClientConnector connector) {
		connectors.put(type, connector);

		if (started()) {
			try {
				connector.init();
				connector.start();
			}
			catch (ExecutableException e) {
				LOG.error("Failed to start connector " + connector, e);
			}
		}
	}
	
	/**
	 * Remove the specified Connector from this Receiver. The removed Connector
	 * will also be stopped.
	 * 
	 * @param type The MediaType of the Connector to be removed.
	 */
	private void removeConnector(MediaType type) {
		ClientConnector connector = connectors.remove(type);
		
		if (connector != null && connector.started()) {
			try {
				connector.stop();
				connector.destroy();
			}
			catch (ExecutableException e) {
				LOG.error("Failed to stop connector " + connector, e);
			}
		}
	}
	
}
