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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentList;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.service.QuizService;
import org.lecturestudio.presenter.api.service.StreamService;
import org.lecturestudio.presenter.api.view.CreateQuizDefaultOptionView;
import org.lecturestudio.presenter.api.view.CreateQuizNumericOptionView;
import org.lecturestudio.presenter.api.view.CreateQuizOptionView;
import org.lecturestudio.presenter.api.view.CreateQuizView;

import org.jsoup.Jsoup;
import org.lecturestudio.web.api.filter.MinMaxRule;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizSet;

public class CreateQuizPresenter extends Presenter<CreateQuizView> {

	private final List<CreateQuizOptionView> optionContextList;

	private final ViewContextFactory viewFactory;

	private final DocumentService documentService;

	private final QuizService quizService;

	private final StreamService streamService;

	/** The Quiz created by the user. */
	private Quiz quiz = new Quiz();

	/** The Quiz to edit. */
	private Quiz quizEdit;

	/** The action that is executed when the user clicks the 'Start Quiz' button. */
	private Action startQuizAction;

	private Document genericDoc;

	private Document selectedDoc;


	@Inject
	CreateQuizPresenter(ApplicationContext context, CreateQuizView view,
			ViewContextFactory viewFactory, DocumentService documentService,
			QuizService quizService, StreamService streamService) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.documentService = documentService;
		this.quizService = quizService;
		this.streamService = streamService;
		this.optionContextList = new ArrayList<>();
	}

	@Override
	public void initialize() throws Exception {
		genericDoc = new Document();
		genericDoc.setTitle(context.getDictionary().get("quiz.generic"));

		DocumentList docList = documentService.getDocuments();
		selectedDoc = docList.getSelectedDocument();

		if (!selectedDoc.isPDF()) {
			selectedDoc = docList.getLastNonWhiteboard();

			if (isNull(selectedDoc)) {
				selectedDoc = genericDoc;
			}
		}

		// Set default values.
		setQuizSet(isGenericSet() ?
				Quiz.QuizSet.GENERIC :
				QuizSet.DOCUMENT_SPECIFIC);
		setQuizType(Quiz.QuizType.MULTIPLE);

		List<Document> documents = new ArrayList<>();
		documents.add(genericDoc);
		documents.addAll(docList.getPdfDocuments());

		view.setDocuments(documents);
		view.setDocument(selectedDoc);
		view.setOnClose(this::close);
		view.setOnDocumentSelected(this::documentSelected);
		view.setOnNewOption(this::newOption);
		view.setOnSaveQuiz(this::saveQuiz);
		view.setOnSaveAndNextQuiz(this::saveAndNextQuiz);
		view.setOnStartQuiz(this::startQuiz);
		view.setOnQuizType(this::setQuizType);

		if (nonNull(quizEdit)) {
			this.quiz = quizEdit.clone();

			fillForm();
		}
	}

	@Override
	public void close() {
		genericDoc.close();

		super.close();
	}

	public Quiz getQuiz() {
		return quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quizEdit = quiz;
	}

	public void setOnStartQuiz(Action action) {
		this.startQuizAction = action;
	}

	private void saveQuiz() {
		try {
			createQuiz();
		}
		catch (Exception e) {
			handleException(e, "Create quiz failed", "quiz", "quiz.save.error");
		}

		boolean isGenericSet = isGenericSet();

		try {
			if (nonNull(quizEdit)) {
				if (isGenericSet) {
					quizService.replaceQuiz(quizEdit, quiz);
				}
				else {
					quizService.replaceQuiz(quizEdit, quiz, selectedDoc);
				}
			}
			else {
				if (isGenericSet) {
					quizService.saveQuiz(quiz);
				}
				else {
					quizService.saveQuiz(quiz, selectedDoc);
				}
			}

			// Remember saved quiz to replace it when edited again.
			setQuiz(quiz.clone());
		}
		catch (IOException e) {
			handleException(e, "Save quiz failed", "quiz.save.error");
		}
	}

	private void saveAndNextQuiz() {
		saveQuiz();
		setQuiz(null);

		quiz = new Quiz(Quiz.QuizType.MULTIPLE, "");
		quiz.setQuizSet(isGenericSet() ?
				Quiz.QuizSet.GENERIC :
				QuizSet.DOCUMENT_SPECIFIC);

		view.setQuizText(quiz.getQuestion());
		view.setQuizType(quiz.getType());

		clearOptions();
	}

	private void startQuiz() {
		try {
			createQuiz();
		}
		catch (Exception e) {
			handleException(e, "Create quiz failed", "quiz", "quiz.save.error");
		}

		streamService.startQuiz(quiz);

		close();

		if (nonNull(startQuizAction)) {
			startQuizAction.execute();
		}
	}

	private void setQuizSet(Quiz.QuizSet set) {
		quiz.setQuizSet(set);
	}

	private void setQuizType(Quiz.QuizType type) {
		quiz.setType(type);

		// Re-create option elements for the new type.
		List<CreateQuizOptionView> optionViews = new ArrayList<>();

		for (CreateQuizOptionView currentView : optionContextList) {
			CreateQuizOptionView newView = createOption();
			newView.setOptionText(currentView.getOptionText());

			optionViews.add(newView);
		}

		clearOptions();

		optionViews.forEach(view -> addOptionView(view, false));
	}

	private boolean isGenericSet() {
		return selectedDoc == genericDoc;
	}

	private void documentSelected(Document doc) {
		if (doc == genericDoc) {
			setQuizSet(Quiz.QuizSet.GENERIC);

			this.selectedDoc = genericDoc;
		}
		else {
			setQuizSet(Quiz.QuizSet.DOCUMENT_SPECIFIC);

			this.selectedDoc = doc;
		}
	}

	private void clearOptions() {
		view.clearOptions();
		optionContextList.clear();
	}

	private CreateQuizOptionView createOption() {
		return switch (quiz.getType()) {
			case MULTIPLE, SINGLE ->
					viewFactory.getInstance(CreateQuizDefaultOptionView.class);
			case NUMERIC ->
					viewFactory.getInstance(CreateQuizNumericOptionView.class);
		};
	}

	private void newOption() {
		addOptionView(createOption(), true);
	}

	private void addOptionView(CreateQuizOptionView optionView, boolean focus) {
		view.addQuizOptionView(optionView);

		optionView.setOnMoveDown(() -> moveOptionDown(optionView));
		optionView.setOnMoveUp(() -> moveOptionUp(optionView));
		optionView.setOnRemove(() -> removeOption(optionView));
		optionView.setOnTabKey(() -> tabKey(optionView));

		if (focus) {
			optionView.focus();
		}

		optionContextList.add(optionView);
	}

	private void removeOption(CreateQuizOptionView optionView) {
		view.removeQuizOptionView(optionView);

		optionContextList.remove(optionView);
	}

	private void moveOptionUp(CreateQuizOptionView optionView) {
		int index = optionContextList.indexOf(optionView);
		int newIndex = index - 1;

		if (index < 0 || newIndex < 0) {
			return;
		}

		view.moveQuizOptionViewUp(optionView);

		Collections.swap(optionContextList, index, newIndex);
	}

	private void moveOptionDown(CreateQuizOptionView optionView) {
		int index = optionContextList.indexOf(optionView);
		int newIndex = index + 1;

		if (index < 0 || newIndex >= optionContextList.size()) {
			return;
		}

		view.moveQuizOptionViewDown(optionView);

		Collections.swap(optionContextList, index, newIndex);
	}

	private void tabKey(CreateQuizOptionView optionView) {
		if (!isLastOption(optionView)) {
			return;
		}

		newOption();
	}

	private boolean isLastOption(CreateQuizOptionView optionView) {
		int index = optionContextList.indexOf(optionView);

		return (index != -1 && index == optionContextList.size() - 1);
	}

	private void createQuiz() {
		org.jsoup.nodes.Document doc = Jsoup.parse(view.getQuizText());
		doc.outputSettings().indentAmount(0);
		doc.outputSettings().prettyPrint(false);

		String htmlBody = doc.body().html().replace("&nbsp;", " ");

		quiz.setQuestion(htmlBody);
		quiz.clearOptions();
		quiz.clearInputFilter();

		for (int index = 0; index < optionContextList.size(); index++) {
			CreateQuizOptionView view = optionContextList.get(index);
			String option = view.getOptionText();

			quiz.addOption(option);

			if (quiz.getType() == Quiz.QuizType.NUMERIC) {
				CreateQuizNumericOptionView numericOptionView = (CreateQuizNumericOptionView) view;

				int min = numericOptionView.getMinValue();
				int max = numericOptionView.getMaxValue();

				MinMaxRule rule = new MinMaxRule(min, max, index);

				quiz.addInputRule(rule);
			}
		}
	}

	private void fillForm() {
		if (nonNull(quiz.getQuestion())) {
			view.setQuizText(quiz.getQuestion());
		}

		setQuizSet(isGenericSet() ?
				Quiz.QuizSet.GENERIC :
				QuizSet.DOCUMENT_SPECIFIC);

		if (isNull(quiz.getType())) {
			setQuizType(Quiz.QuizType.MULTIPLE);
		}
		else {
			clearOptions();

			view.setQuizType(quiz.getType());

			List<String> options = quiz.getOptions();

			for (int i = 0; i < options.size(); i++) {
				String optionText = options.get(i);

				switch (quiz.getType()) {
					case MULTIPLE, SINGLE -> {
						CreateQuizOptionView optionView = viewFactory.getInstance(
								CreateQuizDefaultOptionView.class);
						optionView.setOptionText(optionText);

						addOptionView(optionView, false);
					}
					case NUMERIC -> {
						MinMaxRule rule = (MinMaxRule) quiz.getInputFilter()
								.getRules().get(i);

						CreateQuizNumericOptionView optionView = viewFactory.getInstance(
								CreateQuizNumericOptionView.class);
						optionView.setOptionText(optionText);

						if (rule.getMin() != Integer.MIN_VALUE) {
							optionView.setMinValue(rule.getMin());
						}
						if (rule.getMax() != Integer.MAX_VALUE) {
							optionView.setMaxValue(rule.getMax());
						}

						addOptionView(optionView, false);
					}
				}
			}
		}
	}
}
