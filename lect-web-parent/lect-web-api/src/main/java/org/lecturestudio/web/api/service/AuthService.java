package org.lecturestudio.web.api.service;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.lecturestudio.web.api.client.AuthClient;

@Dependent
public class AuthService extends ProviderService {

	private final AuthClient providerClient;


	@Inject
	public AuthService(ServiceParameters parameters) {
		providerClient = createProxy(AuthClient.class, parameters);
	}

	public String authenticate() {
		return providerClient.authenticate();
	}
}
