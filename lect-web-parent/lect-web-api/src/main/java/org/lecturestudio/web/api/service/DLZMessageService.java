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

    static public boolean active = false;
    RoomService roomClient;
    RoomEventFilter filter;
    chunk chunks;
    List<DLZMessage> messages;
    String start; //Start für den nächsten Aufruf
    ArrayList<String> messageIDs; //saves IDs of allready recived messages


    public DLZMessageService(URI uri){
        filter = new RoomEventFilter();
        filter.getTypes().add("m.room.message");
        roomClient = new DLZService(uri).getRoomClient();
        messageIDs = new ArrayList<>();
    }

    /**
     * Loads new Messages from the Matrix Server
     *
     * @return List of DLZMessages
     */
    public List<DLZMessage> getNewMessages(){
       List<DLZMessage> out = messages.subList(0, messages.size());
       messages = null;
       return out;
    }

    public boolean hasNewMessages(String roomId){
        if(active == false){
            return false;
        }
        messages = new ArrayList<DLZMessage>();
        if(start == null) {
            chunks = roomClient.getMessages(roomId, "b", 15, filter);
            start = chunks.start;
        }else {
            chunks = roomClient.getMessages(roomId, "b", 5, filter);
            start = chunks.start;
        }

        for(MatrixMessage message : chunks.chunk){
            if(!isReceived(message.event_id)) {
                messageIDs.add(message.event_id);
                DLZMessage nmessage = new DLZMessage();
                nmessage.message = message.content.body;
                nmessage.senderId = message.sender;
                UserProfile name = roomClient.getProfile(message.sender);
                nmessage.sender = name.getDisplayName();
                nmessage.type = message.content.msgtype;
                nmessage.age = message.origin_server_ts;

                nmessage.url = message.content.url;
                messages.add(0,nmessage);

            }
        }
        if(messages.size() != 0){
            return true;
        }else{
            return false;
        }

    }

    /**
     * tests if a message was already received before
     *
     * @param id id of the message
     * @return message already received
     */
    private boolean isReceived(String id){
        boolean received = false;
        for(String e : messageIDs){
            if(e.equals(id)){
                received = true;
            }
        }
        return received;
    }
}
