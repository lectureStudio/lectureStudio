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

import org.jsoup.Jsoup;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentList;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.DocumentQuiz;
import org.lecturestudio.presenter.api.service.QuizService;
import org.lecturestudio.presenter.api.service.StreamService;
import org.lecturestudio.presenter.api.view.*;
import org.lecturestudio.web.api.filter.MinMaxRule;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizSet;
import org.lecturestudio.web.api.model.quiz.QuizOption;

public class CreateQuizPresenter extends Presenter<CreateQuizView> {

	/** List of quiz option views that are currently displayed in the form. */
	private final List<CreateQuizOptionView> optionContextList;

	/** Factory for creating view components used in the quiz creation interface. */
	private final ViewContextFactory viewFactory;

	/** Service for document management and retrieval operations. */
	private final DocumentService documentService;

	/** Document representing a generic quiz option that's not associated with any specific document. */
	private final Document genericDoc;

	/** Service for quiz storage and management operations. */
	private final QuizService quizService;

	/** Service for streaming quizzes to participants during a session. */
	private final StreamService streamService;

	/** The Quiz created by the user. */
	private Quiz quiz = new Quiz();

	/** The Quiz to edit. */
	private Quiz quizEdit;

	/** The action that is executed when the user clicks the 'Start Quiz' button. */
	private Action startQuizAction;

