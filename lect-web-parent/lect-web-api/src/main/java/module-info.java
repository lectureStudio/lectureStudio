module org.lecturestudio.web.api {

	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.jaxrs.json;
	requires io.netty.all;
	requires java.management;
	requires javaee.web.api;
	requires org.apache.cxf.frontend.jaxrs;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires org.jsoup;
	requires org.lecturestudio.core;

	exports org.lecturestudio.web.api.config;
	exports org.lecturestudio.web.api.connector;
	exports org.lecturestudio.web.api.connector.client;
	exports org.lecturestudio.web.api.connector.server;
	exports org.lecturestudio.web.api.data;
	exports org.lecturestudio.web.api.http;
	exports org.lecturestudio.web.api.filter;
	exports org.lecturestudio.web.api.message;
	exports org.lecturestudio.web.api.model;
	exports org.lecturestudio.web.api.model.bind;
	exports org.lecturestudio.web.api.model.quiz;
	exports org.lecturestudio.web.api.ws;
	exports org.lecturestudio.web.api.ws.databind;
	exports org.lecturestudio.web.api.ws.rs;

}