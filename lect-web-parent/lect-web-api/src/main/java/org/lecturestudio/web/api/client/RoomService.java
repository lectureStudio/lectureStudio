package org.lecturestudio.web.api.client;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.web.api.data.JsonbContextResolver;
import org.lecturestudio.web.api.exception.MatrixException;
import org.lecturestudio.web.api.filter.AuthorizationFilter;
import org.lecturestudio.web.api.filter.LoggingFilter;
import org.lecturestudio.web.api.filter.MatrixExceptionFilter;
import org.lecturestudio.web.api.model.*;


@Path("/_matrix/client/r0")
@RegisterProviders({
        @RegisterProvider(JsonbContextResolver.class),
        @RegisterProvider(AuthorizationFilter.class),
        @RegisterProvider(LoggingFilter.class),
        @RegisterProvider(MatrixExceptionFilter.class),
})
public interface RoomService {

    public static ObjectProperty<Room> defaultRoom = new ObjectProperty<Room>();

    /**
     * Gets a list of the user's current rooms in which the user has {@code joined}
     * membership.
     *
     * @return A list of room IDs the user has joined.
     */
    @GET
    @Path("/joined_rooms")
    JoinedRooms getJoinedRooms();

    /**
     * Get the user's display name belonging to the specified user ID.
     *
     * @param userId The user's ID whose display name to get.
     *
     * @return The user's display name if set.
     */
    @GET
    @Path("/profile/{userId}/displayname")
    UserName getDisplayName(@PathParam("userId") String userId) throws MatrixException;


    /**
     * Get the combined profile information of a user with to the specified user ID.
     *
     * @param userId The user's ID whose profile information to get.
     *
     * @return The combined profile information for a user.
     */
    @GET
    @Path("/profile/{userId}")
    UserProfile getProfile(@PathParam("userId") String userId) throws MatrixException;


    /**
     *
     * @param roomId
     * @return Name pf the given RoomID
     */
    @GET
    @Path("/rooms/{roomId}/state/m.room.name/")
    JoinedRooms getRoomAliases(@PathParam("roomId") String roomId);


    /**
     *
     * @param roomId the id of the room
     * @param dir in which direction the Messages should be given
     * @param limit how many messages should be received
     * @param filter a filter to filter the messages, often "m.room.message" filter
     * @return the messages of the room as a List in the class chunk
     *
     * Receives the Messages of the given Room
     */
    @GET
    @Path("/rooms/{roomId}/messages")
    chunk getMessages(@PathParam("roomId") String roomId, @QueryParam("dir") String dir,
                                    @QueryParam("limit") int limit, @QueryParam("filter") RoomEventFilter filter);
    @GET
    @Path("/rooms/{roomId}/messages")
    chunk getMessages(@PathParam("roomId") String roomId, @QueryParam("from") String from,@QueryParam("dir") String dir,
                                    @QueryParam("limit") int limit, @QueryParam("filter") RoomEventFilter filter);

}