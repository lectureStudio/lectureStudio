package org.lecturestudio.web.api.model;

import java.util.List;

/**
 * Class for getting RoomIds and room names
 *
 * @author Michel Heidkamp
 */
public class DLZJoinedRooms {
    private List<String> joined_rooms;
    private String name;


    /**
     * Getter for RoomIds
     * @return roomIDs
     */
    public List<String> getRoomIds() {
        return joined_rooms;
    }

    /**
     * Getter Method for clear RoomNames
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
