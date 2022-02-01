package org.lecturestudio.web.api.message;

import org.lecturestudio.web.api.model.Message;

import java.time.ZonedDateTime;

public class MessengerReplyMessage extends MessengerMessage {

    private String repliedMessageId;

    public MessengerReplyMessage(MessengerMessage toReply) {
        this(toReply, null, null, null);
    }

    public MessengerReplyMessage(MessengerMessage toReply, Message message, String remoteAddress, ZonedDateTime date) {
        this(toReply, message, remoteAddress, date, null);
    }

    public MessengerReplyMessage(MessengerMessage toReply, Message message, String remoteAddress, ZonedDateTime date, String messageId) {
        setMessage(message);
        setRemoteAddress(remoteAddress);
        setDate(date);
        setMessageId(messageId);
        this.repliedMessageId = toReply.getMessageId();
    }

    public String getRepliedMessageId() {
        return repliedMessageId;
    }
}
