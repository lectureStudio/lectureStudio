package org.lecturestudio.web.api.service;

import java.util.function.Consumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.lecturestudio.web.api.client.MessageProviderClient;
import org.lecturestudio.web.api.message.MessengerMessage;

@Dependent
public class MessageProviderService extends ReactiveProviderService {

	private final MessageProviderClient providerClient;


	@Inject
	public MessageProviderService(ServiceParameters parameters) {
		super(parameters);

		providerClient = createProxy(MessageProviderClient.class, parameters);
	}

	public String startMessenger(String classroomId) {
		return providerClient.startMessenger(classroomId);
	}

	public void stopMessenger(String classroomId, String serviceId) {
		providerClient.stopMessenger(classroomId, serviceId);
	}

	public void subscribe(String serviceId, Consumer<MessengerMessage> onEvent,
			Consumer<Throwable> onError) {
		subscribeSse("/api/message/subscribe/" + serviceId,
				MessengerMessage.class, onEvent, onError);
	}
}
