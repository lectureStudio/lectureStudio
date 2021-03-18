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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.codec.AudioCodecLoader;
import org.lecturestudio.core.audio.codec.AudioCodecProvider;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomDocument;
import org.lecturestudio.web.api.model.StreamService;
import org.lecturestudio.web.api.ws.ClassroomServiceClient;
import org.lecturestudio.web.api.ws.StreamServiceClient;

import org.lecturestudio.media.playback.StreamEventExecutor;

/**
 * The {@code StreamingController} is a controller class that maintains the
 * {@link RtpAudioClient} and {@link RtpEventClient}. This class is the
 * interface between user interface and the servers.
 * 
 * @author Alex Andres
 */
public class StreamingClient extends ExecutableBase {
	
	private final ApplicationContext context;
	
	private final Classroom classroom;

	private final ClassroomServiceClient classroomClient;

	private final StreamServiceClient streamClient;
	
	private final Map<MediaType, MediaStreamClient<?>> clients;

	private MediaStreamReceiver streamReceiver;


	public StreamingClient(ApplicationContext context, Classroom classroom, ClassroomServiceClient classroomClient, StreamServiceClient streamClient) {
		this.context = context;
		this.classroom = classroom;
		this.classroomClient = classroomClient;
		this.streamClient = streamClient;
		this.clients = new HashMap<>();
	}
	
	public void setVolume(float volume) throws Exception {
		RtpAudioClient audioClient = (RtpAudioClient) getClient(MediaType.Audio);
		
		if (isNull(audioClient)) {
			throw new NullPointerException("No audio player initialized.");
		}
		
		audioClient.setVolume(volume);
	}
	
	public void startCameraStream() throws Exception {
		if (!started()) {
			throw new Exception("Connect to a classroom first.");
		}
		
		MediaStreamClient<?> cameraClient = getClient(MediaType.Camera);
		
		if (isNull(cameraClient)) {
			createCameraClient();
			
			cameraClient = getClient(MediaType.Camera);
			cameraClient.start();
		}
		else {
			if (cameraClient.getState() == ExecutableState.Stopped) {
				cameraClient.start();
			}
		}
	}
	
	public void stopCameraStream() throws Exception {
		if (!started()) {
			throw new Exception("Connect to a classroom first.");
		}
		
		MediaStreamClient<?> cameraClient = getClient(MediaType.Camera);
		
		if (nonNull(cameraClient)) {
			cameraClient.unregister(streamReceiver);
			cameraClient.stop();
			cameraClient.destroy();
			
			removeClient(MediaType.Camera);
		}
	}
	
	@Override
	protected void initInternal() throws ExecutableException {
		// Create media stream receivers.
		try {
			createStreamReceiver();
			createAudioClient(classroom);
			createEventClient();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		// It's imperative to start the clients first in order to receive all incoming packets.
		for (MediaStreamClient<?> client : clients.values()) {
			client.start();
		}

		try {
			streamReceiver.joinClassroom(classroom);
		}
		catch (Exception e) {
			e.printStackTrace();

			// Shutdown clients, since they cannot be used.
			try {
				shutdownClients();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}

			// Propagate caught exception.
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			shutdownClients();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
		
		streamReceiver.stop();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		streamReceiver.destroy();
	}
	
	public String getDocument(DocumentType docType, String docFileName, String docChecksum) throws Exception {
		if (!started()) {
			throw new Exception("Connect to a classroom first.");
		}
		
		String docPath = null;
		
		if (docType == DocumentType.PDF) {
			File docRoot = new File(context.getDataLocator().toAppDataPath("media"));
			if (!docRoot.exists()) {
				docRoot.mkdirs();
			}

			String docRootPath = docRoot.getPath();
			
			docPath = docRootPath + File.separator + docFileName;
			
			// Search document root path for existing match.
			MessageDigest digest = MessageDigest.getInstance("MD5");
			DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(docRootPath), "*.{pdf}");

			for (Path entry : stream) {
				if (entry.getFileName().toString().equals(docFileName)) {
					// Compare document checksums.
					String extHash = FileUtils.getChecksum(digest, entry.toFile());

					if (docChecksum.equals(extHash)) {
						// Checksums match. No need to download document.
						return docPath;
					}
					break;
				}
			}

			// Download document.
			FileOutputStream outStream = new FileOutputStream(new File(docPath));
			streamReceiver.getDocument(new ClassroomDocument(docFileName), outStream);
			outStream.close();
		}
		
		return docPath;
	}

	private void createStreamReceiver() throws Exception {
		streamReceiver = new MediaStreamReceiver(classroomClient, streamClient);
	}

	private void createAudioClient(Classroom classroom) throws Exception {
		Optional<StreamService> service = classroom.getServices()
				.stream()
				.filter(StreamService.class::isInstance)
				.map(StreamService.class::cast)
				.findFirst();

		if (!service.isPresent()) {
			throw new Exception("Stream service is not started.");
		}

		StreamService streamService = service.get();

		String codec = streamService.getAudioCodec();
		AudioFormat audioFormat = streamService.getAudioFormat();
		AudioCodecProvider codecProvider = AudioCodecLoader.getInstance().getProvider(codec);
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		
		RtpAudioClient audioClient = new RtpAudioClient(audioConfig, codecProvider, audioFormat);
		audioClient.register(streamReceiver);
		
		addClient(MediaType.Audio, audioClient);
	}
	
	private void createEventClient() throws Exception {
		StreamEventExecutor eventExecutor = new StreamEventExecutor(new ToolController(context, context.getDocumentService()));
		RtpEventClient eventClient = new RtpEventClient(eventExecutor);
		eventClient.register(streamReceiver);
		
		addClient(MediaType.Event, eventClient);
	}
	
	private void createCameraClient() throws Exception {
		CameraClient cameraClient = new CameraClient();
		cameraClient.register(streamReceiver);
		
		addClient(MediaType.Camera, cameraClient);
	}
	
	private void shutdownClients() throws Exception {
		for (MediaStreamClient<?> client : clients.values()) {
			client.unregister(streamReceiver);
			client.stop();
			client.destroy();
		}
		
		clients.clear();
	}
	
	private synchronized void addClient(MediaType type, MediaStreamClient<?> client) {
		clients.put(type, client);
	}
	
	private synchronized MediaStreamClient<?> getClient(MediaType type) {
		return clients.get(type);
	}
	
	private synchronized void removeClient(MediaType type) {
		clients.remove(type);
	}
	
}
