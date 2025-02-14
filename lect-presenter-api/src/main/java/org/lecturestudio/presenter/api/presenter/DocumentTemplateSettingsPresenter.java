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
import java.util.concurrent.CompletableFuture;
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
import org.lecturestudio.presenter.api.config.DocumentTemplateConfiguration;
import org.lecturestudio.presenter.api.config.TemplateConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.view.DocumentTemplateSettingsView;

public class DocumentTemplateSettingsPresenter extends Presenter<DocumentTemplateSettingsView> {

	private final TemplateConfiguration templateConfig;

	private final TemplateConfiguration defaultTemplateConfig;

	private final ViewContextFactory viewFactory;


	@Inject
	DocumentTemplateSettingsPresenter(PresenterContext context,
			DocumentTemplateSettingsView view, ViewContextFactory viewFactory) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.templateConfig = context.getConfiguration().getTemplateConfig();
		this.defaultTemplateConfig = new DefaultConfiguration().getTemplateConfig();
	}

	@Override
	public void initialize() {
		var chatConfig = templateConfig.getChatMessageTemplateConfig();
		var hallConfig = templateConfig.getHallMessageTemplateConfig();
		var quizConfig = templateConfig.getQuizTemplateConfig();
		var whiteboardConfig = templateConfig.getWhiteboardTemplateConfig();

		bindTemplate(chatConfig, view::setChatMessagePage);
		bindTemplate(hallConfig, view::setHallMessagePage);
		bindTemplate(quizConfig, view::setQuizPage);
		bindTemplate(whiteboardConfig, view::setWhiteboardPage);

		view.bindChatMessageBounds(chatConfig.boundsProperty());
		view.bindHallMessageBounds(hallConfig.boundsProperty());
		view.bindQuizBounds(quizConfig.boundsProperty());
		view.setOnSelectChatMessageTemplatePath(this::selectChatMessageTemplatePath);
		view.setOnResetChatMessageTemplatePath(this::resetChatMessageTemplatePath);
		view.setOnSelectHallMessageTemplatePath(this::selectHallMessageTemplatePath);
		view.setOnResetHallMessageTemplatePath(this::resetHallMessageTemplatePath);
		view.setOnSelectQuizTemplatePath(this::selectQuizTemplatePath);
		view.setOnResetQuizTemplatePath(this::resetQuizTemplatePath);
		view.setOnSelectWhiteboardTemplatePath(this::selectWhiteboardTemplatePath);
		view.setOnResetWhiteboardTemplatePath(this::resetWhiteboardTemplatePath);
		view.setOnReset(this::reset);
	}

	private void bindTemplate(DocumentTemplateConfiguration config,
			BiConsumer<Page, PresentationParameter> consumer) {
		setTemplate(config.getTemplatePath(), consumer);

		config.templatePathProperty().addListener((o, oldValue, newValue) -> {
			setTemplate(newValue, consumer);
		});
	}

	private void setTemplate(String path,
			BiConsumer<Page, PresentationParameter> consumer) {
		try {
			Document document = getDocument(path);

			Page page = document.getCurrentPage();
			PresentationParameterProvider provider = context
					.getPagePropertyProvider(ViewType.User);

			consumer.accept(page, provider.getParameter(page));
		}
		catch (IOException e) {
			handleException(e, "Create template document failed",
					"template.settings.load.error");
		}
	}

	private void selectChatMessageTemplatePath() {
		selectTemplate(templateConfig.getChatMessageTemplateConfig()
				.templatePathProperty());
	}

	private void resetChatMessageTemplatePath() {
		resetTemplate(templateConfig.getChatMessageTemplateConfig(),
				defaultTemplateConfig.getChatMessageTemplateConfig());
	}

	private void selectHallMessageTemplatePath() {
		selectTemplate(templateConfig.getHallMessageTemplateConfig()
				.templatePathProperty());
	}

	private void resetHallMessageTemplatePath() {
		resetTemplate(templateConfig.getHallMessageTemplateConfig(),
				defaultTemplateConfig.getHallMessageTemplateConfig());
	}

	private void selectQuizTemplatePath() {
		selectTemplate(templateConfig.getQuizTemplateConfig()
				.templatePathProperty());
	}

	private void resetQuizTemplatePath() {
		resetTemplate(templateConfig.getQuizTemplateConfig(),
				defaultTemplateConfig.getQuizTemplateConfig());
	}

	private void selectWhiteboardTemplatePath() {
		selectTemplate(templateConfig.getWhiteboardTemplateConfig()
				.templatePathProperty());
	}

	private void resetWhiteboardTemplatePath() {
		resetTemplate(templateConfig.getWhiteboardTemplateConfig(),
				defaultTemplateConfig.getWhiteboardTemplateConfig());
	}

	private void selectTemplate(StringProperty property) {
		Dictionary dict = context.getDictionary();
		File initFile = getFile(property.get());

		CompletableFuture.runAsync(() -> {
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
		});
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

	private void resetTemplate(DocumentTemplateConfiguration tplConfig,
			DocumentTemplateConfiguration defConfig) {
		tplConfig.setTemplatePath(defConfig.getTemplatePath());
		tplConfig.setBounds(defConfig.getBounds());
	}

	private void reset() {
		DefaultConfiguration defaultConfig = new DefaultConfiguration();
		TemplateConfiguration defaultTemplateConfig = defaultConfig.getTemplateConfig();

		var defaultList = defaultTemplateConfig.getAll();
		var configList = templateConfig.getAll();

		for (int i = 0; i < configList.size(); i++) {
			var defaultTplConf = defaultList.get(i);
			var tplConf = configList.get(i);

			resetTemplate(tplConf, defaultTplConf);
		}
	}
}