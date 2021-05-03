package org.lecturestudio.broadcast.service;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.lecturestudio.web.api.filter.IpRangeRule;
import org.lecturestudio.web.api.model.Classroom;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClassroomResourceTest extends ResourceTest {

	static String classroomId;


	@Test
	@Order(1)
	public void testCreateClassroom() {
		Classroom classroom = new Classroom();
		classroom.setName("Test Start Classroom");

		// Test unauthorized.
		given()
				.contentType(ContentType.JSON)
				.body(classroom, ObjectMapperType.JSONB)
		.when()
				.post("/api/classroom")
		.then()
				.statusCode(401);

		classroomId = given()
				.headers(headers)
				.contentType(ContentType.JSON)
				.body(classroom, ObjectMapperType.JSONB)
		.when()
				.post("/api/classroom")
		.then()
				.statusCode(200)
				.extract().asString();

		assertNotNull(classroomId);
		assertFalse(classroomId.isEmpty());

		UUID uuid = UUID.fromString(classroomId);

		assertEquals(classroomId, uuid.toString());
	}

	@Test
	@Order(2)
	public void testGetClassroom() {
		given().when()
				.get("/api/classroom")
		.then()
				.statusCode(200)
				.body("name", equalTo("Test Start Classroom"));
	}

	@Test
	@Order(3)
	public void testGetClassrooms() {
		given().when()
				.get("/api/classroom/list")
		.then()
				.statusCode(200)
				.body("$.size()", is(1),
						"name", contains("Test Start Classroom"));
	}

	@Test
	@Order(4)
	public void testUpdateClassroom() {
		List<IpRangeRule> ipRules = new ArrayList<>();
		ipRules.add(new IpRangeRule("0.0.0.0", "255.255.255.255"));

		Classroom classroom = new Classroom();
		classroom.setUuid(UUID.fromString(classroomId));
		classroom.setName("Test Update Classroom");
		classroom.setIpFilterRules(ipRules);

		given()
				.headers(headers)
				.contentType(ContentType.JSON)
				.body(classroom, ObjectMapperType.JSONB)
		.when()
				.put("/api/classroom")
		.then()
				.statusCode(200);

		// Make sure the name and IP filter rules are updated.
		Classroom remoteClassroom = given().when()
				.get("/api/classroom")
		.then()
				.statusCode(200)
				.extract().as(Classroom.class);

		assertEquals(classroom.getName(), remoteClassroom.getName());
		assertEquals(classroom.getIpFilterRules(), remoteClassroom.getIpFilterRules());
	}

	@Test
	@Order(5)
	public void testDeleteClassroom() {
		// Test unauthorized.
		given().when()
				.delete("/api/classroom/" + classroomId)
		.then()
				.statusCode(401);

		given().when()
				.headers(headers)
				.delete("/api/classroom/" + classroomId)
		.then()
				.statusCode(200);

		// Make sure the classroom got deleted.
		given().when()
				.get("/api/classroom/list")
		.then()
				.statusCode(200)
				.body("$.size()", is(0));
	}
}
