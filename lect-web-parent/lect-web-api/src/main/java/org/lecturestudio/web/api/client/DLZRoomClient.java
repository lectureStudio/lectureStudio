package org.lecturestudio.web.api.client;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.web.api.model.Room;

import java.util.List;

public class DLZRoomClient {
    private static List<Room> raum;

    public static ObjectProperty<Room> defaultRoom = new ObjectProperty<Room>();

    public static List<Room> getRooms(){
    raum.add(new Room("1", "Raum1"));
    raum.add(new Room("2","Raum2"));
    return raum;
    }

}
