package org.lecturestudio.presenter.api.presenter;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.DLZSettingsView;

import org.lecturestudio.web.api.client.RoomService;
import org.lecturestudio.web.api.model.Room;
import org.lecturestudio.web.api.service.DLZRoomService;


import javax.inject.Inject;
import java.util.List;

public class DLZSettingsPresenter extends Presenter<DLZSettingsView> {

    @Inject
    DLZSettingsPresenter(ApplicationContext context, DLZSettingsView view) {
        super(context, view);
    }

    public List<Room> setRoomList() {
        List<Room> rooms = null;
        try {
            rooms = DLZRoomService.getRooms();
        } catch (Exception e) {
            handleException(e, "", e.getMessage());
        }
        return rooms;
    }


    @Override
    public void initialize() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

        view.setRoom(config.dlzRoomProperty());
        view.setRooms(setRoomList());
        view.setOnClose(this::close);
        view.setOnReset(this::reset);
    }

    public void reset() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();


    }

}