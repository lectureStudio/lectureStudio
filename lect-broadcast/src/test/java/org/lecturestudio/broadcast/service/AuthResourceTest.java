package org.lecturestudio.broadcast.service;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;

import javax.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthResourceTest extends ResourceTest {

	@Inject
	JWTParser parser;


	@Test
	@Order(1)
	public void testAuthentication() throws ParseException {
		String token = given().when()
				.post("/api/auth")
		.then()
				.statusCode(200)
				.extract().asString();

		assertNotNull(token);
		assertNotNull(parser.parse(token));
	}

}
