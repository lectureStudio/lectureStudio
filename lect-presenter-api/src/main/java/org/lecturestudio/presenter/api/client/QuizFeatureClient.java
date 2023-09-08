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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import org.lecturestudio.web.api.client.ApiKeyFilter;
import org.lecturestudio.web.api.client.MultipartBody;
import org.lecturestudio.web.api.data.bind.ClientJsonMapper;

@Path("/api/v1/publisher")
@RegisterProviders({
		@RegisterProvider(ApiKeyFilter.class),
		@RegisterProvider(ClientJsonMapper.class)
})
public interface QuizFeatureClient {

	@Path("/quiz/start/{courseId}")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	String startQuiz(@PathParam("courseId") long courseId,
			@MultipartForm MultipartBody data);

	@Path("/quiz/stop/{courseId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	void stopQuiz(@PathParam("courseId") long courseId);

}
