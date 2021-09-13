package org.lecturestudio.web.api.service;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.lecturestudio.web.api.client.DLZMessageService;
import org.lecturestudio.web.api.client.DLZRoomService;

import java.net.URI;

public class DLZWebService {

    private final URI apiUri;


    public DLZWebService(URI apiUri) {
        this.apiUri = apiUri;
    }


    public DLZRoomService getRoomClient() {
        return RestClientBuilder.newBuilder()
                .baseUri(apiUri)
                .build(DLZRoomService.class);
    }

    public DLZMessageService getMessageClient(){
        return RestClientBuilder.newBuilder()
                .baseUri(apiUri)
                .build(DLZMessageService.class);
    }

}
