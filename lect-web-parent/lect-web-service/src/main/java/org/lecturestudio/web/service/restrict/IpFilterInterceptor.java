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

package org.lecturestudio.web.service.restrict;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;

import org.lecturestudio.web.api.filter.IpFilter;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomService;
import org.lecturestudio.web.api.model.ServiceModel;
import org.lecturestudio.web.api.data.ClassroomDataService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@IpRestricted
@Interceptor
public class IpFilterInterceptor {

	private static final Logger LOG = LogManager.getLogger(IpFilterInterceptor.class);

	@Inject
	private ClassroomDataService classroomDataService;


	@AroundInvoke
	public Object runIpFilter(InvocationContext invocationContext) throws Exception {
		Method method = invocationContext.getMethod();
		Classroom classroom = getParameter(invocationContext, Classroom.class);

		boolean allowed = false;

		// Primarily look for a classroom, since it has the IP rules.
		if (isNull(classroom)) {
			// Secondarily look for a classroom service, since it has the context path for the classroom.
			ClassroomService service = getParameter(invocationContext, ClassroomService.class);

			if (isNull(service)) {
				// Last attempt is to find a service model which is associated with a classroom service.
				ServiceModel model = getParameter(invocationContext, ServiceModel.class);

				if (nonNull(model)) {
					service = classroomDataService.getServiceById(model.getServiceId(), ClassroomService.class);
				}
			}

			if (nonNull(service)) {
				classroom = classroomDataService.getByContextPath(service.getContextPath());
			}
		}

		if (nonNull(classroom)) {
			HttpServletRequest request = getParameter(invocationContext, HttpServletRequest.class);

			if (nonNull(request)) {
				IpFilter ipFilter = new IpFilter();
				ipFilter.setRules(classroom.getIpFilterRules());

				String remoteAddress = request.getRemoteAddr();

				if (!ipFilter.isAllowed(remoteAddress)) {
					LOG.debug("Connection attempt blocked from: {} to resource [{}]", remoteAddress, method.toString());

					throw new RestrictedAccessException("restricted.ip.error");
				}

				allowed = true;
			}
		}

		if (!allowed) {
			LOG.warn("Access to unprotected resource: {}", method.toString());
		}

		return invocationContext.proceed();
	}

	@SuppressWarnings("unchecked")
	private <T> T getParameter(InvocationContext ic, Class<T> paramClass) {
		for (Object parameter : ic.getParameters()) {
			if (paramClass.isAssignableFrom(parameter.getClass())) {
				return (T) parameter;
			}
		}
		return null;
	}
}
