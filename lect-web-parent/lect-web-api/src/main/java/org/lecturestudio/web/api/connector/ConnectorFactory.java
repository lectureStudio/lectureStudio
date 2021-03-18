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

package org.lecturestudio.web.api.connector;

import org.lecturestudio.web.api.connector.client.ClientConnector;
import org.lecturestudio.web.api.connector.client.ClientTcpConnector;
import org.lecturestudio.web.api.connector.server.Connector;
import org.lecturestudio.web.api.connector.server.TcpConnector;
import org.lecturestudio.web.api.connector.server.UdpConnector;
import org.lecturestudio.web.api.connector.server.WebSocketConnector;
import org.lecturestudio.web.api.model.StreamDescription;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectorFactory {

	/**
	 * The dynamic port range defined by IANA consists of the 49152-65535
	 * range, and is meant for the selection of ephemeral ports.
	 */
	private static final int INIT_PORT = 49152;
	
	private static AtomicInteger CURRENT_PORT = new AtomicInteger(INIT_PORT);
	
	
	public static Connector createConnector(StreamDescription desc) {
		switch (desc.getTransport()) {
			case RTP_TCP:
			case RTP_TCP_TLS:
				return new TcpConnector(desc);

			case RTP_UDP:
				return new UdpConnector(desc);

			case WS:
			case WSS:
				return new WebSocketConnector(desc);

			default:
				return null;
		}
	}
	
	public static ClientConnector createClientConnector(StreamDescription desc) throws Exception {
		switch (desc.getTransport()) {
			case RTP_TCP:
				return new ClientTcpConnector(desc);

			default:
				throw new Exception("No mapping for a ClientConnector for transport: " + desc.getTransport());
		}
	}
	
	public static int getNextPort() {
		/*
		 * Alternatively use one of the 'Algorithms for the Obfuscation of the Ephemeral Port Selection':
		 * 
		 * https://tools.ietf.org/html/rfc6056
		 */
		
		// Handle wrap-around in case the maximum port number is reached.
		if (CURRENT_PORT.get() > 65534) {
			CURRENT_PORT.set(INIT_PORT);
		}
		return CURRENT_PORT.incrementAndGet();
	}
	
}
