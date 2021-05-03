package org.lecturestudio.broadcast.service;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSource;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomService;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.service.JwtRequestFilter;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageResourceTest extends ResourceTest {

	static String classroomId;

	static String serviceId;


	@BeforeAll
	static void setup() {
		Classroom classroom = new Classroom();
		classroom.setName("Test Message Classroom");

		classroomId = given()
				.port(ConfigProvider.getConfig()
						.getValue("quarkus.http.test-port", Integer.class))
				.headers(headers)
				.contentType(ContentType.JSON)
				.body(classroom, ObjectMapperType.JSONB)
		.when()
				.post("/api/classroom")
		.then()
				.statusCode(200)
				.extract().asString();
	}

	@AfterAll
	static void shutdown() {
		given()
				.port(ConfigProvider.getConfig()
				.getValue("quarkus.http.test-port", Integer.class))
				.headers(headers)
		.when()
				.delete("/api/classroom/" + classroomId)
		.then()
				.statusCode(200);
	}

	@Test
	@Order(1)
	void testStartService() {
		// Test unauthorized.
		given().when()
				.post("/api/message/start/" + classroomId)
		.then()
				.statusCode(401);

		// Start service.
		given().when()
				.headers(headers)
				.post("/api/message/start/" + classroomId)
		.then()
				.statusCode(200);

		// Make sure the service has been started.
		Classroom[] classrooms = given().when()
				.get("/api/classroom/list")
		.then()
				.statusCode(200)
				.body("$.size()", is(1))
				.extract().as(Classroom[].class, ObjectMapperType.JSONB);

		ClassroomService service = classrooms[0].getServices().stream()
				.findFirst().orElse(null);

		// Make sure the service is active.
		assertNotNull(service);
		assertNotNull(service.getServiceId());

		serviceId = service.getServiceId();
	}

	@Test
	@Order(2)
	void testReceiveMessage() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicInteger errors = new AtomicInteger(0);
		final AtomicReference<MessengerMessage> messageRef = new AtomicReference<>();

		// Subscribe for SSE.
		Client client = ClientBuilder.newClient()
				.register(JwtRequestFilter.class);
		WebTarget target = client.target(getURL("api/message/subscribe", serviceId));

		try (SseEventSource eventSource = SseEventSource.target(target).build()) {
			eventSource.register(sseEvent -> {
				MessengerMessage message = sseEvent.readData(MessengerMessage.class,
						MediaType.APPLICATION_JSON_TYPE);

				messageRef.set(message);

				latch.countDown();
			}, throwable -> errors.incrementAndGet());
			eventSource.open();

			// Send a message to the service.
			Message message = new Message("Hello World");
			message.setServiceId(serviceId);

			given()
					.contentType(ContentType.JSON)
					.body(message, ObjectMapperType.JSONB)
			.when()
					.post("/api/message/post")
			.then()
					.statusCode(200);

			assertTrue(latch.await(5, TimeUnit.SECONDS));
			assertEquals(0, errors.get());
			assertNotNull(messageRef.get());

			Message recvMessage = messageRef.get().getMessage();

			assertNotNull(recvMessage);
			assertEquals(message.getText(), recvMessage.getText());
			assertEquals(serviceId, recvMessage.getServiceId());
		}
		finally {
			client.close();
		}
	}

	@Test
	@Order(3)
	void testStopService() {
		// Test unauthorized.
		given().when()
				.post("/api/message/stop/" + classroomId + "/" + serviceId)
		.then()
				.statusCode(401);

		// Stop the service.
		given().when()
				.headers(headers)
				.post("/api/message/stop/" + classroomId + "/" + serviceId)
		.then()
				.statusCode(200);

		// Make sure the service has been deleted.
		Classroom[] classrooms = given().when()
				.get("/api/classroom/list")
		.then()
				.statusCode(200)
				.body("$.size()", is(1))
				.extract().as(Classroom[].class, ObjectMapperType.JSONB);

		assertNotNull(classrooms[0]);
		assertTrue(classrooms[0].getServices().isEmpty());
	}

	@Test
	@Order(4)
	void testSubscribeFail() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicInteger errors = new AtomicInteger(0);

		// Subscribe for SSE.
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(getURL("api/message/subscribe", "no-id"));

		try (SseEventSource eventSource = SseEventSource.target(target).build()) {
			eventSource.register(sseEvent -> latch.countDown(),
					throwable -> errors.incrementAndGet());
			eventSource.open();

			assertFalse(latch.await(1, TimeUnit.SECONDS));
			assertEquals(1, errors.get());
		}
		finally {
			client.close();
		}
	}
}
