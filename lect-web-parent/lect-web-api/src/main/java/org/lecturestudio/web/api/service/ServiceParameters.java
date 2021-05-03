package org.lecturestudio.web.api.service;

import javax.enterprise.context.Dependent;

@Dependent
public class ServiceParameters {

	private String url;


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
