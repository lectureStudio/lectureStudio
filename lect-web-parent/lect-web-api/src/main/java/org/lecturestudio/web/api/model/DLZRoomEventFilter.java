package org.lecturestudio.web.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 * @author Alex Andres, Michel Heidkamp
 * Is used in the reciving of Messages from a Matrix client
 * used in Interface Roomservice
 *
 */
public class DLZRoomEventFilter {

    private final List<String> types = new ArrayList<>();


    public List<String> getTypes() {
        return types;
    }

    @Override
    public String toString() {
        Jsonb jsonb = JsonbBuilder.create();

        return jsonb.toJson(this);
    }
}
