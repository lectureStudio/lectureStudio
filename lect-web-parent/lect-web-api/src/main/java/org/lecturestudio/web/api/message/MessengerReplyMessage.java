package org.lecturestudio.web.api.message;

import java.time.ZonedDateTime;

public class MessengerReplyMessage extends WebMessage {

	private String repliedMessageId;


	public MessengerReplyMessage(WebMessage toReply) {
		this(toReply, null, null, null);
	}

	public MessengerReplyMessage(WebMessage toReply, String remoteAddress,
			ZonedDateTime date) {
		this(toReply, remoteAddress, date, null);
	}

	public MessengerReplyMessage(WebMessage toReply, String userId,
			ZonedDateTime date, String messageId) {
		setUserId(userId);
		setDate(date);
		setMessageId(messageId);
		this.repliedMessageId = toReply.getMessageId();
	}

	public String getRepliedMessageId() {
		return repliedMessageId;
	}
}