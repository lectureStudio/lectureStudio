package org.lecturestudio.web.api.service;

import org.lecturestudio.web.api.model.DLZPushMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * @author Daniel Schröter
 * Class for sending messages to the Matrix API
 */
public class DLZSendMessageService {

    /**
     * Sends a message to a dedicated Matrix room
     * @param Message content of the message
     * @param roomId roomId of the dedicated room
     */
    public static void SendTextMessage(String Message, String roomId){
        String domain = "https://chat.etit.tu-darmstadt.de";

        try{
            DLZWebService matrixClient = new DLZWebService(new URI(domain));
            DLZPushMessage PushMessage = new DLZPushMessage("m.text", Message);
            String eventType = "m.room.message";
            UUID txnId = UUID.randomUUID();
            matrixClient.getMessageClient().SendMessage(roomId,eventType,txnId,PushMessage);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
