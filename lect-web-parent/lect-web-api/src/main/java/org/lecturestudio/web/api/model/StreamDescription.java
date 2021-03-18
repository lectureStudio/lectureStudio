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

import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.core.net.protocol.Transport;

import java.io.Serializable;

public class StreamDescription implements Serializable {

	/** The address of the media provider. */
	private String address;
	
	/** The port of the media provider. */
	private int port;
	
	/** The transport protocol of the stream. */
	private Transport transport;
	
	/** The media type of the stream. */
	private MediaType mediaType;

	
	public StreamDescription() {
		
	}
	
	public StreamDescription(String address, int port, Transport transport, MediaType mediaType) {
		setAddress(address);
		setPort(port);
		setTransport(transport);
		setMediaType(mediaType);
	}
	
	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the transport
	 */
	public Transport getTransport() {
		return transport;
	}

	/**
	 * @param transport the transport to set
	 */
	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	/**
	 * @return the mediaType
	 */
	public MediaType getMediaType() {
		return mediaType;
	}

	/**
	 * @param mediaType the mediaType to set
	 */
	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	@Override
	public String toString() {
		return "StreamDescription [" + address + ", " + port + ", " + transport + ", " + mediaType + "]";
	}
	
}
