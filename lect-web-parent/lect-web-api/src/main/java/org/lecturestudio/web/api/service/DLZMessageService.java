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

    String roomId;
    RoomService roomClient;
    RoomEventFilter filter;
    chunk chunks;
    List<DLZMessage> messages;
    String start; //Start für den nächsten Aufruf
    ArrayList<String> messageIDs; //saves IDs of allready recived messages

    public DLZMessageService(URI uri, String roomId){
        filter = new RoomEventFilter();
        filter.getTypes().add("m.room.message");
        roomClient = new DLZService(uri).getRoomClient();
        this.roomId = roomId;
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
   /** public List<DLZMessage> getNewMessages(){
        List<DLZMessage> list = new ArrayList<>();
        DLZMessage mes = new DLZMessage();
        mes.message = "testo";
        list.add(mes);
        DLZMessage meso = new DLZMessage();
        meso.message = "x";
        list.add(meso);
        return list;
    }**/
    public boolean hasNewMessages(){

        messages = new ArrayList<DLZMessage>();
        if(start == null) {
            chunks = roomClient.getMessages(roomId, "b", 5, filter);
            start = chunks.start;
        }else {
            chunks = roomClient.getMessages(roomId, "b", 3, filter);
            start = chunks.start;
        }

        for(MatrixMessage message : chunks.chunk){
        //chunks.chunk.forEach(message -> {
            if(!isReceived(message.event_id)) {
                messageIDs.add(message.event_id);
                DLZMessage nmessage = new DLZMessage();
                nmessage.message = message.content.body;
                nmessage.senderId = message.sender;
                //nmessage.sender = abfragen
                nmessage.url = message.content.url;
                messages.add(nmessage);
            }
        }
        //);
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
