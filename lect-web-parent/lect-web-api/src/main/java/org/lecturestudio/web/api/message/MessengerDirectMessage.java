package org.lecturestudio.web.api.message;

import org.lecturestudio.web.api.model.Message;

import java.time.ZonedDateTime;
import java.util.StringJoiner;

public class MessengerDirectMessage extends MessengerMessage {

	private String recipient;


	public MessengerDirectMessage() {

	}

	public MessengerDirectMessage(String recipient) {
		setRecipient(recipient);
	}

	public MessengerDirectMessage(MessengerDirectMessage other) {
		setRecipient(other.recipient);
		setFirstName(other.getFirstName());
		setFamilyName(other.getFamilyName());
		setMessage(other.getMessage());
		setUserId(other.getUserId());
		setDate(other.getDate());
		setMessageId(other.getMessageId());
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