package org.lecturestudio.web.api.model;



import java.util.List;

/**
 * @author Michel Heidkamp
 * Class is used in the recieving of messages from a Room in the Matrix API via JSON
 */
public class chunk {
    public List<DLZMatrixMessage> chunk;
    public String end;
    public String start;
}
