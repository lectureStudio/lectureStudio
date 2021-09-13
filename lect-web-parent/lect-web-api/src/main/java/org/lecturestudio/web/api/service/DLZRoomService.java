package org.lecturestudio.web.api.service;

import org.lecturestudio.web.api.model.JoinedRooms;
import org.lecturestudio.web.api.model.DLZRoom;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * manages the dlz room membership request
 */
public class DLZRoomService {

    public static List<DLZRoom> getRooms() {
        String domain = "https://chat.etit.tu-darmstadt.de";
        List<DLZRoom> rooms = new ArrayList<DLZRoom>();

        try {
            DLZWebService matrixClient = new DLZWebService(new URI(domain));
            JoinedRooms roomsId = matrixClient.getRoomClient().getJoinedRooms();
            for (int i = 0; i < roomsId.getRoomIds().size(); i++) {
                try{
                    JoinedRooms aliases = matrixClient.getRoomClient().getRoomAliases(roomsId.getRoomIds().get(i));
                    rooms.add(new DLZRoom(roomsId.getRoomIds().get(i), aliases.getRoomName()));
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
}
