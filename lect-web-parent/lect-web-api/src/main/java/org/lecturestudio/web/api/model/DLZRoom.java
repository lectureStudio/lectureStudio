package org.lecturestudio.web.api.model;

public class DLZRoom {
    private String id; //id des Raums
    private String name;//Anzeigename des Raums

    public DLZRoom(){

    }

    public DLZRoom(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Room [id=" + id + "Name" + name + "]";
    }
}
