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

package org.lecturestudio.web.auth.service.oauth2.client.filter;

import static java.util.Objects.nonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.helpers.IOUtils;

public class LoggingFilter implements ClientResponseFilter {

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		System.out.println();
		System.out.println(requestContext.getMethod() + " " + requestContext.getUri());

		MultivaluedMap<String, Object> headers = requestContext.getHeaders();

		for (String key : headers.keySet()) {
			System.out.println(key + ": " + headers.get(key));
		}

		System.out.println();
		System.out.println(responseContext.getStatus() + " " + responseContext.getStatusInfo().getReasonPhrase());

		InputStream stream = responseContext.getEntityStream();

		if (nonNull(stream)) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(stream.available());
			IOUtils.copy(stream, buffer);
			responseContext.setEntityStream(new ByteArrayInputStream(buffer.toByteArray()));

			System.out.println(IOUtils.toString(responseContext.getEntityStream()));

			responseContext.getEntityStream().reset();
		}
		System.out.println();
	}

}
