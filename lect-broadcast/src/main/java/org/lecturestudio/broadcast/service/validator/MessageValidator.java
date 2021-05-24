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

package org.lecturestudio.broadcast.service.validator;

import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import org.lecturestudio.web.api.model.ClassroomServiceResponse;
import org.lecturestudio.web.api.model.ClassroomServiceResponse.Status;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.model.MessageService;

@ApplicationScoped
public class MessageValidator {

	public Response validate(MessageService service, Message message) {
		Response.ResponseBuilder responseBuilder;

		if (isNull(service)) {
			ClassroomServiceResponse serviceResponse = new ClassroomServiceResponse();
			serviceResponse.statusCode = Status.ERROR.getCode();
			serviceResponse.statusMessage = "message.service.absent";

			responseBuilder = Response.status(Response.Status.BAD_REQUEST);
			responseBuilder.entity(serviceResponse);
		}
		else {
			Map<Integer, String> fieldErrors = new HashMap<>();

			try {
				validateServiceId(service, message);
				validateInputFields(message, fieldErrors);

				ClassroomServiceResponse serviceResponse = new ClassroomServiceResponse();
				serviceResponse.statusCode = Status.SUCCESS.getCode();
				serviceResponse.statusMessage = "message.sent";

				responseBuilder = Response.status(Response.Status.OK);
				responseBuilder.entity(serviceResponse);
			}
			catch (Exception e) {
				ClassroomServiceResponse serviceResponse = new ClassroomServiceResponse();
				serviceResponse.statusCode = Status.DATA_ERROR.getCode();
				serviceResponse.statusMessage = e.getMessage();
				serviceResponse.data = fieldErrors;

				responseBuilder = Response.status(Response.Status.BAD_REQUEST);
				responseBuilder.entity(serviceResponse);
			}
		}

		return responseBuilder.build();
	}

	private static void validateServiceId(MessageService service, Message message) throws Exception {
		if (!service.getServiceId().equals(message.getServiceId())) {
			throw new Exception("message.service.absent");
		}
	}

	private static void validateInputFields(Message message, Map<Integer, String> fieldErrors) throws Exception {
		String messageStr = message.getText();

		if (isNull(messageStr) || messageStr.isEmpty()) {
			fieldErrors.put(0, "message.input.empty");
		}

		if (!fieldErrors.isEmpty()) {
			throw new Exception("message.input.invalid");
		}
	}

}
