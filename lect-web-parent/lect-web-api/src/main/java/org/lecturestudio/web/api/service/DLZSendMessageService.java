package org.lecturestudio.web.api.service;

import org.lecturestudio.web.api.model.DLZPushMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class DLZSendMessageService {
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
