package org.lecturestudio.web.api.model;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DLZRoom dlzRoom = (DLZRoom) o;
        return Objects.equals(id, dlzRoom.id) && Objects.equals(name, dlzRoom.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Room [id=" + id + "Name" + name + "]";
    }
}
