package org.lecturestudio.web.api.service;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.lecturestudio.web.api.client.RoomService;

import java.net.URI;

public class DLZService {

    private final URI apiUri;


    public DLZService(URI apiUri) {
        this.apiUri = apiUri;
    }


    public RoomService getRoomClient() {
        return RestClientBuilder.newBuilder()
                .baseUri(apiUri)
                .build(RoomService.class);
    }

}
