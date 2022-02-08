package org.lecturestudio.web.api.message;

import org.lecturestudio.web.api.model.Message;

import java.time.ZonedDateTime;

public class MessengerReplyMessage extends WebMessage {

    private String repliedMessageId;

    public MessengerReplyMessage(WebMessage toReply) {
        this(toReply, null, null, null);
    }

    public MessengerReplyMessage(WebMessage toReply, String remoteAddress, ZonedDateTime date) {
        this(toReply, remoteAddress, date, null);
    }

    public MessengerReplyMessage(WebMessage toReply, String remoteAddress, ZonedDateTime date, String messageId) {
        setRemoteAddress(remoteAddress);
        setDate(date);
        setMessageId(messageId);
        this.repliedMessageId = toReply.getMessageId();
    }

    public String getRepliedMessageId() {
        return repliedMessageId;
    }
}
