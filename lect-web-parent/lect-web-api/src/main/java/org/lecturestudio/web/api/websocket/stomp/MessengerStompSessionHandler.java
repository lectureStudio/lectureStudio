package org.lecturestudio.web.api.websocket.stomp;

import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.WebMessage;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.stream.model.Course;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;

import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

public class MessengerStompSessionHandler implements StompSessionHandler {

    private final Course course;

    private final Jsonb jsonb;

    private final Map<Class<? extends WebMessage>, List<Consumer<WebMessage>>> listenerMap;


    public MessengerStompSessionHandler(Course course, Jsonb jsonb, Map<Class<? extends WebMessage>, List<Consumer<WebMessage>>> listenerMap) {
        this.course = course;
        this.jsonb = jsonb;
        this.listenerMap = listenerMap;
    }

    private void handleMessage(WebMessage message) {
        Class<? extends WebMessage> cls = message.getClass();
        List<Consumer<WebMessage>> consumerList = listenerMap.get(cls);

        if (nonNull(consumerList)) {
            for (Consumer<WebMessage> listener : consumerList) {
                listener.accept(message);
            }
        }
    }

    @Override
    public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
        stompSession.subscribe("/topic/chat/" + course.getId(), this);
        stompSession.subscribe("/user/queue/chat/" + course.getId(), this);
    }

    @Override
    public void handleException(StompSession stompSession, StompCommand stompCommand, StompHeaders stompHeaders, byte[] bytes, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void handleTransportError(StompSession stompSession, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        return Object.class;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
        System.out.println("Hello");
        LinkedHashMap map = (LinkedHashMap) o;
        if (map.get("_type").equals("MessengerMessage")) {
            MessengerMessage message = new MessengerMessage(new Message((String) map.get("text")), (String) map.get("username"), ZonedDateTime.parse( (String) map.get("time")));
            message.setFirstName((String) map.get("firstName"));
            message.setFamilyName((String) map.get("familyName"));
            handleMessage(message);
        }
    }
}