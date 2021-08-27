package org.lecturestudio.web.api.service;

import java.util.function.Consumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.lecturestudio.web.api.client.QuizProviderClient;
import org.lecturestudio.web.api.message.QuizAnswerMessage;
import org.lecturestudio.web.api.model.quiz.Quiz;

@Dependent
public class QuizProviderService extends ReactiveProviderService {

	private final QuizProviderClient providerClient;


	@Inject
	public QuizProviderService(ServiceParameters parameters) {
		super(parameters);

		providerClient = createProxy(QuizProviderClient.class, parameters);
	}

	public String startQuiz(String classroomId, Quiz quiz) {
		return providerClient.startQuiz(classroomId, quiz);
	}

	public void stopQuiz(String classroomId, String serviceId) {
		providerClient.stopQuiz(classroomId, serviceId);
	}

	public void subscribe(String serviceId, Consumer<QuizAnswerMessage> onEvent,
			Consumer<Throwable> onError) {
		subscribeSse("/api/quiz/subscribe/" + serviceId,
				QuizAnswerMessage.class, onEvent, onError);
	}
}
