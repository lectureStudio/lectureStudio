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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;

/**
 * A {@code Participant} is represented by an IP address and open ports mapped
 * to the media type. This class is used to register connected clients and to
 * distribute the streams to them.
 * 
 * @author Alex Andres
 * 
 */
public class Participant {

	/**
	 * The IP address of the participant.
	 */
	private String ipAddress;

	/**
	 * The map of media addresses.
	 */
	private Map<String, InetSocketAddress> addressMap;


	/**
	 * Creates a new {@link Participant} with provided address parameters.
	 * 
	 * @param ipAddress
	 *            the IP address of the participant
	 * @param addressMap
	 *            map of media addresses
	 */
	public Participant(String ipAddress, Map<String, InetSocketAddress> addressMap) {
		this.ipAddress = ipAddress;
		this.addressMap = addressMap;
	}

	/**
	 * @return the IP address of the participant
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param mediaName the media name
	 * 
	 * @return the socket address for the corresponding media
	 */
	public InetSocketAddress getInetSocketAddress(String mediaName) {
		return addressMap.get(mediaName);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(ipAddress).append(" : ");

		int i = 0;
		Collection<InetSocketAddress> values = addressMap.values();

		for (InetSocketAddress socket : values) {
			buffer.append(socket.getPort());

			if (i < values.size() - 1)
				buffer.append(", ");

			i++;
		}

		return buffer.toString();
	}

}
