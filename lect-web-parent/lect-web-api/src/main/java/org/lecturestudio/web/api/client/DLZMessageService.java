package org.lecturestudio.web.api.client;


import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.lecturestudio.web.api.data.JsonbContextResolver;
import org.lecturestudio.web.api.filter.AuthorizationFilter;
import org.lecturestudio.web.api.filter.LoggingFilter;
import org.lecturestudio.web.api.filter.MatrixExceptionFilter;
import org.lecturestudio.web.api.model.DLZPushMessage;

import javax.ws.rs.*;
import java.io.InputStream;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

/**
 * @author Daniel Schröter
 * interface executing message sending and picture receiving dlz commands
 */
@Path("/_matrix")
@RegisterProviders({
        @RegisterProvider(JsonbContextResolver.class),
        @RegisterProvider(AuthorizationFilter.class),
        @RegisterProvider(LoggingFilter.class),
        @RegisterProvider(MatrixExceptionFilter.class),
})
public interface DLZMessageService {

    /**
     * sends a message into a given room
     *
     * @param roomId the id of the room
     * @param eventType type of the message which should be send
     * @param txnId random number to avoid redundancies
     * @param message the message which should be send
     */
    @PUT
    @Path("/client/r0/rooms/{roomId}/send/{eventType}/{txnId}")
    void SendMessage(@PathParam("roomId") String roomId, @PathParam("eventType") String eventType, @PathParam("txnId") UUID txnId, DLZPushMessage message);

    /**
     * requests the picture dedicated to a given mediaId
     *
     * @param serverName the url of the server
     * @param mediaId id of the requested picture
     * @return the requested picture
     */
    @GET
    @Path("/media/r0/download/{serverName}/{mediaId}")
    @Produces(APPLICATION_OCTET_STREAM)
    InputStream getPicture(@PathParam("serverName") String serverName, @PathParam("mediaId") String mediaId);
}
