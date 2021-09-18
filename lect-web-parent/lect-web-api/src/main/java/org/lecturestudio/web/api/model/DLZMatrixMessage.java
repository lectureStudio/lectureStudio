package org.lecturestudio.web.api.model;

import java.util.StringJoiner;

/**
 * Class is used in the recieving of messages from a Room in the Matrix API via JSON
 *
 * @author Michel Heidkamp
 */
public class DLZMatrixMessage {
    public content content;
    public String sender;
    public String event_id;
    public long origin_server_ts;


    @Override
    public String toString() {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]")
                .add("content=" + content).add("sender='" + sender + "'")
                .toString();
    }
}
