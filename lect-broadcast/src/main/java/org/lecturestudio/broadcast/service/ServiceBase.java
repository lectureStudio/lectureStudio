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

package org.lecturestudio.broadcast.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseEventSink;

import org.lecturestudio.web.api.data.ClassroomDataService;
import org.lecturestudio.web.api.exception.ServiceNotFoundException;
import org.lecturestudio.web.api.message.WebMessage;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomService;

abstract class ServiceBase {

	private OutboundSseEvent.Builder sseEventBuilder;

	@Inject
	SseSinkManager sseSinkManager;

	@Inject
	ClassroomDataService classroomDataService;


	void setSseEventBuilder(OutboundSseEvent.Builder builder) {
		sseEventBuilder = builder;
	}

	<T extends ClassroomService> void registerSseSink(SseEventSink eventSink,
			String serviceId, Class<T> sClass) {
		T service = classroomDataService.getServiceById(serviceId, sClass);

		if (isNull(service)) {
			throw new ServiceNotFoundException(
					"Classroom service has not been started");
		}

		sseSinkManager.registerSseSink(eventSink, serviceId);
	}

	void sendWebMessage(WebMessage message, String serviceId) {
		final SseEventSink eventSink = sseSinkManager.getSseSink(serviceId);

		if (nonNull(eventSink)) {
			OutboundSseEvent outboundSseEvent = sseEventBuilder
					.data(message)
					.mediaType(MediaType.APPLICATION_JSON_TYPE)
					.build();
			eventSink.send(outboundSseEvent);
		}
	}

	Response startService(String classroomId, ClassroomService service)
			throws Exception {
		Classroom classroom = classroomDataService.getByUuid(UUID.fromString(classroomId));
		boolean serviceActive = false;

		if (isNull(classroom)) {
			throw new Exception("Classroom does not exist");
		}

		ClassroomService classroomService = classroom.getServices()
				.stream()
				.filter(s -> s.getClass().equals(service.getClass()))
				.findFirst().orElse(null);

		if (isNull(classroomService)) {
			classroom.getServices().add(service);
		}
		else {
			serviceActive = true;
		}

		if (!serviceActive) {
			service.setContextPath(classroom.getShortName());
			service.setServiceId(Long.toString(new Random().nextLong()));
		}

		return Response.ok().entity(service.getServiceId()).build();
	}

	Response stopService(String classroomId, String serviceId, Class<? extends ClassroomService> serviceClass) throws Exception {
		Classroom classroom = classroomDataService.getByUuid(UUID.fromString(classroomId));

		if (isNull(classroom)) {
			throw new Exception("Classroom does not exist");
		}

		ClassroomService classroomService = classroomDataService.getServiceById(serviceId, serviceClass);

		if (isNull(classroomService)) {
			throw new Exception("Classroom service has not been started");
		}

		classroom.getServices().remove(classroomService);

		classroomDataService.update(classroom);

		return Response.ok().build();
	}

}
