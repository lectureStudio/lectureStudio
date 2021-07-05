package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.web.api.model.Room;

import java.util.List;

public interface DLZSettingsView extends SettingsBaseView {
    void setRoom(ObjectProperty<Room> room);
    void setRooms(List<Room> rooms);
    void setDLZAccessToken(ObjectProperty<String> DLZAccessToken);
}
