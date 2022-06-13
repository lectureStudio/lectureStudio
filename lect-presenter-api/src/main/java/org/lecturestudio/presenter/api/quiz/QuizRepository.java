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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.lecturestudio.web.api.model.quiz.Quiz;

public interface QuizRepository {

	List<Quiz> findAll() throws IOException;

	void save(Quiz quiz) throws IOException;

	void saveAll(Collection<Quiz> quizzes) throws IOException;

	void delete(Quiz quiz) throws IOException;

	void deleteAll() throws IOException;

}
