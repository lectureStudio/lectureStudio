package org.lecturestudio.web.api.model;

public class AuthState {

	private String token;

	private static AuthState instance;


	private AuthState() {
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public static AuthState getInstance() {
		if (AuthState.instance == null) {
			AuthState.instance = new AuthState();
		}
		return AuthState.instance;
	}
}
