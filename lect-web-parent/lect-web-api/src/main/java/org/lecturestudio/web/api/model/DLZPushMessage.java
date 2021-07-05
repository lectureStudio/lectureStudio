package org.lecturestudio.web.api.model;

public class DLZPushMessage {
    private String msgtype;

    private String body;

    public DLZPushMessage(String msgtype, String body) {
        this.msgtype = msgtype;
        this.body = body;
    }
    @Override
    public String toString(){
        return "Nachricht [body=" + body + "]";
    }
}
