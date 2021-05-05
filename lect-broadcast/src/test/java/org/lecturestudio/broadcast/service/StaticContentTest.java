package org.lecturestudio.broadcast.service;

import static io.restassured.RestAssured.given;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.lecturestudio.web.api.filter.IpRangeRule;
import org.lecturestudio.web.api.model.Classroom;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaticContentTest extends ResourceTest {

	static String classroomId;

	@TestHTTPResource("index.html")
	URL url;


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
	public void testNoClassrooms() {
		given().when()
				.get("/")
		.then()
				.statusCode(404);
	}

	@Test
	@Order(2)
	public void testClassroomIpFilterBlock() {
		Classroom classroom = new Classroom();
		classroom.setName("Test Static Resource Classroom");

		// Create classroom with no IP filter rules.
		classroomId = given()
				.contentType(ContentType.JSON)
				.body(classroom, ObjectMapperType.JSONB)
		.when()
				.headers(headers)
				.post("/api/classroom")
		.then()
				.statusCode(200)
				.extract().asString();

		// Make sure the resource access is forbidden.
		given().when()
				.get("/")
		.then()
				.statusCode(403);
	}

	@Test
	@Order(3)
	public void testClassroomsIpFilterAllow() {
		List<IpRangeRule> ipRules = new ArrayList<>();
		ipRules.add(new IpRangeRule("0.0.0.0", "255.255.255.255"));

		Classroom classroom = new Classroom();
		classroom.setUuid(UUID.fromString(classroomId));
		classroom.setName("Test Static Resource Classroom");
		classroom.setIpFilterRules(ipRules);

		given()
				.contentType(ContentType.JSON)
				.body(classroom, ObjectMapperType.JSONB)
		.when()
				.headers(headers)
				.put("/api/classroom")
		.then()
				.statusCode(200);

		// Make sure the resource can be accessed.
		given().when()
				.get("/")
		.then()
				.statusCode(200);
	}

	@Test
	@Order(4)
	public void testIndexHtml() throws Exception {
		try (InputStream in = url.openStream()) {
			String contents = readStream(in);
			Assertions.assertFalse(contents.isEmpty());
		}
	}

	private static String readStream(InputStream in) {
		return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));
	}
}
