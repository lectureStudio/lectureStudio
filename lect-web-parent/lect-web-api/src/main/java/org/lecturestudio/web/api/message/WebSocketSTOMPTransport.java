package org.lecturestudio.web.api.message;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.web.api.data.bind.*;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.websocket.WebSocketHeaderProvider;
import org.lecturestudio.web.api.websocket.stomp.MessengerStompSessionHandler;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class WebSocketSTOMPTransport extends ExecutableBase implements MessageTransport {

    private final Map<Class<? extends WebMessage>, List<Consumer<WebMessage>>> listenerMap;

    private final Course course;

    private final ServiceParameters serviceParameters;

    private final WebSocketHeaderProvider headerProvider;

    private WebSocketStompClient stompClient;

    private Jsonb jsonb;

    private StompSession session;

    public WebSocketSTOMPTransport(ServiceParameters serviceParameters,
                                   WebSocketHeaderProvider headerProvider,
                                   Course course) {
        this.serviceParameters = serviceParameters;
        this.headerProvider = headerProvider;
        this.course = course;
        this.listenerMap = new HashMap<>();
    }

    @Override
    public <T extends WebMessage> void addListener(Class<T> cls, Consumer<T> listener) {
        List<Consumer<WebMessage>> consumerList = listenerMap.get(cls);

        if (isNull(consumerList)) {
            consumerList = new ArrayList<>();

            listenerMap.put(cls, consumerList);
        }

        consumerList.add((Consumer<WebMessage>) listener);
    }

    @Override
    public <T extends WebMessage> void removeListener(Class<T> cls, Consumer<T> listener) {
        List<Consumer<WebMessage>> consumerList = listenerMap.get(cls);

        if (nonNull(consumerList)) {
            consumerList.remove(listener);
        }
    }

    @Override
    protected void initInternal() throws ExecutableException {
        JsonbConfig jsonbConfig = JsonConfigProvider.createConfig();
        jsonbConfig.withAdapters(
                new CourseParticipantMessageAdapter(),
                new MessengerMessageAdapter(),
                new QuizAnswerMessageAdapter(),
                new SpeechMessageAdapter()
        );

        jsonb = JsonbBuilder.create(jsonbConfig);
    }

    @Override
    protected void startInternal() throws ExecutableException {
        if (isNull(this.stompClient)) {
            StandardWebSocketClient simpleWebSocketClient = new StandardWebSocketClient();


            List<Transport> transports = new ArrayList();
            transports.add(new org.springframework.web.socket.sockjs.client.WebSocketTransport(simpleWebSocketClient));

            WebSocketClient webSocketClient = new SockJsClient(transports);
            WebSocketHttpHeaders headers = headerProvider.getHeaders();


            MessengerStompSessionHandler sessionHandler = new MessengerStompSessionHandler(this.course, this.jsonb, this.listenerMap);


            this.stompClient = new WebSocketStompClient(webSocketClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            ListenableFuture<StompSession> listenableSession = stompClient.connect(this.serviceParameters.getUrl(), headers, sessionHandler);
            try {
                this.session = listenableSession.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (! this.stompClient.isRunning()) {
            this.stompClient.start();
        }
    }

    @Override
    protected void stopInternal() throws ExecutableException {
        if (this.stompClient.isRunning()) {
            this.stompClient.stop();
        }
    }

    @Override
    protected void destroyInternal() throws ExecutableException {
        //no-op
    }

    public void sendMessage(MessengerMessage message) {
        if (super.started()) {
            if (nonNull(this.session)) {
                String messageAsJson = jsonb.toJson(message, message.getClass());
                StompHeaders headers = new StompHeaders();
                headerProvider.addHeadersForStomp(headers);
                headers.setDestination("/app/publisher/message/" +  this.course.getId());
                this.session.send(headers, messageAsJson);
            }
        }
    }
}
