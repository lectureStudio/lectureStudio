package org.lecturestudio.web.api.service;

import io.netty.buffer.search.KmpSearchProcessorFactory;
import org.lecturestudio.web.api.client.RoomService;
import org.lecturestudio.web.api.model.*;

import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michel Heidkamp
 * manages Messages for a given room
 */
public class DLZMessageService {

    static String roomId;
    RoomService roomClient;
    RoomEventFilter filter = new RoomEventFilter();
    chunk chunks;
    List<DLZMessage> messages;
    String start; //Start für den nächsten Aufruf

    public DLZMessageService(URI uri, String roomId){
        filter.getTypes().add("m.room.message");
        roomClient = new DLZService(uri).getRoomClient();
        this.roomId = roomId;
    }

    /**
     * Loads new Messages from the Matrix Server
     *
     * @return List of DLZMessages
     */
    public List<DLZMessage> getMessage(){
        if(start == null) {
            DLZMessageStructure msgs = roomClient.getMessages(roomId, "b", 50, filter);
            chunks = msgs.chunk;
            msgs.end = start;
        }else{
            DLZMessageStructure msgs = roomClient.getMessages(roomId,start, "b", 50, filter);
            chunks = msgs.chunk;
            msgs.end = start;
        }
        chunks.chunk.forEach(message -> {
            DLZMessage nmessage = new DLZMessage();
            nmessage.message = message.content.body;
            nmessage.senderId = message.sender;
            //nmessage.sender = abfragen
            nmessage.url = message.content.url;
            messages.add(nmessage);
        });
        return messages;
    }
    public static List<DLZMessage> getNewMessages(){
        List<DLZMessage> list = new ArrayList<>();
        DLZMessage mes = new DLZMessage();
        mes.message = "testo";
        list.add(mes);
        return list;
    }
    public static  boolean hasNewMessages(){
        return true;
    }
}
