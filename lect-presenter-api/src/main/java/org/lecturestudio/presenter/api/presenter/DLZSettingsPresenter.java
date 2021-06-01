package org.lecturestudio.presenter.api.presenter;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.DLZSettingsView;
import org.lecturestudio.web.api.client.DLZRoomClient;

import org.lecturestudio.web.api.model.Room;

import javax.inject.Inject;

public class DLZSettingsPresenter extends Presenter<DLZSettingsView> {

   @Inject
    DLZSettingsPresenter(ApplicationContext context, DLZSettingsView view) {
        super(context, view);
    }

    @Override
    public void initialize() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

        view.setRoom(DLZRoomClient.defaultRoom);
        view.setRooms(DLZRoomClient.getRooms());
        view.setOnClose(this::close);
        view.setOnReset(this::reset);
    }



    public void reset() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();


    }

}