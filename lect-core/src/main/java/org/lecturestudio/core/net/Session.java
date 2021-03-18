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

package org.lecturestudio.core.net;

import java.util.HashMap;
import java.util.Map;

import org.lecturestudio.core.net.protocol.Transport;

/**
 * The Session describes the media that should be broadcasted. A Session
 * defines the media provider who specifies which media types should be
 * broadcasted by associated transport protocols. Each Session is identified
 * by an unique name.
 * 
 * @author Alex Andres
 */
public class Session
{
	/** The name of the session. */
	private String sessionName;
	
	/** The address of the media provider. */
	private String providerAddress;
	
//	/** The port of the media provider. */
//	private int providerPort;
	
	/** The media type to broadcast transport mapping. */
	private Map<MediaType, Transport> mediaTransportTypes;
	
	
	/**
	 * Create a new Session.
	 */
	public Session()
	{
		mediaTransportTypes = new HashMap<>();
	}
	
	/**
	 * Get the name of the Session.
	 */
	public String getSessionName()
	{
		return sessionName;
	}

	/**
	 * Set the new name of this Session.
	 * 
	 * @param sessionName The new name.
	 */
	public void setSessionName(String sessionName)
	{
		this.sessionName = sessionName;
	}
	
	/**
	 * Get the provider's remote address.
	 */
	public String getProviderAddress()
	{
		return providerAddress;
	}

	/**
	 * Set new provider address. The address describes a socket address.
	 * 
	 * @param providerAddress The provider's remote address.
	 */
	public void setProviderAddress(String providerAddress)
	{
		this.providerAddress = providerAddress;
	}
	
//	/**
//	 * Get the provider's remote port.
//	 */
//	public int getProviderPort()
//	{
//		return providerPort;
//	}
//
//	/**
//	 * Get provider's port associated to the socket address.
//	 * 
//	 * @param providerPort The port associated to the socket address.
//	 */
//	public void setProviderPort(int providerPort)
//	{
//		this.providerPort = providerPort;
//	}
	
	/**
	 * Add a new MediaType to Transport mapping.
	 * 
	 * @param type The media type to broadcast.
	 * @param transport The transport protocol for the media type.
	 */
	public void addMediaType(MediaType type, Transport transport)
	{
		mediaTransportTypes.put(type, transport);
	}
	
	/**
	 * Get all MediaType to Transport mappings.
	 */
	public Map<MediaType, Transport> getMediaTransportTypes()
	{
		return mediaTransportTypes;
	}

	/**
	 * Get transport protocol for the media type.
	 * 
	 * @param type The transport protocol.
	 */
	public Transport getTransport(MediaType type)
	{
		return mediaTransportTypes.get(type);
	}

	@Override
	public String toString()
	{
		return sessionName + " @ " + providerAddress;
	}
	
}
