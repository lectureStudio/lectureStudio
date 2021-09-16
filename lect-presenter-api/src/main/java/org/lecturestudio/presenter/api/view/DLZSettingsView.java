package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.web.api.model.DLZRoom;

import java.util.List;

/**
 * @author Daniel Schröter
 * Represents connection between logical part and graphical part of the dlz settings
 */
public interface DLZSettingsView extends SettingsBaseView {

    /**
     * Method to initialize the current selected DLZRoom in the joined rooms combobox
     * @param room current DLZRoom
     */
    void setDLZRoom(ObjectProperty<DLZRoom> room);

    /**
     * Method to initialize the joined rooms combobox
     * @param rooms list containing the users joined DLZRooms
     */
    void setDLZRooms(List<DLZRoom> rooms);

    /**
     * Method to determine the DLZAccessToken
     * @param DLZAccessToken
     */
    void setDLZAccessToken(ObjectProperty<String> DLZAccessToken);

    /**
     * Method to refresh the inserted DLZAccessToken
     */
    void refreshDLZAccessToken(Action action);

    /**
     * Method which returns the inserted AccessToken
     * @return the AccessToken
     */
    String getDLZAccessTokenInField();
}
