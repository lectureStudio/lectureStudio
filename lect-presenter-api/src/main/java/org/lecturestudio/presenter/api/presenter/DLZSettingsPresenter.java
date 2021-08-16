package org.lecturestudio.presenter.api.presenter;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DLZSettingsPresenter extends Presenter<DLZSettingsView> {

    private DLZMessageService messageservice;
    private ScheduledExecutorService service; //Service for requesting Messages
    private URI uri;

    @Inject
    DLZSettingsPresenter(ApplicationContext context, DLZSettingsView view) {
        super(context, view);
    }

    @Override
    public void initialize() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
        config.setDlzAccessToken(System.getProperty("dlz.token"));


        try{
            uri = new URI("https://chat.etit.tu-darmstadt.de");
        }catch (URISyntaxException e){
            e.printStackTrace();
        }


        messageservice = new DLZMessageService(uri);
        service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(() -> {

        try {

            if (messageservice.hasNewMessages(config.getDlzRoom().getId())) {
                List<DLZMessage> messages = messageservice.getNewMessages();

                for (var message : messages) {
                    if(message.type == "m.text") {
                        String text = message.message;

                        MessengerMessage messengerMessage = new MessengerMessage();
                        messengerMessage.setDate(new Date());
                        messengerMessage.setMessage(new Message(text));
                        messengerMessage.setRemoteAddress(message.sender);

                        context.getEventBus().post(messengerMessage);

                        showNotificationPopup("New DLZ Message", text);
                    }
                    //TODO Ablauf für Bild einfügen
                }
            }
        }
        catch (NullPointerException e){
            //do nothing in this case
            //No Room selected
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
        view.setDLZAccessToken(config.DLZAccessToken());
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

    public void reset() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
    }

}