	/** The currently selected document for which a quiz is being created or edited. */
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
		this.genericDoc = ((PresenterContext) context).getGenericDocument();
	}

	@Override
	public void initialize() throws Exception {
		DocumentList docList = documentService.getDocuments();

		if (isNull(selectedDoc)) {
			selectedDoc = docList.getSelectedDocument();

			if (!selectedDoc.isPDF()) {
				selectedDoc = docList.getLastNonWhiteboard();

				if (isNull(selectedDoc)) {
					selectedDoc = genericDoc;
				}
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

	/**
	 * Returns the current quiz being created or edited.
	 *
	 * @return the current quiz instance.
	 */
	public Quiz getQuiz() {
		return quiz;
	}

	/**
	 * Sets a quiz to be edited in this presenter.
	 *
	 * @param quiz the quiz to be edited.
	 */
	public void setQuiz(Quiz quiz) {
		this.quizEdit = quiz;
	}

	/**
	 * Sets the currently selected document for which a quiz is being created.
	 *
	 * @param document the document to set as selected.
	 */
	public void setSelectedDoc(Document document) {
		this.selectedDoc = document;
	}

	/**
	 * Sets the action to be executed when the user starts a quiz.
	 *
	 * @param action the action to execute when starting a quiz.
	 */
	public void setOnStartQuiz(Action action) {
		this.startQuizAction = action;
	}

	/**
	 * Saves the current quiz to the quiz service.
	 * <p>
	 * This method creates a quiz from the current form data and saves it using the quiz service.
	 * If editing an existing quiz, it replaces the old quiz with the new one.
	 * The quiz is saved either as a generic quiz or as document-specific depending on the current selection.
	 */
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

	/**
	 * Saves the current quiz and prepares the form for creating a new quiz.
	 */
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

	/**
	 * Creates a quiz from the current form data and starts it immediately.
	 * If an error occurs during quiz creation, it will be handled by the
	 * application's exception handling mechanism.
	 */
	private void startQuiz() {
		try {
			createQuiz();
		}
		catch (Exception e) {
			handleException(e, "Create quiz failed", "quiz", "quiz.save.error");
		}

		streamService.startQuiz(new DocumentQuiz(selectedDoc, quiz));

		close();

		if (nonNull(startQuizAction)) {
			startQuizAction.execute();
		}
	}

	/**
	 * Sets the quiz set type for the current quiz.
	 *
	 * @param set the quiz set type to assign to the quiz (GENERIC or DOCUMENT_SPECIFIC).
	 */
	private void setQuizSet(Quiz.QuizSet set) {
		quiz.setQuizSet(set);
	}

	/**
	 * Sets the quiz type for the current quiz and updates the option views accordingly.
	 * This method recreates all option elements to match the new quiz type while preserving
	 * existing option data when possible.
	 *
	 * @param type the quiz type to set (MULTIPLE, SINGLE, NUMERIC, or FREE_TEXT).
	 */
	private void setQuizType(Quiz.QuizType type) {
		quiz.setType(type);

		// Re-create option elements for the new type.
		List<CreateQuizOptionView> optionViews = new ArrayList<>();

		for (CreateQuizOptionView currentView : optionContextList) {
			CreateQuizOptionView newView = createOption();
			newView.setOption(currentView.getOption());

			optionViews.add(newView);
		}

		clearOptions();

		if (type == Quiz.QuizType.FREE_TEXT) {
			CreateQuizOptionView newView = createOption();
			addOptionView(newView, false);
		}
		else {
			optionViews.forEach(view -> addOptionView(view, false));
		}
	}

	/**
	 * Determines if the currently selected document is the generic document.
	 *
	 * @return true if the selected document is the generic document.
	 */
	private boolean isGenericSet() {
		return selectedDoc == genericDoc;
	}

	/**
	 * Handles document selection events and updates the quiz set type accordingly.
	 * <p>
	 * If the generic document is selected, the quiz will be set to GENERIC type.
	 * Otherwise, it will be set to DOCUMENT_SPECIFIC type.
	 *
	 * @param doc the document that was selected.
	 */
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

	/**
	 * Removes all quiz options from the view and clears the internal option list.
	 */
	private void clearOptions() {
		view.clearOptions();
		optionContextList.clear();
	}

	/**
	 * Creates an appropriate quiz option view based on the current quiz type.
	 * <p>
	 * This factory method returns different types of option views depending on
	 * whether the quiz is multiple-choice, single-choice, numeric, or free text.
	 *
	 * @return a new quiz option view appropriate for the current quiz type.
	 */
	private CreateQuizOptionView createOption() {
		return switch (quiz.getType()) {
			case MULTIPLE, SINGLE ->
					viewFactory.getInstance(CreateQuizDefaultOptionView.class);
			case NUMERIC ->
					viewFactory.getInstance(CreateQuizNumericOptionView.class);
			case FREE_TEXT ->
					viewFactory.getInstance(CreateQuizFreeTextOptionView.class);
		};
	}

	/**
	 * Creates and adds a new option to the quiz form.
	 * <p>
	 * The new option will be focused on immediate editing.
	 */
	private void newOption() {
		addOptionView(createOption(), true);
	}

	/**
	 * Adds a quiz option view to the UI and sets up its event handlers.
	 * <p>
	 * This method adds the provided option view to the quiz form, configures its event handlers
	 * for moving up/down, removing, and tab key navigation. It also adds special behavior for
	 * single-choice quizzes to ensure mutual exclusivity of correct answers.
	 *
	 * @param optionView the quiz option view to add to the form.
	 * @param focus      whether to set focus on the new option view after adding it.
	 */
	private void addOptionView(CreateQuizOptionView optionView, boolean focus) {
		view.addQuizOptionView(optionView);

		optionView.setOnMoveDown(() -> moveOptionDown(optionView));
		optionView.setOnMoveUp(() -> moveOptionUp(optionView));
		optionView.setOnRemove(() -> removeOption(optionView));
		optionView.setOnTabKey(() -> tabKey(optionView));

		if (focus) {
			optionView.focus();
		}

		// For single-choice quizzes, ensure mutual exclusivity of correct answers
		// by adding a listener that unchecks all other options when one is marked as correct.
		if (quiz.getType() == Quiz.QuizType.SINGLE) {
			optionView.addOnChangeCorrect(correct -> {
				// Uncheck all other options when a single option is selected.
				for (CreateQuizOptionView view : optionContextList) {
					if (view != optionView) {
						view.getOption().setCorrect(false);
					}
				}
			});
		}

		optionContextList.add(optionView);
	}

	/**
	 * Removes a quiz option view from the UI and the internal option list.
	 * <p>
	 * This method removes the specified option view from both the visual presentation
	 * and the internal tracking list of options.
	 *
	 * @param optionView the quiz option view to remove.
	 */
	private void removeOption(CreateQuizOptionView optionView) {
		view.removeQuizOptionView(optionView);

		optionContextList.remove(optionView);
	}

	/**
	 * Moves a quiz option up in the display order (one position earlier).
	 * <p>
	 * This method updates both the visual presentation in the view and the internal
	 * data structure to reflect the new order of options. The operation is ignored
	 * if the option is already at the top of the list.
	 *
	 * @param optionView the quiz option view to move up.
	 */
	private void moveOptionUp(CreateQuizOptionView optionView) {
		int index = optionContextList.indexOf(optionView);
		int newIndex = index - 1;

		if (index < 0 || newIndex < 0) {
			return;
		}

		view.moveQuizOptionViewUp(optionView);

		Collections.swap(optionContextList, index, newIndex);
	}

	/**
	 * Moves a quiz option down in the display order (one position later).
	 * <p>
	 * This method updates both the visual presentation in the view and the internal
	 * data structure to reflect the new order of options. The operation is ignored
	 * if the option is already at the bottom of the list.
	 *
	 * @param optionView the quiz option view to move down.
	 */
	private void moveOptionDown(CreateQuizOptionView optionView) {
		int index = optionContextList.indexOf(optionView);
		int newIndex = index + 1;

		if (index < 0 || newIndex >= optionContextList.size()) {
			return;
		}

		view.moveQuizOptionViewDown(optionView);

		Collections.swap(optionContextList, index, newIndex);
	}

	/**
	 * Handles tab key behavior for quiz option views.
	 * <p>
	 * When the tab key is pressed on the last option in the list, this method will
	 * create a new option, providing a seamless way to add multiple options.
	 *
	 * @param optionView the quiz option view where the tab key was pressed.
	 */
	private void tabKey(CreateQuizOptionView optionView) {
		if (!isLastOption(optionView)) {
			return;
		}

		newOption();
	}

	/**
	 * Determines if the provided option view is the last one in the option list.
	 * <p>
	 * This method is used to control tab key behavior and potentially other
	 * operations that need to know if an option is at the end of the list.
	 *
	 * @param optionView the quiz option view to check.
	 *
	 * @return true if the option is the last one in the list, false otherwise.
	 */
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
		quiz.setComment(view.getQuizComment());
		quiz.clearOptions();
		quiz.clearInputFilter();

		for (int index = 0; index < optionContextList.size(); index++) {
			CreateQuizOptionView view = optionContextList.get(index);
			QuizOption option = view.getOption();

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

	/**
	 * Fills the quiz creation form with data from an existing quiz that is being edited.
	 * <p>
	 * This method populates all form fields with values from the {@code quiz} object:
	 * <ul>
	 *   <li>Sets the quiz question text</li>
	 *   <li>Sets the quiz set type (generic or document-specific)</li>
	 *   <li>Sets the quiz type (multiple choice, single choice, numeric, or free text)</li>
	 *   <li>Sets the quiz comment</li>
	 *   <li>Creates and configures option views for each existing quiz option</li>
	 *   <li>For numeric quizzes, retrieves and applies min/max rules from the input filter</li>
	 * </ul>
	 * This method is called during initialization when editing an existing quiz.
	 */
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
			view.setQuizComment(quiz.getComment());

			List<QuizOption> options = quiz.getOptions();

			for (int i = 0; i < options.size(); i++) {
				QuizOption option = options.get(i);

				switch (quiz.getType()) {
					case MULTIPLE, SINGLE -> {
						CreateQuizOptionView optionView = viewFactory.getInstance(CreateQuizDefaultOptionView.class);
						optionView.setOption(option);

						addOptionView(optionView, false);
					}
					case NUMERIC -> {
						MinMaxRule rule = (MinMaxRule) quiz.getInputFilter().getRules().get(i);

						CreateQuizNumericOptionView optionView = viewFactory.getInstance(CreateQuizNumericOptionView.class);
						optionView.setOption(option);

						if (rule.getMin() != Integer.MIN_VALUE) {
							optionView.setMinValue(rule.getMin());
						}
						if (rule.getMax() != Integer.MAX_VALUE) {
							optionView.setMaxValue(rule.getMax());
						}

						addOptionView(optionView, false);
					}
					case FREE_TEXT -> {
						CreateQuizOptionView optionView = viewFactory.getInstance(CreateQuizFreeTextOptionView.class);
						optionView.setOption(option);

						addOptionView(optionView, false);
					}
				}
			}
		}
	}
}
