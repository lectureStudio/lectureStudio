/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.service;

import java.text.MessageFormat;
import java.util.Properties;

import org.lecturestudio.core.beans.StringProperty;

/**
 * Holds information (e.g. URLs) to establish a connection to various
 * web-services.
 *
 * @author Alex Andres
 */
public class WebServiceInfo {

	private final String janusWebSocketUrl;

	private final String streamStateWebSocketUrl;

	private final String streamPublisherApiUrl;

	private final String streamMessageApiUrl;

	private final String streamStunServers;

	private StringProperty serverName;


	/**
	 * Create a new {@code WebServiceInfo}.
	 *
	 * @param serviceProps {@code Properties} containing the service
	 *                     information.
	 */
	public WebServiceInfo(Properties serviceProps) {
		janusWebSocketUrl = serviceProps.getProperty("stream.janus.websocket.url");
		streamStateWebSocketUrl = serviceProps.getProperty("stream.state.websocket.url");
		streamPublisherApiUrl = serviceProps.getProperty("stream.publisher.api.url");
		streamMessageApiUrl = serviceProps.getProperty("stream.message.api.url");
		streamStunServers = serviceProps.getProperty("stream.stun.servers");
	}

	/**
	 * @return The Janus WebRTC Websocket endpoint URL.
	 */
	public String getJanusWebSocketUrl() {
		return MessageFormat.format(janusWebSocketUrl, serverName.get());
	}

	/**
	 * @return The stream publisher API endpoint URL.
	 */
	public String getStreamPublisherApiUrl() {
		return MessageFormat.format(streamPublisherApiUrl, serverName.get());
	}

	/**
	 * @return The Websocket endpoint URL to maintain the stream state.
	 */
	public String getStreamStateWebSocketUrl() {
		return MessageFormat.format(streamStateWebSocketUrl, serverName.get());
	}

	/**
	 * @return The course feature API endpoint URL.
	 */
	public String getStreamMessageApiUrl() {
		return MessageFormat.format(streamMessageApiUrl, serverName.get());
	}

	/**
	 * @return The STUN servers used for streaming.
	 */
	public String getStreamStunServers() {
		return streamStunServers;
	}

	/**
	 * @param serverName The name of the server that provides the streaming
	 *                   services.
	 */
	public void setServerName(StringProperty serverName) {
		this.serverName = serverName;
	}
}
