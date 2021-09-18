package org.lecturestudio.web.api.model;

/**
 * An easy class to represent each Message send in a MatrixRoom
 *
 * @author Michel Heidkamp
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
