package org.lecturestudio.broadcast.service;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.json.bind.Jsonb;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.lecturestudio.web.api.data.bind.JsonConfig;
import org.lecturestudio.web.api.filter.MinMaxRule;
import org.lecturestudio.web.api.filter.RegexRule;
import org.lecturestudio.web.api.message.QuizAnswerMessage;
import org.lecturestudio.web.api.model.AuthState;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomService;
import org.lecturestudio.web.api.model.HttpResourceFile;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.api.service.JwtRequestFilter;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuizResourceTest extends ResourceTest {

	static String classroomId;

	static String serviceId;


	@BeforeAll
	static void setup() {
		Classroom classroom = new Classroom();
		classroom.setName("Test Quiz Classroom");

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
		List<RegexRule> regexRules = new ArrayList<>();
		regexRules.add(new RegexRule("23"));
		regexRules.add(new RegexRule("42"));

		HttpResourceFile resourceFile = new HttpResourceFile("test.png", Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAPAAAADwBAMAAADMe/ShAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAHlBMVEX+hIRTAAD+ICC6AAD+XFzbAABrAAD+OTn+TU3///8sMZbGAAAAAWJLR0QJ8dml7AAAAAd0SU1FB+EICgA0N02Wn9oAAACWSURBVHja7c3BAAAhFAXAr5DCKqSQQgqrkELYnf/tAcwITFVohL5QicVisVgsFovFYrFYLBaLxWKxWCwWi8VisVgsFovFYrG4xTO0QjskFovFYrFYLBaLxWKxWCwWi8VisVgsFovFYrFYLBaLxeIe/6ETuiGxWCwWi8VisVgsFovFYrFYLBaLxWKxWCwWi8VisVgsFjcPBtPjtLnhxlEAAAAldEVYdGRhdGU6Y3JlYXRlADIwMTctMDgtMTBUMDA6NTI6NTUrMDA6MDD+3er1AAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE3LTA4LTEwVDAwOjUyOjU1KzAwOjAwj4BSSQAAAABJRU5ErkJggg=="));
		resourceFile.setModified(System.currentTimeMillis());

		List<HttpResourceFile> resources = new ArrayList<>();
		resources.add(resourceFile);

		Quiz quiz = new Quiz();
		quiz.setQuestion("What's happening?");
		quiz.setQuestionResources(resources);
		quiz.setOptions(List.of("a", "b", "c"));
		quiz.setRegexRules(regexRules);
		quiz.setQuizSet(Quiz.QuizSet.GENERIC);
		quiz.setType(Quiz.QuizType.NUMERIC);
		quiz.addInputRule(new MinMaxRule(0, 1, 0));
		quiz.addInputRule(new MinMaxRule(2, 3, 1));

		// Test unauthorized.
		given()
				.contentType(ContentType.JSON)
				.body(quiz, ObjectMapperType.JSONB)
		.when()
				.post("/api/quiz/start/" + classroomId)
		.then()
				.statusCode(401);

		// Start the service.
		given()
				.contentType(ContentType.JSON)
				.body(quiz, ObjectMapperType.JSONB)
		.when()
				.headers(headers)
				.post("/api/quiz/start/" + classroomId)
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
	void testReceiveAnswer() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicInteger errors = new AtomicInteger(0);
		final AtomicReference<QuizAnswerMessage> answerRef = new AtomicReference<>();

		// Subscribe for SSE.
		AuthState.getInstance().setToken(TestProducers.getToken());

		Client client = ClientBuilder.newClient()
				.register(JwtRequestFilter.class);
		WebTarget target = client.target(getURL("api/quiz/subscribe", serviceId));

		try (SseEventSource eventSource = SseEventSource.target(target).build()) {
			eventSource.register(sseEvent -> {
				Jsonb jsonb = new JsonConfig().getContext(null);
				QuizAnswerMessage answer = jsonb.fromJson(sseEvent.readData(), QuizAnswerMessage.class);

				answerRef.set(answer);

				latch.countDown();
			}, throwable -> errors.incrementAndGet());
			eventSource.open();

			// Send a message to the service.
			QuizAnswer answer = new QuizAnswer(new String[] { "1", "2" });
			answer.setServiceId(serviceId);

			given()
					.contentType(ContentType.JSON)
					.body(answer, ObjectMapperType.JSONB)
			.when()
					.post("/api/quiz/post")
			.then()
					.statusCode(200);

			assertTrue(latch.await(5, TimeUnit.SECONDS));
			assertEquals(0, errors.get());
			assertNotNull(answerRef.get());

			QuizAnswer recvAnswer = answerRef.get().getQuizAnswer();

			assertNotNull(recvAnswer);
			assertArrayEquals(answer.getOptions(), recvAnswer.getOptions());
			assertEquals(serviceId, recvAnswer.getServiceId());
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
				.post("/api/quiz/stop/" + classroomId + "/" + serviceId)
		.then()
				.statusCode(401);

		// Stop the service.
		given().when()
				.headers(headers)
				.post("/api/quiz/stop/" + classroomId + "/" + serviceId)
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
		WebTarget target = client.target(getURL("api/quiz/subscribe", "no-id"));

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
