package org.lecturestudio.presenter.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.lecturestudio.web.api.exception.MatrixUnauthorizedException;
import org.lecturestudio.web.api.filter.AuthorizationFilter;
import org.lecturestudio.web.api.model.DLZJoinedRooms;
import org.lecturestudio.web.api.model.DLZPushMessage;
import org.lecturestudio.web.api.model.DLZRoomEventFilter;
import org.lecturestudio.web.api.model.UserName;
import org.lecturestudio.web.api.model.UserProfile;
import org.lecturestudio.web.api.service.DLZWebService;

/**
 * Basic DLZ web-service test.
 *
 * @author Daniel Schröter, Michel Heidkamp
 */
public class DLZWebServiceTest {

	static final String domain = "https://chat.etit.tu-darmstadt.de";

	DLZWebService dlzClient;

	String userId;

	@BeforeEach
	void setUp() throws URISyntaxException {
		AuthorizationFilter.setToken(System.getProperty("dlz.access.token"));

		userId = System.getProperty("dlz.user");
		dlzClient = new DLZWebService(new URI(domain));
	}

	@Test
	final void testUnauthorized() {
		AuthorizationFilter.setToken(null);

		assertThrows(MatrixUnauthorizedException.class, () -> {
			dlzClient.getRoomClient().getJoinedRooms();
		});
	}

	@Test
	final void testAuthorized() {
		assertDoesNotThrow(() -> {
			dlzClient.getRoomClient().getJoinedRooms();
		});
	}

	@Test
	final void testJoinedRooms() {
		assertNotNull(dlzClient.getRoomClient().getJoinedRooms());
	}

	@Test
	final void testRoomAlias() {
		DLZJoinedRooms roomsId = dlzClient.getRoomClient().getJoinedRooms();

		for (String roomId : roomsId.getRoomIds()) {
			assertDoesNotThrow(() -> {
				dlzClient.getRoomClient().getRoomAliases(roomId);
			});
		}
	}

	@Test
	final void testDisplayName() {
		UserName userName = dlzClient.getRoomClient().getDisplayName(userId);

		assertNotNull(userName);
		assertNotNull(userName.getDisplayName());
	}

	@Test
	final void testUserProfile() {
		UserProfile profile = dlzClient.getRoomClient().getProfile(userId);

		assertNotNull(profile);
		assertNotNull(profile.getDisplayName());
	}

	@Test
	final void testGetRoomMessages() {
		DLZRoomEventFilter filter = new DLZRoomEventFilter();
		filter.getTypes().add("m.room.message");

		DLZJoinedRooms roomsId = dlzClient.getRoomClient().getJoinedRooms();

		for (String roomId : roomsId.getRoomIds()) {
			assertDoesNotThrow(() -> {
				dlzClient.getRoomClient().getMessages(roomId, "b", 15, filter);
			});
		}
	}

	@Test
	final void testPushRoomMessage() {
		DLZRoomEventFilter filter = new DLZRoomEventFilter();
		filter.getTypes().add("m.room.message");

		String roomId = dlzClient.getRoomClient().getJoinedRooms().getRoomIds().get(0);

		assertNotNull(roomId);

		DLZPushMessage PushMessage = new DLZPushMessage("m.text", "hello..");
		String eventType = "m.room.message";

		assertDoesNotThrow(() -> {
			dlzClient.getMessageClient().SendMessage(roomId, eventType, UUID.randomUUID(), PushMessage);
		});
	}
}
