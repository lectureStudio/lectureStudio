/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.quiz;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableHashSet;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.util.ObservableSet;
import org.lecturestudio.web.api.model.quiz.Quiz;

/**
 * A JSON file-based implementation of {@link QuizRepository}.
 * This class provides functionality to store and retrieve {@link Quiz} objects
 * using a JSON file as persistence.
 * <p>
 * Uses Jackson for JSON serialization and deserialization with customized
 * configurations for handling Observable collections.
 * </p>
 *
 * @author Alex Andres
 */
public class JsonQuizFileRepository implements QuizRepository {

	/** The Jackson ObjectMapper used for JSON serialization and deserialization. */
	private final ObjectMapper mapper;

	/** The file used as the persistent storage location for Quiz objects. */
	private final File file;


	/**
	 * Constructs a new JsonQuizFileRepository with the specified file as storage.

	 * @param file The file to use for persistent storage of quizzes.
	 */
	public JsonQuizFileRepository(File file) {
		this.file = file;

		SimpleModule module = new SimpleModule();
		module.addAbstractTypeMapping(ObservableList.class, ObservableArrayList.class);
		module.addAbstractTypeMapping(ObservableSet.class, ObservableHashSet.class);

		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.registerModules(new Jdk8Module());
		mapper.registerModule(module);
	}

	@Override
	public List<Quiz> findAll() throws IOException {
		if (file == null || !file.exists()) {
			return new ArrayList<>();
		}

		return mapper.readValue(file, new TypeReference<>() { });
	}

	@Override
	public void save(Quiz quiz) throws IOException {
		List<Quiz> quizzes = findAll();

		if (!quizzes.contains(quiz)) {
			quizzes.add(quiz);

			saveAll(quizzes);
		}
	}

	@Override
	public void saveAll(Collection<Quiz> quizzes) throws IOException {
		List<Quiz> all = findAll();
		int size = all.size();

		for (Quiz quiz : quizzes) {
			if (!all.contains(quiz)) {
				all.add(quiz);
			}
		}

		if (all.size() > size) {
			mapper.writeValue(file, all);
		}
	}

	@Override
	public void delete(Quiz quiz) throws IOException {
		List<Quiz> quizzes = findAll();

		if (quizzes.remove(quiz)) {
			mapper.writeValue(file, quizzes);
		}
	}

	@Override
	public void deleteAll() throws IOException {
		mapper.writeValue(file, List.of());
	}
}
