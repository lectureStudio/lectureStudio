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

package org.lecturestudio.web.api.ws.databind;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.lecturestudio.web.api.model.ClassroomService;
import org.lecturestudio.web.api.model.bind.ClassroomServiceMixIn;
import org.lecturestudio.web.api.model.quiz.Quiz;

@Provider
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
@ApplicationScoped
@Default
public class JsonProvider extends JacksonJsonProvider {

	private static final ObjectMapper MAPPER = new ObjectMapper();


	static {
		SimpleModule module = new SimpleModule();
		module.setMixInAnnotation(ClassroomService.class, ClassroomServiceMixIn.class);
		module.addSerializer(Quiz.class, new QuizSerializer());

		MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER.registerModule(module);
	}


	public JsonProvider() {
		super(MAPPER);
	}
}
