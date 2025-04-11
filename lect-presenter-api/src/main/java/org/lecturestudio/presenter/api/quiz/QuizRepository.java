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

/**
 * Repository interface for managing Quiz entities. Provides CRUD operations for Quiz objects with
 * persistence capabilities.
 *
 * @author Alex Andres
 */
public interface QuizRepository {

	/**
	 * Retrieves all Quiz entities from the repository.
	 *
	 * @return a list containing all stored Quiz objects.
	 *
	 * @throws IOException if an I/O error occurs during the operation.
	 */
	List<Quiz> findAll() throws IOException;

	/**
	 * Saves a single Quiz entity to the repository.
	 *
	 * @param quiz the Quiz object to save.
	 *
	 * @throws IOException if an I/O error occurs during the operation.
	 */
	void save(Quiz quiz) throws IOException;

	/**
	 * Saves multiple Quiz entities to the repository.
	 *
	 * @param quizzes a collection of Quiz objects to save.
	 *
	 * @throws IOException if an I/O error occurs during the operation.
	 */
	void saveAll(Collection<Quiz> quizzes) throws IOException;

	/**
	 * Deletes a specific Quiz entity from the repository.
	 *
	 * @param quiz the Quiz object to delete.
	 *
	 * @throws IOException if an I/O error occurs during the operation.
	 */
	void delete(Quiz quiz) throws IOException;

	/**
	 * Deletes all Quiz entities from the repository.
	 *
	 * @throws IOException if an I/O error occurs during the operation.
	 */
	void deleteAll() throws IOException;

}
