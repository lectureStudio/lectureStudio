package org.lecturestudio.web.api.service;

import org.lecturestudio.web.api.model.DLZJoinedRooms;
import org.lecturestudio.web.api.model.DLZRoom;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Schröter
 * Manages the dlz room membership request
 */
public class DLZRoomService {

    /**
     * Fetches a List of joined DLZrooms
     * @return List of DLZ_Rooms
     */
    public static List<DLZRoom> getRooms() {
        String domain = "https://chat.etit.tu-darmstadt.de";
        List<DLZRoom> rooms = new ArrayList<DLZRoom>();

        try {
            DLZWebService matrixClient = new DLZWebService(new URI(domain));
            DLZJoinedRooms roomsId = matrixClient.getRoomClient().getJoinedRooms();
            for (int i = 0; i < roomsId.getRoomIds().size(); i++) {
                try{
                    DLZJoinedRooms aliases = matrixClient.getRoomClient().getRoomAliases(roomsId.getRoomIds().get(i));
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
