package org.lecturestudio.web.api.message;

import org.lecturestudio.web.api.model.Message;

import java.time.ZonedDateTime;

public class MessengerDirectMessage extends WebMessage {

    private Message message;

    private boolean reply;

    private String messageDestinationUsername;


    public MessengerDirectMessage(String messageDestinationUsername) {
        this(messageDestinationUsername, null, null, null, null);
    }

    public MessengerDirectMessage(String messageDestinationUsername, Message message, String remoteAddress, ZonedDateTime date) {
        this(messageDestinationUsername, message, remoteAddress, date, null);
    }

    public MessengerDirectMessage(String messageDestinationUsername, Message message, String remoteAddress, ZonedDateTime date, String messageId) {
        setMessageDestinationUsername(messageDestinationUsername);
        setMessage(message);
        setRemoteAddress(remoteAddress);
        setDate(date);
        setMessageId(messageId);
    }

    public Message getMessage() {
        return message;
    }

    public boolean isReply() {
        return reply;
    }

    public String getMessageDestinationUsername() {
        return messageDestinationUsername;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setReply(boolean reply) {
        this.reply = reply;
    }

    public void setMessageDestinationUsername(String messageDestinationUsername) {
        this.messageDestinationUsername = messageDestinationUsername;
    }

    @Override
    public String toString() {
        return "MessengerDirectMessage{" +
                "message=" + message +
                ", reply=" + reply +
                ", messageDestinationUsername='" + messageDestinationUsername + '\'' +
                '}';
    }
}
