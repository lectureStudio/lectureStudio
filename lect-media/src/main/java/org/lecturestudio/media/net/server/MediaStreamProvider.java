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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.media.config.AudioStreamConfig;
import org.lecturestudio.media.config.CameraStreamConfig;
import org.lecturestudio.web.api.connector.ConnectorFactory;
import org.lecturestudio.web.api.connector.client.ClientConnector;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomDocument;
import org.lecturestudio.web.api.model.ClassroomService;
import org.lecturestudio.web.api.model.StreamDescription;
import org.lecturestudio.web.api.model.StreamService;
import org.lecturestudio.web.api.ws.StreamServiceClient;

public class MediaStreamProvider extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(MediaStreamProvider.class);

	private final Map<MediaType, Executable> servers;

	private final ApplicationContext context;
	
	private final StreamServiceClient streamService;

	private StreamService streamConfig;

	private AudioStreamConfig audioStreamConfig;

	private CameraStreamConfig cameraStreamConfig;
	
	private Classroom classroom;
	
	
	public MediaStreamProvider(ApplicationContext context, StreamServiceClient streamService) {
		this.context = context;
		this.streamService = streamService;
		this.servers = new HashMap<>();
	}

	public void setClassroom(Classroom classroom) {
		this.classroom = classroom;
	}

	public void setAudioStreamConfig(AudioStreamConfig config) {
		this.audioStreamConfig = config;
	}

	public void setCameraStreamConfig(CameraStreamConfig config) {
		this.cameraStreamConfig = config;
	}

	public void setStreamConfig(StreamService config) {
		this.streamConfig = config;
	}

	public void addDocument(String docName) throws Exception {
		if (getState() != ExecutableState.Started) {
			throw new Exception("No classroom created.");
		}

		for (Document doc : context.getDocuments().asList()) {
			String name = FileUtils.stripExtension(docName);
			
			if (name.equals(doc.getName())) {
				// Add specified document to the classroom.
				MessageDigest digest = MessageDigest.getInstance("MD5");
				String fileName = doc.getName() + ".pdf";
				
				// Check if a document with the same name already exists and remove it.
				classroom.getDocuments().removeIf(classDoc -> classDoc.getFileName().equals(fileName));

				ClassroomDocument classDoc = new ClassroomDocument();
				classDoc.setFileName(fileName);
				classDoc.setChecksum(doc.getChecksum(digest));

				classroom.getDocuments().add(classDoc);
				
				streamService.sendDocument(classroom, doc);
				break;
			}
		}
	}
	
	public void startCameraStream() throws ExecutableException {
		Executable cameraServer = getServer(MediaType.Camera);

		if (nonNull(cameraServer)) {
			throw new ExecutableException("Camera stream already open.");
		}

		Optional<StreamDescription> camDesc = classroom.getServices()
				.stream()
				.filter(StreamService.class::isInstance)
				.flatMap(service -> service.getStreamDescriptions().stream())
				.filter(desc -> desc.getMediaType() == MediaType.Camera)
				.findFirst();

		if (camDesc.isEmpty()) {
			throw new ExecutableException("Camera stream is not supported by this classroom.");
		}

		try {
			createServer(camDesc.get());
		}
		catch (Exception e) {
			throw new ExecutableException("Create camera server failed.", e);
		}

		cameraServer = getServer(MediaType.Camera);
		cameraServer.start();
	}

	public void stopCameraStream() throws ExecutableException {
		Executable cameraServer = getServer(MediaType.Camera);

		if (nonNull(cameraServer)) {
			cameraServer.stop();
			cameraServer.destroy();

			removeServer(MediaType.Camera);
		}

		setCameraStreamConfig(null);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		if (isNull(audioStreamConfig)) {
			throw new ExecutableException("Missing audio stream configuration.");
		}

		try {
			createStreamService();
		}
		catch (Exception e) {
			throw new ExecutableException("Create classroom failed.", e);
		}

		for (Executable server : servers.values()) {
			server.init();
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		for (Executable server : servers.values()) {
			server.start();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		for (Executable server : servers.values()) {
			// Stop only started servers.
			if (server.getState() == ExecutableState.Started) {
				server.stop();
			}
		}

		try {
			classroom = streamService.stopService(classroom, new StreamService());
		}
		catch (Exception e) {
			LOG.error("Close classroom stream service failed.", e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		for (Executable server : servers.values()) {
			server.destroy();
		}

		servers.clear();
		classroom = null;
	}
	
	private void createStreamService() throws Exception {
		if (isNull(classroom)) {
			throw new Exception("Missing classroom. Set classroom with 'setClassroom()'.");
		}

		// Create the classroom on the remote host.
		StreamService streamService = new StreamService();
		streamService.setAudioCodec(streamConfig.getAudioCodec());
		streamService.setAudioFormat(streamConfig.getAudioFormat());

		classroom = this.streamService.startService(classroom, streamService);

		if (!classroom.getDocuments().isEmpty()) {
			// Upload required documents.
			for (ClassroomDocument classDoc : classroom.getDocuments()) {
				if (classDoc.getFileName() == null) {
					continue;
				}

				for (Document doc : context.getDocuments().asList()) {
					String name = FileUtils.stripExtension(classDoc.getFileName());

					if (name.equals(doc.getName())) {
						this.streamService.sendDocument(classroom, doc);
						break;
					}
				}
			}
		}

		createConnectors(classroom);
	}

	private void createConnectors(Classroom classroom) throws Exception {
		for (ClassroomService service : classroom.getServices()) {
			if (service instanceof StreamService) {
				for (StreamDescription streamDesc : service.getStreamDescriptions()) {
					MediaType mediaType = streamDesc.getMediaType();

					if (mediaType == MediaType.Camera && isNull(cameraStreamConfig)) {
						continue;
					}

					createServer(streamDesc);
				}
			}
		}
	}

	private void createServer(StreamDescription streamDesc) throws Exception {
		MediaType mediaType = streamDesc.getMediaType();
		ClientConnector connector = ConnectorFactory.createClientConnector(streamDesc);

		switch (mediaType) {
			case Audio:
				createAudioServer(connector);
				break;

			case Camera:
				createCameraServer(connector);
				break;

			case Event:
				createEventServer(connector);
				break;

			default:
				throw new Exception("No server implementation for media type: " + mediaType);
		}
	}

	private Executable getServer(MediaType type) {
		return servers.get(type);
	}

	private void addServer(MediaType type, Executable server) {
		servers.put(type, server);
	}

	/**
	 * Remove a server with the specified media type from this Provider.
	 *
	 * @param type The MediaType of the server to be removed.
	 */
	private void removeServer(MediaType type) {
		servers.remove(type);
	}

	private void serverError(MediaType mediaType, Executable server) {
		removeServer(mediaType);

		if (servers.isEmpty()) {
			// All servers shut down due to error.
			try {
				setState(ExecutableState.Error);
			}
			catch (ExecutableException e) {
				LOG.error("Handle server error failed.", e);
			}
		}
	}

	private void registerServer(MediaType mediaType, ExecutableBase server) {
		server.addStateListener((oldState, newState) -> {
			if (started() && newState == ExecutableState.Destroyed) {
				serverError(mediaType, server);
			}
		});

		addServer(mediaType, server);
	}

	private void createAudioServer(ClientConnector connector) {
		RtpAudioServer audioServer = new RtpAudioServer(audioStreamConfig, connector);

		registerServer(MediaType.Audio, audioServer);
	}

	private void createEventServer(ClientConnector connector) {
		RtpEventServer eventServer = new RtpEventServer(connector, this);

		registerServer(MediaType.Event, eventServer);
	}

	private void createCameraServer(ClientConnector connector) {
		CameraServer cameraServer = new CameraServer(cameraStreamConfig, connector);

		registerServer(MediaType.Camera, cameraServer);
	}
}
