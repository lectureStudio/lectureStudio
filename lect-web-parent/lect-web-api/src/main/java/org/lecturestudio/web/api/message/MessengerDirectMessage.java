package org.lecturestudio.web.api.message;

import org.lecturestudio.web.api.model.Message;

import java.time.ZonedDateTime;

public class MessengerDirectMessage extends WebMessage {

	private Message message;

	private Boolean reply;

	private String recipient;


	public MessengerDirectMessage(String recipient) {
		this(recipient, null, null, null, null);
	}

	public MessengerDirectMessage(String recipient, Message message,
			String remoteAddress, ZonedDateTime date) {
		this(recipient, message, remoteAddress, date, null);
	}

	public MessengerDirectMessage(String recipient, Message message,
			String userId, ZonedDateTime date, String messageId) {
		setRecipient(recipient);
		setMessage(message);
		setUserId(userId);
		setDate(date);
		setMessageId(messageId);
	}

	public Message getMessage() {
		return message;
	}

	public Boolean isReply() {
		return reply;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public void setReply(boolean reply) {
		this.reply = reply;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	@Override
	public String toString() {
		return "MessengerDirectMessage{" +
				"message=" + message +
				", reply=" + reply +
				", recipient='" + recipient + '\'' +
				'}';
	}
}