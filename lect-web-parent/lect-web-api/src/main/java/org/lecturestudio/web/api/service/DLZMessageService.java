package org.lecturestudio.web.api.service;

import org.lecturestudio.web.api.client.DLZRoomService;
import org.lecturestudio.web.api.model.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michel Heidkamp
 * manages Messages for a given room
 */
public class DLZMessageService {


    static public boolean active = false; // changes if DLZis active
    DLZRoomService roomClient;
    DLZRoomEventFilter filter;
    chunk chunks;
    List<DLZMessage> messages;
    String start; //Start für den nächsten Aufruf
    ArrayList<String> messageIDs; //saves IDs of allready recived messages


    /**
     * initialises the Service for receiving messages
     * @param uri The URL for the DLZ Server
     */
    public DLZMessageService(URI uri){
        filter = new DLZRoomEventFilter();
        filter.getTypes().add("m.room.message");
        roomClient = new DLZWebService(uri).getRoomClient();
        messageIDs = new ArrayList<>();
    }

    /**
     * giving the newly recieved messages
     *
     * @return boolean for r
     */
    public List<DLZMessage> getNewMessages(){
       List<DLZMessage> out = messages.subList(0, messages.size());
       messages = null;
       return out;
    }

    /**
     * checks for new Messages through the Matrix API, loads them in a puffer Cache
     * @param roomId room for which messages should be received
     * @return boolean, if there are new messages
     */
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
