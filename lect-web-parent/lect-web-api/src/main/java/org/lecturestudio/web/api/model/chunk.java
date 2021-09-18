package org.lecturestudio.web.api.model;



import java.util.List;

/**
 * Class is used in the recieving of messages from a Room in the Matrix API via JSON
 *
 * @author Michel Heidkamp
 */
public class chunk {
    public List<DLZMatrixMessage> chunk;
    public String end;
    public String start;
}
