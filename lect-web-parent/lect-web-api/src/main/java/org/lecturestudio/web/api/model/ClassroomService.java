/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.web.api.model;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.lecturestudio.web.api.config.WebServiceConfiguration;
import org.lecturestudio.web.api.connector.RelayConnectors;

public abstract class ClassroomService implements Serializable, Cloneable {

	private long id;

	private String type;

	/** The unique service ID number of the service session. */
	private String serviceId;

	private String contextPath;

	/** The media stream descriptions for this classroom session. */
	private List<StreamDescription> streamDescriptions;


	abstract public RelayConnectors initialize(Classroom classroom, WebServiceConfiguration config,
			HttpServletRequest request) throws Exception;


	/**
	 * @return the service ID
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * @param serviceId the service ID to set
	 */
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * @return the streamDescriptions
	 */
	public List<StreamDescription> getStreamDescriptions() {
		return streamDescriptions;
	}

	/**
	 * @param streamDescriptions the streamDescriptions to set
	 */
	public void setStreamDescriptions(List<StreamDescription> streamDescriptions) {
		this.streamDescriptions = streamDescriptions;
	}

	public ClassroomService clone() throws CloneNotSupportedException {
		return (ClassroomService) super.clone();
	}
}
