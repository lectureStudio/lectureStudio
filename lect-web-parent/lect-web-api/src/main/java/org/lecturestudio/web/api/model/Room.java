package org.lecturestudio.web.api.model;

public class Room {
    private String id; //id des Raums
    private String name;//Anzeigename des Raums

    public Room(){

    }

    public Room(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Room [id=" + id + "Name" + name + "]";
    }
}
