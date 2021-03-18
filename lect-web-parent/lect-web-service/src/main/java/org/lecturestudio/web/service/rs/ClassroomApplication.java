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

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.lecturestudio.web.api.ws.databind.JsonProvider;

@ApplicationPath("/")
@ApplicationScoped
public class ClassroomApplication extends Application {

	@Inject
	private ClassroomServiceREST classroomServiceREST;

	@Inject
	private MessageServiceREST messageServiceREST;

	@Inject
	private QuizServiceREST quizServiceREST;

	@Inject
	private StreamServiceREST streamServiceREST;


	public Set<Object> getSingletons() {
		Set<Object> objects = new HashSet<>();

		objects.add(new JsonProvider());
		objects.add(classroomServiceREST);
		objects.add(messageServiceREST);
		objects.add(quizServiceREST);
		objects.add(streamServiceREST);

		return objects;
	}

}
