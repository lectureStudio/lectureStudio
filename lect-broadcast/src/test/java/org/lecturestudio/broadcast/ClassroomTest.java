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

package org.lecturestudio.broadcast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lecturestudio.web.api.filter.IpRangeRule;
import org.lecturestudio.web.api.filter.RegexRule;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomDocument;
import org.lecturestudio.web.api.model.MessageService;
import org.lecturestudio.web.api.model.QuizService;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.ws.ClassroomServiceClient;
import org.lecturestudio.web.api.ws.ConnectionParameters;
import org.lecturestudio.web.api.ws.MessageServiceClient;
import org.lecturestudio.web.api.ws.QuizServiceClient;
import org.lecturestudio.web.api.ws.rs.ClassroomRestClient;
import org.lecturestudio.web.api.ws.rs.MessageRestClient;
import org.lecturestudio.web.api.ws.rs.QuizRestClient;

public class ClassroomTest {

	public static void main(final String[] args) throws Exception {
		run();
	}

	public static void run() throws Exception {
		ConnectionParameters parameters = new ConnectionParameters("127.0.0.1", 80, false);
		ClassroomTest test = new ClassroomTest();
		test.testMessageService(parameters);
		//test.testQuizService(parameters);

		test.testClassroomService(parameters);
	}

	private void testClassroomService(ConnectionParameters parameters) throws Exception {
		ClassroomServiceClient serviceClient = new ClassroomRestClient(parameters);
		System.out.println(serviceClient.getClassrooms());
	}

	private void testMessageService(ConnectionParameters parameters) throws Exception {
		Classroom classroom = createClassroom();

		MessageService messageService = new MessageService();

		MessageServiceClient serviceClient = new MessageRestClient(parameters);
		System.out.println(serviceClient.startService(classroom, messageService));
	}

	private void testQuizService(ConnectionParameters parameters) throws Exception {
		Classroom classroom = createClassroom();

		List<RegexRule> regexRules = new ArrayList<>();
		regexRules.add(new RegexRule("^23"));
		regexRules.add(new RegexRule("^42"));
		regexRules.add(new RegexRule("^666"));

		Quiz quiz = new Quiz();
		quiz.setType(Quiz.QuizType.MULTIPLE);
		quiz.setQuestion("What's wrong?");
		quiz.addOption("nothing");
		quiz.addOption("everything");
		quiz.addOption("something");

		QuizService quizService = new QuizService();
		quizService.setQuiz(quiz);
		quizService.setRegexRules(regexRules);

		QuizServiceClient serviceClient = new QuizRestClient(parameters);
		System.out.println(serviceClient.startService(classroom, quizService));
	}

	private Classroom createClassroom() {
		List<IpRangeRule> ipRules = new ArrayList<>();
		ipRules.add(new IpRangeRule("192.168.0.0", "192.168.0.255"));
		ipRules.add(new IpRangeRule("192.168.2.0", "192.168.2.255"));
		ipRules.add(new IpRangeRule("127.0.0.0", "127.0.0.255"));

		Classroom classroom = new Classroom("Test Classroom", "");
		classroom.setLocale(Locale.GERMANY);
		classroom.setIpFilterRules(ipRules);

		classroom.getDocuments().add(new ClassroomDocument("hello.pdf"));

		return classroom;
	}

}
