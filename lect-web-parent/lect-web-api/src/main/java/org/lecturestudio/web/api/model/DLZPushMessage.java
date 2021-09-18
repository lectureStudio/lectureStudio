package org.lecturestudio.web.api.model;


/**
 * Class which represents an outgoing push message
 *
 * @author Daniel Schröter
 */
public class DLZPushMessage {
    private String msgtype;

    private String body;

    /**
     * Initialises a new DLZPushMessage
     * @param msgtype type of the message
     * @param body content of the message
     */
    public DLZPushMessage(String msgtype, String body) {
        this.msgtype = msgtype;
        this.body = body;
    }
    @Override
    public String toString(){
        return "Nachricht [body=" + body + "]";
    }
}
