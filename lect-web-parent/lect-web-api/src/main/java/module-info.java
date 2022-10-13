module org.lecturestudio.web.api {

	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.jaxrs.base;
	requires com.fasterxml.jackson.jaxrs.json;
	requires jakarta.inject;
	requires jakarta.persistence;
	requires java.json;
	requires java.json.bind;
	requires java.net.http;
	requires java.ws.rs;
	requires maven.artifact;
	requires microprofile.rest.client.api;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires org.lecturestudio.core;
	requires resteasy.multipart.provider;
	requires spring.websocket;
	requires spring.messaging;
	requires spring.context;
	requires spring.core;
	requires webrtc.java;

	exports org.lecturestudio.web.api.client;
	exports org.lecturestudio.web.api.data.bind;
	exports org.lecturestudio.web.api.event;
	exports org.lecturestudio.web.api.filter;
	exports org.lecturestudio.web.api.message;
	exports org.lecturestudio.web.api.model;
	exports org.lecturestudio.web.api.model.quiz;
	exports org.lecturestudio.web.api.service;
	exports org.lecturestudio.web.api.stream;
	exports org.lecturestudio.web.api.stream.action;
	exports org.lecturestudio.web.api.stream.client;
	exports org.lecturestudio.web.api.stream.model;
	exports org.lecturestudio.web.api.stream.service;
	exports org.lecturestudio.web.api.websocket;

}