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

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.core.net.protocol.Transport;
import org.lecturestudio.web.api.connector.server.Connector;
import org.lecturestudio.web.api.connector.server.Connectors;
import org.lecturestudio.web.api.connector.server.EventWriteHandler;
import org.lecturestudio.web.api.connector.server.OutboundHandler;
import org.lecturestudio.web.api.connector.server.TcpConnectorRelayHandler;
import org.lecturestudio.web.api.connector.server.WebSocketOutboundHandler;
import org.lecturestudio.web.api.connector.server.WriteHandler;
import org.lecturestudio.web.api.model.StreamDescription;

public class RelayConnectorsFactory {

	public static RelayConnectors createRelayConnectors(String hostName, Transport providerTransport, Transport receiverTransport) {
		List<StreamDescription> providerDescList = new ArrayList<>();
		List<StreamDescription> receiverDescList = new ArrayList<>();
		
		providerDescList.add(new StreamDescription(hostName, ConnectorFactory.getNextPort(), providerTransport, MediaType.Audio));
		providerDescList.add(new StreamDescription(hostName, ConnectorFactory.getNextPort(), providerTransport, MediaType.Camera));
		providerDescList.add(new StreamDescription(hostName, ConnectorFactory.getNextPort(), providerTransport, MediaType.Event));
		
		receiverDescList.add(new StreamDescription(hostName, ConnectorFactory.getNextPort(), receiverTransport, MediaType.Audio));
		receiverDescList.add(new StreamDescription(hostName, ConnectorFactory.getNextPort(), receiverTransport, MediaType.Camera));
		receiverDescList.add(new StreamDescription(hostName, ConnectorFactory.getNextPort(), receiverTransport, MediaType.Event));
		
		Connectors receiverConnectors = new Connectors();
		Connectors providerConnectors = new Connectors();

		for (StreamDescription providerDesc : providerDescList) {
			Connector providerConnector = ConnectorFactory.createConnector(providerDesc);
			
			for (StreamDescription receiverDesc : receiverDescList) {
				if (providerDesc.getMediaType() == receiverDesc.getMediaType()) {
					WriteHandler writeHandler;
					OutboundHandler outboundHandler;

					if (receiverTransport == Transport.WS || receiverTransport == Transport.WSS) {
						outboundHandler = new WebSocketOutboundHandler();
					}
					else {
						outboundHandler = new OutboundHandler();
					}
					
					if (receiverDesc.getMediaType() == MediaType.Event) {
						writeHandler = new EventWriteHandler(outboundHandler);
					}
					else {
						writeHandler = new WriteHandler(outboundHandler);
					}

					Connector relayConnector = ConnectorFactory.createConnector(receiverDesc);
					relayConnector.addChannelHandler(writeHandler);
					
					providerConnector.addChannelHandler(new TcpConnectorRelayHandler(writeHandler));

					receiverConnectors.addConnector(relayConnector);
					break;
				}
			}

			providerConnectors.addConnector(providerConnector);
		}

		RelayConnectors relayConnectors = new RelayConnectors();
		relayConnectors.setReceiverConnectors(receiverConnectors);
		relayConnectors.setProviderConnectors(providerConnectors);
		
		return relayConnectors;
	}
	
	public static RelayConnectors createProviderConnectors(String hostName, Transport mediaTransport) {
		StreamDescription streamDescription = new StreamDescription(hostName, 8081, mediaTransport, MediaType.Messenger);

		Connector connector = ConnectorFactory.createConnector(streamDescription);
		connector.addChannelHandler(new WriteHandler(new OutboundHandler()));
		
		Connectors connectors = new Connectors();
		connectors.addConnector(connector);
		
		RelayConnectors relayConnectors = new RelayConnectors();
		relayConnectors.setProviderConnectors(connectors);

		return relayConnectors;
	}

}
