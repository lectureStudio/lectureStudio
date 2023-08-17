package org.lecturestudio.web.api.exception;

public class ServiceNotFoundException extends RuntimeException {

	public ServiceNotFoundException() {
		super();
	}

	public ServiceNotFoundException(String message) {
		super(message);
	}

	public ServiceNotFoundException(String message, Exception e) {
		super(message, e);
	}

}
