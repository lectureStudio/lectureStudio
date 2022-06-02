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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.TemplateConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.view.DocumentTemplateSettingsView;

public class DocumentTemplateSettingsPresenter extends Presenter<DocumentTemplateSettingsView> {

	private final TemplateConfiguration templateConfig;

	private final ViewContextFactory viewFactory;


	@Inject
	DocumentTemplateSettingsPresenter(PresenterContext context,
			DocumentTemplateSettingsView view, ViewContextFactory viewFactory) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.templateConfig = context.getConfiguration().getTemplateConfig();
	}

	@Override
	public void initialize() {
		String chatTemplate = templateConfig.getChatMessageTemplatePath();
		String hallTemplate = templateConfig.getHallMessageTemplatePath();
		String quizTemplate = templateConfig.getQuizTemplatePath();
		String whiteboardTemplate = templateConfig.getWhiteboardTemplatePath();

		view.setOnSelectChatMessageTemplatePath(this::selectChatMessageTemplatePath);
		view.setOnSelectHallMessageTemplatePath(this::selectHallMessageTemplatePath);
		view.setOnSelectQuizTemplatePath(this::selectQuizTemplatePath);
		view.setOnSelectWhiteboardTemplatePath(this::selectWhiteboardTemplatePath);
		view.setOnReset(this::reset);

		setTemplate(chatTemplate, view::setChatMessagePage);
		setTemplate(hallTemplate, view::setHallMessagePage);
		setTemplate(quizTemplate, view::setQuizPage);
		setTemplate(whiteboardTemplate, view::setWhiteboardPage);

		templateConfig.chatMessageTemplatePathProperty().addListener((o, oldValue, newValue) -> {
			setTemplate(newValue, view::setChatMessagePage);
		});
		templateConfig.hallMessageTemplatePathProperty().addListener((o, oldValue, newValue) -> {
			setTemplate(newValue, view::setHallMessagePage);
		});
		templateConfig.quizTemplatePathProperty().addListener((o, oldValue, newValue) -> {
			setTemplate(newValue, view::setQuizPage);
		});
		templateConfig.whiteboardTemplatePathProperty().addListener((o, oldValue, newValue) -> {
			setTemplate(newValue, view::setWhiteboardPage);
		});
	}

	private void setTemplate(String path, BiConsumer<Page, PresentationParameter> consumer) {
		try {
			Document document = getDocument(path);

			Page page = document.getCurrentPage();
			PresentationParameterProvider provider = context.getPagePropertyProvider(ViewType.User);

			consumer.accept(page, provider.getParameter(page));
		}
		catch (IOException e) {
			handleException(e, "Create template document failed",
					"template.settings.load.error");
		}
	}

	private void selectChatMessageTemplatePath() {
		selectTemplate(templateConfig.chatMessageTemplatePathProperty());
	}

	private void selectHallMessageTemplatePath() {
		selectTemplate(templateConfig.hallMessageTemplatePathProperty());
	}

	private void selectQuizTemplatePath() {
		selectTemplate(templateConfig.quizTemplatePathProperty());
	}

	private void selectWhiteboardTemplatePath() {
		selectTemplate(templateConfig.whiteboardTemplatePathProperty());
	}

	private void selectTemplate(StringProperty property) {
		Dictionary dict = context.getDictionary();
		File initFile = getFile(property.get());

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.addExtensionFilter(dict.get("file.description.pdf"),
				PresenterContext.SLIDES_EXTENSION);

		if (initFile.exists()) {
			fileChooser.setInitialDirectory(initFile.getParentFile());
			fileChooser.setInitialFileName(initFile.getName());
		}

		File selectedFile = fileChooser.showOpenFile(view);

		if (nonNull(selectedFile)) {
			property.set(selectedFile.getAbsolutePath());
		}
	}

	private Document getDocument(String path) throws IOException {
		File file = getFile(path);
		Document document;

		if (file.exists()) {
			document = new Document(file);
		}
		else {
			document = new Document();
			document.setDocumentType(DocumentType.WHITEBOARD);
			document.createPage();
		}

		return document;
	}

	private File getFile(String path) {
		return new File(nonNull(path) ? path : "");
	}

	private void reset() {
		DefaultConfiguration defaultConfig = new DefaultConfiguration();
		TemplateConfiguration defaultTemplateConfig = defaultConfig.getTemplateConfig();

		templateConfig.setChatMessageTemplatePath(defaultTemplateConfig.getChatMessageTemplatePath());
		templateConfig.setHallMessageTemplatePath(defaultTemplateConfig.getHallMessageTemplatePath());
		templateConfig.setQuizTemplatePath(defaultTemplateConfig.getQuizTemplatePath());
		templateConfig.setWhiteboardTemplatePath(defaultTemplateConfig.getWhiteboardTemplatePath());
	}
}