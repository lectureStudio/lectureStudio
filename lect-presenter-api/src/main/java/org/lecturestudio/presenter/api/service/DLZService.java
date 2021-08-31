package org.lecturestudio.presenter.api.service;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.presenter.command.NotificationPopupCommand;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.DLZSettingsView;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.model.DLZMessage;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.service.DLZMessageService;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class DLZService {
    private DLZMessageService messageservice;
    private ScheduledExecutorService service; //Service for requesting Messages
    private URI uri;
    private ApplicationContext context;

    @Inject
    public DLZService(ApplicationContext context) {
        this.context = context;
    }

    public void start(){
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
        try{
            uri = new URI("https://chat.etit.tu-darmstadt.de");
        }catch (URISyntaxException e){
            e.printStackTrace();
        }


        messageservice = new DLZMessageService(uri);
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {

            System.out.println("test");

            try {
                if( config.getDlzRoom().getId() != null) {
                    if (messageservice.hasNewMessages(config.getDlzRoom().getId())) {
                        List<DLZMessage> messages = messageservice.getNewMessages();

                        for (var message : messages) {
                            if(message.getType().equals("m.text")) {
                            String text = message.message;

                            MessengerMessage messengerMessage = new MessengerMessage();
                            messengerMessage.setDate(new Date());
                            messengerMessage.setMessage(new Message(text));
                            messengerMessage.setRemoteAddress(message.sender);

                            context.getEventBus().post(messengerMessage);

                            showNotificationPopup("New DLZ Message", text);
                            }

                    if(message.getType().equals("m.image")){
                        InputStream test;
                        String url = message.url;

                        String[] split = url.split("/");

                        String mediaId = split[3];
                        test = org.lecturestudio.web.api.service.DLZPictureService.getPic(mediaId);
                        BufferedImage imBuff = ImageIO.read(test);
                        System.out.println(imBuff);


                        MessengerMessage messengerMessage = new MessengerMessage();
                        messengerMessage.setDate(new Date());
                        messengerMessage.setImage(imBuff);
                        messengerMessage.setMessage(new Message("Test"));

                        context.getEventBus().post(messengerMessage);

                        //showNotificationPopup("DLZ Bild", text);
                    }
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                //handleException(e, "Get DLZ messages failed", "generic.error");
                //service.shutdownNow();
            }

        }, 0, 3, TimeUnit.SECONDS);

    }

    public void stop(){
        service.shutdown();
    }

    final protected void showNotificationPopup(String title, String message) {
        if (context.getDictionary().contains(title)) {
            title = context.getDictionary().get(title);
        }
        if (context.getDictionary().contains(message)) {
            message = context.getDictionary().get(message);
        }

        context.getEventBus().post(new NotificationPopupCommand(Position.TOP_RIGHT, title, message));
    }
}
