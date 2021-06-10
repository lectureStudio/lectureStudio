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

package org.lecturestudio.core.net.protocol;

/**
 * Transport enumerations to be used for media transmission. Each enumeration
 * has it's own short description which is retrieved by {@link #asString()}.
 * 
 * @author Alex Andres
 */
public enum Transport {

	/** UDP transport with RTP payload. */
	RTP_UDP("RTP/UDP"),
	
	/** TCP transport with RTP payload. */
	RTP_TCP("RTP/TCP"),
	
	/** TLS encrypted TCP transport with RTP payload. */
	RTP_TCP_TLS("RTP/TCP-TLS"),
	
	/** HTTP transport with RTP payload. */
	RTP_HTTP("RTP/HTTP"),
	
	/** TLS encrypted HTTP transport with RTP payload. */
	RTP_HTTP_TLS("RTP/HTTP-TLS"),
	
	/** Web-Server HTTP transport. */
	HTTP("HTTP"),
	
	/** Web-Server TLS encrypted HTTP transport. */
	HTTP_TLS("HTTP-TLS"),

	/** WebSocket transport. */
	WS("WS"),

	/** WebSocket TLS encrypted transport. */
	WSS("WS-TLS");


	/** The description. */
	private final String name;


	/**
	 * Create a new {@link Transport} enumeration with the specified description.
	 * 
	 * @param name The description.
	 */
	Transport(String name) {
		this.name = name;
	}

	/**
	 * Get the description for this transport protocol.
	 *
	 * @return The description for this transport protocol.
	 */
	public String asString() {
		return name;
	}

	public boolean isEncrypted() {
		return this == RTP_TCP_TLS;
	}

	/**
	 * Specifies whether TLS is enabled.
	 *
	 * @return {@code true} if TLS is enabled, otherwise {@code false}.
	 */
	public boolean isTlsEnabled() {
		return this == RTP_TCP_TLS || this == RTP_HTTP_TLS || this == HTTP_TLS;
	}
}
