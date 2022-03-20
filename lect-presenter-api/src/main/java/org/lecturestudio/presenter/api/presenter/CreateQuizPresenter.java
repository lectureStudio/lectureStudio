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
		genericDoc.close();

		DocumentList docList = documentService.getDocuments();
		selectedDoc = docList.getSelectedDocument();

		if (!selectedDoc.isPDF()) {
			selectedDoc = docList.getLastNonWhiteboard();

			if (isNull(selectedDoc)) {
				selectedDoc = genericDoc;
			}
		}

		// Set default values.
		setQuizSet(Quiz.QuizSet.GENERIC);
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
		view.setOnOverwriteQuiz(this::overwriteQuiz);
		view.setOnStartQuiz(this::startQuiz);
		view.setOnQuizType(this::setQuizType);

		if (nonNull(quizEdit)) {
			view.setTitleText(context.getDictionary().get("create.quiz.edit.title"));
			view.setOverwriteQuiEnabled(true);

			this.quiz = quizEdit.clone();

			fillForm();
		}
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
		createQuiz();

		Document quizDoc = (selectedDoc == genericDoc) ? null : selectedDoc;

		try {
			quizService.saveQuiz(quiz, quizDoc);
		}
		catch (IOException e) {
			handleException(e, "Save quiz failed", "quiz.save.error");
		}

		quizEdit = quiz.clone();
		view.setOverwriteQuiEnabled(true);

		showNotificationPopup(context.getDictionary().get("create.quiz.saved"));
	}

	private void overwriteQuiz() {
		createQuiz();

		Document quizDoc = (selectedDoc == genericDoc) ? null : selectedDoc;

		try {
			quizService.deleteQuiz(quizEdit);
			quizService.saveQuiz(quiz, quizDoc);
		}
		catch (IOException e) {
			handleException(e, "Overwriting quiz failed", "quiz.overwrite.error");
		}

		showNotificationPopup(context.getDictionary().get("create.quiz.overwritten"));
	}

	private void startQuiz() {
		createQuiz();

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

		clearOptions();
		newOption();
	}

	private void documentSelected(Document doc) {
		if (doc == genericDoc) {
			setQuizSet(Quiz.QuizSet.GENERIC);

			this.selectedDoc = null;
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

	private void newOption() {
		switch (quiz.getType()) {
			case MULTIPLE:
			case SINGLE:
				addOptionView(viewFactory.getInstance(CreateQuizDefaultOptionView.class));
				break;

			case NUMERIC:
				addOptionView(viewFactory.getInstance(CreateQuizNumericOptionView.class));
				break;

			default:
				break;
		}
	}

	private void addOptionView(CreateQuizOptionView optionView) {
		view.addQuizOptionView(optionView);

		optionView.setOnMoveDown(() -> moveOptionDown(optionView));
		optionView.setOnMoveUp(() -> moveOptionUp(optionView));
		optionView.setOnRemove(() -> removeOption(optionView));
		optionView.setOnEnterKey(() -> enterKey(optionView));
		optionView.setOnTabKey(() -> tabKey(optionView));
		optionView.focus();

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

	private void enterKey(CreateQuizOptionView optionView) {
		if (!isLastOption(optionView)) {
			return;
		}

		startQuiz();
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
		String htmlBody = Jsoup.parse(view.getQuizText()).body().html();

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
		if (isNull(quiz.getQuizSet())) {
			setQuizSet(Quiz.QuizSet.GENERIC);
		}
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
					case MULTIPLE:
					case SINGLE: {
						CreateQuizOptionView optionView = viewFactory.getInstance(CreateQuizDefaultOptionView.class);
						optionView.setOptionText(optionText);

						addOptionView(optionView);
						break;
					}
					case NUMERIC: {
						MinMaxRule rule = (MinMaxRule) quiz.getInputFilter().getRules().get(i);

						CreateQuizNumericOptionView optionView = viewFactory.getInstance(CreateQuizNumericOptionView.class);
						optionView.setOptionText(optionText);
						optionView.setMinValue(rule.getMin());
						optionView.setMaxValue(rule.getMax());

						addOptionView(optionView);
						break;
					}
				}
			}
		}
	}
}
