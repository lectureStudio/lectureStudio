package org.lecturestudio.web.api.model;

import java.util.List;

public class JoinedRooms {
    private List<String> joined_rooms;
    private String name;


    public List<String> getRoomIds() {
        return joined_rooms;
    }
    public String getRoomName(){
        return name;
    }

    @Override
    public String toString() {
        return "JoinedRooms [joined_rooms=" + joined_rooms + "]";
    }
}
