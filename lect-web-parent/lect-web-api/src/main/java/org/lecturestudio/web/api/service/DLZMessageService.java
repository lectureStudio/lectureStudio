package org.lecturestudio.web.api.service;

import io.netty.buffer.search.KmpSearchProcessorFactory;
import org.lecturestudio.web.api.client.RoomService;
import org.lecturestudio.web.api.model.*;

import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class DLZMessageService {

    String roomId;
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
}
