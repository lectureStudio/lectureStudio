package org.lecturestudio.web.api.model;
import java.util.StringJoiner;

/**
 * @author Michel Heidkamp
 * Class is used in the recieving of messages from a Room in the Matrix API via JSON
 */
public class content {


    public String body;
    public String msgtype;
    public String url;

    @Override
    public String toString() {
        return new StringJoiner(", ", content.class.getSimpleName() + "[", "]")
                .add("body='" + body + "'").add("msgtype='" + msgtype + "'")
                .add("url='" + url + "'").toString();
    }
}
