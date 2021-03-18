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

package org.lecturestudio.web.api.model;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.net.protocol.Transport;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.web.api.config.WebServiceConfiguration;
import org.lecturestudio.web.api.connector.RelayConnectors;
import org.lecturestudio.web.api.connector.RelayConnectorsFactory;
import org.lecturestudio.web.api.connector.server.Connector;
import org.lecturestudio.web.api.connector.server.Connectors;

public class StreamService extends ClassroomService {

	private AudioFormat audioFormat;
	
	private String audioCodec;


	@Override
	public RelayConnectors initialize(Classroom classroom, WebServiceConfiguration config, HttpServletRequest request) throws Exception {
		String hostName = request.getLocalAddr();
		String contextPath = classroom.getShortName();
		String classroomDir = getClassroomDir(request, config, contextPath);

		RelayConnectors relayConnectors = RelayConnectorsFactory.createRelayConnectors(hostName, config.mediaTransport, Transport.RTP_TCP);

		Connectors providerConnectors = relayConnectors.getProviderConnectors();
		Connectors receiverConnectors = relayConnectors.getReceiverConnectors();

		providerConnectors.start();
		receiverConnectors.start();

		List<StreamDescription> streamDescriptions = receiverConnectors.getConnectors().
				stream().
				map(Connector::getStreamDescription).
				collect(Collectors.toList());

		setStreamDescriptions(streamDescriptions);

		Path classroomPath = Paths.get(classroomDir);

		if (Files.exists(classroomPath)) {
			// Classroom already created. Check document file checksums.
			String whitelist = String.join(",", config.fileWhitelist);
			DirectoryStream<Path> stream = Files.newDirectoryStream(classroomPath, "*.{" + whitelist + "}");
			MessageDigest digest = MessageDigest.getInstance("MD5");
			Iterator<ClassroomDocument> iter = classroom.getDocuments().iterator();

			while (iter.hasNext()) {
				ClassroomDocument doc = iter.next();
				String docFile = doc.getFileName();
				String docHash = doc.getChecksum();

				for (Path entry : stream) {
					if (entry.getFileName().toString().equals(docFile)) {
						// Compare document checksums.
						String extHash = FileUtils.getChecksum(digest, entry.toFile());

						if (docHash.equals(extHash)) {
							// Checksums match. Remove document from required list.
							iter.remove();
						}
						break;
					}
				}
			}
		}

		return relayConnectors;
	}

	/**
	 * @return the audioFormat
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}
	
	/**
	 * @param audioFormat the audioFormat to set
	 */
	public void setAudioFormat(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
	}
	
	/**
	 * @return the audioCodec
	 */
	public String getAudioCodec() {
		return audioCodec;
	}
	
	/**
	 * @param audioCodec the audioCodec to set
	 */
	public void setAudioCodec(String audioCodec) {
		this.audioCodec = audioCodec;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getSimpleName());
		buffer.append(": ");
		buffer.append("Audio Format: ");
		buffer.append(getAudioFormat());
		buffer.append(", ");
		buffer.append("Audio Codec: ");
		buffer.append(getAudioCodec());
		buffer.append(", ");
		buffer.append("Stream Descriptions: ");
		buffer.append(this.getStreamDescriptions());

		return buffer.toString();
	}

	private String getClassroomDir(HttpServletRequest request, WebServiceConfiguration config, String contextPath) {
		String baseDir = request.getServletContext().getRealPath("/");
		String classroomsDir = config.classroomsDir;
		classroomsDir = baseDir + File.separator + classroomsDir;
		classroomsDir = classroomsDir + File.separator + contextPath;

		return classroomsDir;
	}
}
