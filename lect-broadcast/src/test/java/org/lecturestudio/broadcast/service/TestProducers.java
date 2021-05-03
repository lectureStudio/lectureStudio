package org.lecturestudio.broadcast.service;

import io.quarkus.arc.AlternativePriority;
import io.smallrye.jwt.build.Jwt;

import java.text.MessageFormat;
import java.util.Set;

import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.ConfigProvider;
import org.lecturestudio.web.api.service.ServiceParameters;

public class TestProducers {

	@Produces
	@AlternativePriority(1)
	ServiceParameters produceServiceParameters() {
		ServiceParameters parameters = new ServiceParameters();
		parameters.setUrl(getURL());

		return parameters;
	}

	static String getURL() {
		String httpHost = ConfigProvider.getConfig()
				.getValue("quarkus.http.host", String.class);
		String httpsPort = ConfigProvider.getConfig()
				.getValue("quarkus.http.test-ssl-port", String.class);

		MessageFormat form = new MessageFormat("https://{0}:{1}");

		return form.format(new Object[] { httpHost, httpsPort });
	}

	public static String getToken() {
		return Jwt.issuer("https://lecturestudio.org/issuer")
				.upn("lecturer@lecturestudio.org")
				.groups(Set.of("admin", "lecturer"))
				.sign();
	}
}
