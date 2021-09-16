package org.lecturestudio.web.api.service;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.lecturestudio.web.api.client.DLZMessageService;
import org.lecturestudio.web.api.client.DLZRoomService;

import java.net.URI;

/**
 * @author Daniel Schröter, Michel Heidkamp
 * Class which creates the Restclient instances for the DLZ-Services
 */
public class DLZWebService {

    private final URI apiUri;


    /**
     * Initialises the DLZWebService
     * @param apiUri the URL of the Matrix API
     */
    public DLZWebService(URI apiUri) {
        this.apiUri = apiUri;
    }


    /**
     * Builds and returns a new DLZRoomService
     * @return the DLZRoomService
     */
    public DLZRoomService getRoomClient() {
        return RestClientBuilder.newBuilder()
                .baseUri(apiUri)
                .build(DLZRoomService.class);
    }

    /**
     * Builds and returns a new DLZMessageService
     * @return the DLZMessageService
     */
    public DLZMessageService getMessageClient(){
        return RestClientBuilder.newBuilder()
                .baseUri(apiUri)
                .build(DLZMessageService.class);
    }

}
