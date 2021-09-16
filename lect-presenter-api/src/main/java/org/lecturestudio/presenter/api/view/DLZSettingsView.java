package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.web.api.model.DLZRoom;

import java.util.List;

/**
 * Represents connection between logical part and graphical part of the dlz settings
 */
public interface DLZSettingsView extends SettingsBaseView {

    void setDLZRoom(ObjectProperty<DLZRoom> room);

    void setDLZRooms(List<DLZRoom> rooms);

    void setDLZAccessToken(ObjectProperty<String> DLZAccessToken);

    void refreshDLZAccessToken(Action action);

    String getDLZAccessTokenInField();
}
