package org.lecturestudio.web.api.model;

import java.util.List;

/**
 * @author Michel Heidkamp
 * Class for getting RoomIds and Room Names
 */
public class DLZJoinedRooms {
    private List<String> joined_rooms;
    private String name;


    /**
     * getter for RoomIds
     * @return roomIDs
     */
    public List<String> getRoomIds() {
        return joined_rooms;
    }

    /**
     * getter Method for clear RoomNames
     * @return Room Name
     */
    public String getRoomName(){
        return name;
    }

    @Override
    public String toString() {
        return "JoinedRooms [joined_rooms=" + joined_rooms + "]";
    }
}
