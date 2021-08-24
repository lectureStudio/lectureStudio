package org.lecturestudio.presenter.api.presenter;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.DLZSettingsView;

import org.lecturestudio.web.api.client.RoomService;
import org.lecturestudio.web.api.exception.MatrixUnauthorizedException;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.model.DLZMessage;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.model.DLZRoom;
import org.lecturestudio.web.api.service.DLZMessageService;
import org.lecturestudio.web.api.service.DLZRoomService;


import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DLZSettingsPresenter extends Presenter<DLZSettingsView> {

    @Inject
    DLZSettingsPresenter(ApplicationContext context, DLZSettingsView view) {
        super(context, view);
    }

    @Override
    public void initialize() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
        org.lecturestudio.web.api.filter.AuthorizationFilter.setToken(config.getDLZAccessToken());


        System.out.println(config.getDlzRoom());
        view.setDLZAccessToken(config.DLZAccessToken());
        try {
           view.setRoom(config.dlzRoomProperty());
           view.setRooms(setRoomList());
        }
        catch (Exception e){
            config.setDlzRoom(null);
            view.setRoom(null);
        }
        view.setOnClose(this::close);
        view.setOnReset(this::reset);
        view.refreshaccesstoken(this::saveAccessToken);
    }

    public List<DLZRoom> setRoomList() {
        List<DLZRoom> rooms = List.of();
        try {
            rooms = DLZRoomService.getRooms();
        } catch (MatrixUnauthorizedException e) {
            PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
            config.setDlzRoom(null);
            handleException(e, "", e.getMessage());
        }
        return rooms;
    }

    public void saveAccessToken(){
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
        String token = view.getDLZAccessTokenInField();
        config.setDLZAccessToken(token);
        org.lecturestudio.web.api.filter.AuthorizationFilter.setToken(config.getDLZAccessToken());
        view.setRooms(setRoomList());
    }

    public void reset() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
    }

}