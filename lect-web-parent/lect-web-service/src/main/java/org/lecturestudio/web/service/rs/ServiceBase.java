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

package org.lecturestudio.web.service.rs;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.lecturestudio.web.api.config.WebServiceConfiguration;
import org.lecturestudio.web.api.connector.RelayConnectors;
import org.lecturestudio.web.api.connector.server.Connector;
import org.lecturestudio.web.api.data.ClassroomDataService;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomService;
import org.lecturestudio.web.api.model.StreamDescription;
import org.lecturestudio.web.service.model.WebSessions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

abstract class ServiceBase {

	private static final Logger LOG = LogManager.getLogger(ServiceBase.class);

	@Inject
	WebServiceConfiguration config;

	@Inject
	ClassroomDataService classroomDataService;

	@Inject
	WebSessions sessions;


	Response startService(
			HttpServletRequest request,
			Classroom classroom,
			ClassroomService service) throws Exception
	{
		String contextPath = classroom.getShortName();

		LOG.debug("Starting service: {} [context path {}]", service.getClass().getSimpleName(), contextPath);

		Classroom contextClassroom = classroomDataService.getByContextPath(contextPath);
		RelayConnectors serviceConnectors;

		if (isNull(contextClassroom)) {
			// Create new classroom.
			contextClassroom = new Classroom(classroom.getName(), classroom.getShortName());
			contextClassroom.setLocale(classroom.getLocale());
			contextClassroom.setCreatedTimestamp(System.currentTimeMillis());
			contextClassroom.setDocuments(classroom.getDocuments());
			contextClassroom.setIpFilterRules(classroom.getIpFilterRules());
			contextClassroom.getServices().add(service);

			serviceConnectors = service.initialize(contextClassroom, config, request);

			classroomDataService.add(contextClassroom);
		}
		else {
			ClassroomService classroomService = classroomDataService.getServiceByContextPath(contextPath, service.getClass());

			if (isNull(classroomService)) {
				// Update IP filter rules.
				contextClassroom.setIpFilterRules(classroom.getIpFilterRules());

				contextClassroom.getServices().add(service);

				serviceConnectors = service.initialize(contextClassroom, config, request);
			}
			else {
				throw new Exception("Classroom service " + service.getClass() + " is already started.");
			}
		}

		sessions.addWebSession(new WebSessions.KeyPair(contextPath, service.getClass()), serviceConnectors);

		List<StreamDescription> streamDescriptions = serviceConnectors.getProviderConnectors().getConnectors().
				stream().
				map(Connector::getStreamDescription).
				collect(Collectors.toList());

		service.setContextPath(contextPath);
		service.setServiceId(Long.toString(new Random().nextLong()));

		// Return a provider specific classroom.
		ClassroomService provService = service.clone();
		provService.setStreamDescriptions(streamDescriptions);

		Classroom provClassroom = new Classroom(contextClassroom.getName(), contextClassroom.getShortName());
		provClassroom.setLocale(contextClassroom.getLocale());
		provClassroom.setCreatedTimestamp(contextClassroom.getCreatedTimestamp());
		provClassroom.setDocuments(contextClassroom.getDocuments());
		provClassroom.setIpFilterRules(contextClassroom.getIpFilterRules());
		provClassroom.getServices().addAll(contextClassroom.getServices());
		provClassroom.getServices().remove(service);
		provClassroom.getServices().add(provService);

		return Response.ok().entity(provClassroom).build();
	}

	Response stopService(Classroom classroom, ClassroomService service) throws Exception
	{
		String contextPath = classroom.getShortName();

		LOG.debug("Stopping service: {} [context path {}]", service.getClass().getSimpleName(), contextPath);

		Classroom contextClassroom = classroomDataService.getByContextPath(contextPath);

		if (isNull(contextClassroom)) {
			throw new Exception("Classroom does not exist.");
		}

		ClassroomService classroomService = classroomDataService.getServiceByContextPath(contextPath, service.getClass());

		if (isNull(classroomService)) {
			throw new Exception("Classroom service is not started.");
		}

		contextClassroom.getServices().remove(classroomService);

		classroomDataService.update(contextClassroom);

		WebSessions.KeyPair key = new WebSessions.KeyPair(contextPath, service.getClass());
		RelayConnectors session = sessions.getWebSession(key);

		if (nonNull(session.getProviderConnectors())) {
			session.getProviderConnectors().destroy();
		}
		if (nonNull(session.getReceiverConnectors())) {
			session.getReceiverConnectors().destroy();
		}

		sessions.removeWebSession(key);

		if (contextClassroom.getServices().isEmpty()) {
			// Remove classroom if it has no services running.
			classroomDataService.delete(contextClassroom);
		}

		return Response.ok().entity(contextClassroom).build();
	}

}
