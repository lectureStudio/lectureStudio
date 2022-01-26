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

package org.lecturestudio.presenter.api.client;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;

import org.lecturestudio.web.api.client.ApiKeyFilter;
import org.lecturestudio.web.api.data.bind.JsonConfigProvider;
import org.lecturestudio.web.api.model.messenger.MessengerConfig;

@Path("/api/publisher")
@RegisterProviders({
		@RegisterProvider(ApiKeyFilter.class),
		@RegisterProvider(JsonConfigProvider.class)
})
public interface MessageFeatureClient {

	@Path("/messenger/start/{courseId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	String startMessenger(@PathParam("courseId") long courseId, @QueryParam("mode") String mode);

	@Path("/messenger/stop/{courseId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	void stopMessenger(@PathParam("courseId") long courseId);

}
