package org.lecturestudio.web.api.model;

/**
 * @author Michel Heidkamp
 * an easy class to represent each Message send in a MatrixRoom
 */
public class DLZMessage {
    public String message;
    public String sender;
    public String senderId;
    public String url;
    public String type;
    public long age;

    public String getType() {
        return type;
    }

}
