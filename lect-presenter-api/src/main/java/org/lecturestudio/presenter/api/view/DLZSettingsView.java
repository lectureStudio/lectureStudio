package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.web.api.model.DLZRoom;

import java.util.List;

public interface DLZSettingsView extends SettingsBaseView {
    void setRoom(ObjectProperty<DLZRoom> room);
    void setRooms(List<DLZRoom> rooms);
    void setDLZAccessToken(ObjectProperty<String> DLZAccessToken);
    void refreshaccesstoken(Action action);
    String getDLZAccessTokenInField();
}
