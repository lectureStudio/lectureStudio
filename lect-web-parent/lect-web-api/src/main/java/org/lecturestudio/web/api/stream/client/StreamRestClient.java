/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.stream.client;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;

import org.lecturestudio.web.api.client.AuthorizationFilter;
import org.lecturestudio.web.api.data.bind.JsonConfigProvider;
import org.lecturestudio.web.api.stream.model.Lecture;

/**
 * Streaming API REST client implementation.
 *
 * @author Alex Andres
 */
@Path("/api")
@RegisterProviders({
	@RegisterProvider(AuthorizationFilter.class),
	@RegisterProvider(JsonConfigProvider.class)
})
public interface StreamRestClient {

	/**
	 * Gets a list of all lectures associated with a user. The user must be
	 * authenticated with a token to the API.
	 *
	 * @return A list of all lectures associated with a user.
	 */
	@GET
	@Path("/lectures")
	List<Lecture> getLectures();

}
