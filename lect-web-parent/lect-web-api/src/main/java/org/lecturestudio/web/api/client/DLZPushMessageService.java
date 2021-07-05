package org.lecturestudio.web.api.client;


import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.lecturestudio.web.api.data.JsonbContextResolver;
import org.lecturestudio.web.api.filter.AuthorizationFilter;
import org.lecturestudio.web.api.filter.LoggingFilter;
import org.lecturestudio.web.api.filter.MatrixExceptionFilter;
import org.lecturestudio.web.api.model.DLZPushMessage;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.UUID;

@Path("/_matrix")
@RegisterProviders({
        @RegisterProvider(JsonbContextResolver.class),
        @RegisterProvider(AuthorizationFilter.class),
        @RegisterProvider(LoggingFilter.class),
        @RegisterProvider(MatrixExceptionFilter.class),
})
public interface DLZPushMessageService {

    @PUT
    @Path("/client/r0/rooms/{roomId}/send/{eventType}/{txnId}")
    void SendMessage(@PathParam("roomId") String roomId, @PathParam("eventType") String eventType, @PathParam("txnId") UUID txnId, DLZPushMessage message);
}
