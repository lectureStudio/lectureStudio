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
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.lecturestudio.web.api.config.WebServiceConfiguration;
import org.lecturestudio.web.api.connector.RelayConnectors;
import org.lecturestudio.web.api.connector.RelayConnectorsFactory;
import org.lecturestudio.web.api.connector.server.Connectors;
import org.lecturestudio.web.api.filter.RegexRule;
import org.lecturestudio.web.api.model.quiz.Quiz;

public class QuizService extends ClassroomService {

	/** The quiz of this session. */
	private Quiz quiz;

	private List<RegexRule> regexRules;

	/** The end-points which posted an answer. This is a concurrent hash set. */
	private Set<Integer> hosts;


	@Override
	public RelayConnectors initialize(Classroom classroom, WebServiceConfiguration config, HttpServletRequest request) throws Exception {
		hosts = new HashSet<>();
		String hostName = request.getServerName();

		// Create a new session.
		RelayConnectors relayConnectors = RelayConnectorsFactory.createProviderConnectors(hostName, config.mediaTransport);

		Connectors connectors = relayConnectors.getProviderConnectors();
		connectors.start();

		return relayConnectors;
	}

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

	/**
	 * @return the regex rules
	 */
	public List<RegexRule> getRegexRules() {
		return regexRules;
	}

	/**
	 * @param rules the regex rules to set
	 */
	public void setRegexRules(List<RegexRule> rules) {
		this.regexRules = rules;
	}
}
