package org.lecturestudio.web.api.model;

public class MessageAsReply extends Message {

    private String msgIdToReplyTo;


    public MessageAsReply() {
        this(null);
    }

    public MessageAsReply(String msgIdToReplyTo) {
        setMsgIdToReplyTo(msgIdToReplyTo);
    }

    public String getMsgIdToReplyTo() {
        return msgIdToReplyTo;
    }

    public void setMsgIdToReplyTo(final String msgIdToReplyTo) {
        this.msgIdToReplyTo = msgIdToReplyTo;
    }

    @Override
    public boolean isReply() {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + msgIdToReplyTo;
    }
}
