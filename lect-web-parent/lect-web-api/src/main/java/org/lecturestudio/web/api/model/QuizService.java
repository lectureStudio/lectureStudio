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

package org.lecturestudio.web.api.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.lecturestudio.web.api.model.quiz.Quiz;

@Entity
public class QuizService extends ClassroomService {

	/** The quiz of this session. */
	@OneToOne(cascade = { CascadeType.ALL })
	private Quiz quiz;

	/** The end-points which posted an answer. This is a concurrent hash set. */
	@ElementCollection
	@CollectionTable(name = "QuizServiceHosts", joinColumns = @JoinColumn(name = "id"))
	@Column(name = "host")
	private Set<Integer> hosts = new HashSet<>();


	/**
	 * @return the quiz
	 */
	public Quiz getQuiz() {
		return quiz;
	}

	/**
	 * @param quiz the Quiz to set
	 */
	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	/**
	 * @return the hosts
	 */
	public Set<Integer> getHosts() {
		return hosts;
	}

	public void setHosts(Set<Integer> hosts) {
		this.hosts = hosts;
	}
}
