package org.lecturestudio.broadcast.service;

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.lecturestudio.web.api.data.bind.JsonConfig;

abstract class ResourceTest {

	static Map<String, String> headers;

	@ConfigProperty(name = "quarkus.http.host")
	String httpHost;

	@ConfigProperty(name = "quarkus.http.test-port")
	String httpPort;


	@BeforeAll
	static void initialize() {
		headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + TestProducers.getToken());

		RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
				objectMapperConfig().jsonbObjectMapperFactory((type, s) -> {
					return new JsonConfig().getContext(null);
				}));
	}

	String getURL(String path, Object pathArg) {
		MessageFormat form = new MessageFormat("http://{0}:{1}/{2}/{3}");

		return form.format(new Object[] { httpHost, httpPort, path, pathArg });
	}
}
