package org.lecturestudio.presenter.api.presenter;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.DLZSettingsView;

import org.lecturestudio.web.api.client.RoomService;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.model.DLZMessage;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.model.Room;
import org.lecturestudio.web.api.service.DLZMessageService;
import org.lecturestudio.web.api.service.DLZRoomService;


import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DLZSettingsPresenter extends Presenter<DLZSettingsView> {

    private ScheduledExecutorService service; //Service for requesting Messages

    @Inject
    DLZSettingsPresenter(ApplicationContext context, DLZSettingsView view) {
        super(context, view);
    }

    @Override
    public void initialize() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
        service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(() -> {
        // TODO
        try {
            if (DLZRoomService.hasNewMessages()) {
                List<DLZMessage> messages = DLZRoomService.getNewMessages();

                for (var message : messages) {
                    String text = message.message;

                    MessengerMessage messengerMessage = new MessengerMessage();
                    messengerMessage.setDate(new Date());
                    messengerMessage.setMessage(new Message(text));
                    messengerMessage.setRemoteAddress(message.sender);

                    context.getEventBus().post(messengerMessage);

                    showNotificationPopup("New DLZ Message", text);
                }
            }
        }
        catch (Exception e) {
            handleException(e, "Get DLZ messages failed", "generic.error");
            service.shutdownNow();
        }
    }, 0, 3, TimeUnit.SECONDS);

        view.setRoom(config.dlzRoomProperty());
        view.setRooms(setRoomList());
        view.setOnClose(this::close);
        view.setOnReset(this::reset);
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


    /**@Override
    public void initialize() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

        view.setRoom(config.dlzRoomProperty());
        view.setRooms(setRoomList());
        view.setOnClose(this::close);
        view.setOnReset(this::reset);
    }**/

    public void reset() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();


    }

}