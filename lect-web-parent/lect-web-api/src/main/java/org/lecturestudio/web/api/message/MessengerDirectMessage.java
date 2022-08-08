package org.lecturestudio.web.api.message;

import org.lecturestudio.web.api.model.Message;

import java.time.ZonedDateTime;
import java.util.StringJoiner;

public class MessengerDirectMessage extends MessengerMessage {

	private String recipient;


	public MessengerDirectMessage() {
		this(null);
	}

	public MessengerDirectMessage(String recipient) {
		this(recipient, null, null, null, null);
	}

	public MessengerDirectMessage(String recipient, Message message,
			String userId, ZonedDateTime date, String messageId) {
		setRecipient(recipient);
		setMessage(message);
		setUserId(userId);
		setDate(date);
		setMessageId(messageId);
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ",
				MessengerDirectMessage.class.getSimpleName() + "[", "]").add(
				"recipient='" + recipient + "'").toString();
	}
}