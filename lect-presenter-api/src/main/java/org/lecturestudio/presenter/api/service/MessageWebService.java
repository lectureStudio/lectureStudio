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

package org.lecturestudio.presenter.api.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import com.google.common.eventbus.Subscribe;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.core.util.NetUtils;
import org.lecturestudio.media.config.NetworkConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.event.AbuseMessageEvent;
import org.lecturestudio.presenter.api.util.HtmlMessageLogger;
import org.lecturestudio.web.api.connector.ConnectorFactory;
import org.lecturestudio.web.api.connector.JsonDecoder;
import org.lecturestudio.web.api.connector.client.ClientConnector;
import org.lecturestudio.web.api.connector.client.ClientTcpConnectorHandler;
import org.lecturestudio.web.api.connector.client.ConnectorListener;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.WebPacket;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.MessageService;
import org.lecturestudio.web.api.model.StreamDescription;
import org.lecturestudio.web.api.ws.ConnectionParameters;
import org.lecturestudio.web.api.ws.MessageServiceClient;
import org.lecturestudio.web.api.ws.rs.MessageRestClient;

public class MessageWebService extends ExecutableBase implements ConnectorListener<WebPacket> {

	private final ApplicationContext context;
	
	/** The web service client. */
	private MessageServiceClient webService;
	
	private ClientConnector connector;
	
	private Classroom classroom;

	private org.lecturestudio.web.api.model.MessageService service;
	
	/** A message logger. */
	private HtmlMessageLogger logger;

	/* The received message count. */
	private long messageCount;
	
	
	/**
	 * Creates a new {@link MessageWebService}.
	 * 
	 * @param context The ApplicationContext.
	 */
	public MessageWebService(ApplicationContext context) {
		this.context = context;
	}
	
	@Subscribe
	public void onEvent(AbuseMessageEvent event) {
		logAbuse(event.getHost(), event.getMessage());
	}
	
	@Override
	public void onConnectorRead(WebPacket packet) {
		Class<?> msgClass = packet.getMessage().getClass();

		if (MessengerMessage.class.isAssignableFrom(msgClass)) {
			MessengerMessage msgMessage = (MessengerMessage) packet.getMessage();

			logMessage(msgMessage.getRemoteAddress(), msgMessage.getMessage());

			messageCount++;

			// Forward message to UI.
			ApplicationBus.post(msgMessage);
			ApplicationBus.post(new MessageWebServiceState(getState(), messageCount));
		}
	}
	
	@Override
	protected void initInternal() throws ExecutableException {
		messageCount = 0;

		try {
			initSession();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			Classroom webClassroom = webService.startService(classroom, service);

			connector = createConnector(webClassroom);
			connector.start();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		createLogFile();
		
		ApplicationBus.register(this);
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		ApplicationBus.unregister(this);
		
		try {
			webService.stopService(classroom, service);

			connector.stop();
			connector.destroy();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() {

	}
	
	/**
	 * Creates a new log file. The log file is parsed and written in the HTML
	 * format.
	 */
	private void createLogFile() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH.mm");
		String date = dateFormat.format(new Date());

		String name = classroom.getShortName() + "_" + date + ".html";
		File messengerLog = new File(context.getDataLocator().toAppDataPath(name));

		logger = new HtmlMessageLogger(messengerLog);
	}

	/**
	 * Adds a message to the log file.
	 * 
	 * @param host The host name or IP address.
	 * @param message The descriptive message.
	 */
	private void logMessage(String host, String message) {
		logger.logMessage(host, new Date(), message);
	}
	
	/**
     * Adds a abuse message to the log file.
     *
	 * @param host The host name or IP address.
	 * @param message The descriptive message.
     */
	private void logAbuse(String host, String message) {
		logger.logAbuse(host, message);
	}
	
	private void initSession() throws Exception {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration netConfig = config.getNetworkConfig();
		String broadcastAddress = netConfig.getBroadcastAddress();
		int broadcastPort = netConfig.getBroadcastTlsPort();
		String classShortName = config.getClassroomShortName();

		ConnectionParameters parameters = new ConnectionParameters(broadcastAddress, broadcastPort, true);

		if (NetUtils.isLocalAddress(broadcastAddress, broadcastPort)) {
			// No need to identify classroom by short name on the local machine.
			classShortName = "";
		}

		webService = new MessageRestClient(parameters);

		classroom = new Classroom(config.getClassroomName(), classShortName);
		classroom.setLocale(config.getLocale());
		classroom.setShortName(classShortName);
		classroom.setIpFilterRules(netConfig.getIpFilter().getRules());

		service = new org.lecturestudio.web.api.model.MessageService();
	}

	private ClientConnector createConnector(Classroom classroom) throws Exception {
		Optional<StreamDescription> streamDesc = classroom.getServices()
				.stream()
				.filter(MessageService.class::isInstance)
				.flatMap(service -> service.getStreamDescriptions().stream())
				.filter(desc -> desc.getMediaType() == MediaType.Messenger)
				.findFirst();

		if (streamDesc.isEmpty()) {
			throw new Exception("No stream provided for the messenger session.");
		}

		ClientConnector connector = ConnectorFactory.createClientConnector(streamDesc.get());
		connector.addChannelHandler(new JsonDecoder());
		connector.addChannelHandler(new ClientTcpConnectorHandler<>(this));

		return connector;
	}
}
