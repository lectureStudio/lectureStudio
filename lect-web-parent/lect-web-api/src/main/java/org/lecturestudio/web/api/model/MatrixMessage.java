package org.lecturestudio.web.api.model;

import java.util.StringJoiner;

/**
 * @author Michel Heidkamp
 * Class is used in the recieving of Messages from a Room in the Matrix API via JSON
 */
public class MatrixMessage {
    public content content;
    public String sender;

    @Override
    public String toString() {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]")
                .add("content=" + content).add("sender='" + sender + "'")
                .toString();
    }
}
