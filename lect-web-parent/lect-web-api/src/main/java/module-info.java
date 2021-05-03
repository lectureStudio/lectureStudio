module org.lecturestudio.web.api {

	requires jakarta.enterprise.cdi.api;
	requires jakarta.inject.api;
	requires java.json.bind;
	requires java.management;
	requires java.persistence;
	requires java.ws.rs;
	requires microprofile.rest.client.api;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires org.lecturestudio.core;
	requires quarkus.core;
	requires resteasy.core;
	requires smallrye.jwt;

	exports org.lecturestudio.web.api.data;
	exports org.lecturestudio.web.api.filter;
	exports org.lecturestudio.web.api.message;
	exports org.lecturestudio.web.api.model;
	exports org.lecturestudio.web.api.model.quiz;

}