package org.lecturestudio.web.api.service;

import org.lecturestudio.web.api.model.DLZMessage;
import org.lecturestudio.web.api.model.JoinedRooms;
import org.lecturestudio.web.api.model.DLZRoom;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class DLZRoomService {

    public static List<DLZRoom> getRooms() {
        String domain = "https://chat.etit.tu-darmstadt.de";
        List<DLZRoom> rooms = new ArrayList<DLZRoom>();

        try {
            DLZService matrixClient = new DLZService(new URI(domain));

            System.out.println();
            JoinedRooms roomsId = matrixClient.getRoomClient().getJoinedRooms();
            System.out.println(roomsId.getRoomIds());

            for (int i = 0; i < roomsId.getRoomIds().size(); i++) {
                try{
                    JoinedRooms aliases = matrixClient.getRoomClient().getRoomAliases(roomsId.getRoomIds().get(i));
                    rooms.add(new DLZRoom(roomsId.getRoomIds().get(i), aliases.getRoomName()));
                    System.out.println(aliases.getRoomName());
                }
                catch (Exception e){
                }
            }
        } catch (
                URISyntaxException e) {
            e.printStackTrace();
        }
        return rooms;
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
