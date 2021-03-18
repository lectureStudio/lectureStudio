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

import static java.util.Objects.nonNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.ProgressPresenter;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ProgressView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.service.WebService;
import org.lecturestudio.presenter.api.view.SaveQuizResultsView;

public class SaveQuizResultsPresenter extends Presenter<SaveQuizResultsView> {

	private final SimpleDateFormat dateFormat;

	private final Set<String> selectedFiles;

	private final ViewContextFactory viewFactory;

	private final WebService webService;

	private final StringProperty savePath;


	@Inject
	SaveQuizResultsPresenter(ApplicationContext context, SaveQuizResultsView view,
			ViewContextFactory viewFactory, WebService webService) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.webService = webService;
		this.dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		this.selectedFiles = new HashSet<>();
		this.savePath = new StringProperty();
	}

	@Override
	public void initialize() {
		savePath.set(System.getProperty("user.home") + File.separator + getFileName());

		selectCSV(true);
		selectPDF(true);

		view.setSavePath(savePath);
		view.selectCsvOption(true);
		view.selectPdfOption(true);
		view.setOnCsvSelection(this::selectCSV);
		view.setOnPdfSelection(this::selectPDF);
		view.setOnSave(this::save);
		view.setOnSelectPath(this::selectSavePath);
		view.setOnClose(this::close);
	}

	private void save() {
		List<String> files = new ArrayList<>();

		for (String ext : selectedFiles) {
			files.add(savePath + "." + ext);
		}

		context.getEventBus().post(new ShowPresenterCommand<>(ProgressPresenter.class) {
			@Override
			public void execute(ProgressPresenter presenter) {
				ProgressView progressView = presenter.getView();
				progressView.setTitle(context.getDictionary().get("save.quiz.result.saving"));
				progressView.setMessage(savePath.get());
				progressView.setOnViewShown(() -> {
					saveAsync(progressView, files);
				});
			}
		});
	}

	private void saveAsync(ProgressView progressView, List<String> files) {
		CompletableFuture.runAsync(() -> {
			try {
				webService.saveQuizResult(files, progressView::setProgress);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		})
		.thenRun(() -> {
			progressView.setTitle(context.getDictionary().get("quiz.save.result.success"));
		})
		.exceptionally(throwable -> {
			logException(throwable, "Save quiz result failed");

			progressView.setError(context.getDictionary().get("save.quiz.result.error"));
			progressView.setMessage(context.getDictionary().get("quiz.save.error"));
			return null;
		});
	}

	private void selectSavePath() {
		File initPath = new File(savePath.get());

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.addExtensionFilter("CSV Files", "csv");
		fileChooser.addExtensionFilter("PDF Files", "pdf");
		fileChooser.setInitialFileName(initPath.getName());
		fileChooser.setInitialDirectory(initPath.getParentFile());

		File selectedFile = fileChooser.showSaveFile(view);

		if (nonNull(selectedFile)) {
			savePath.set(FileUtils.stripExtension(selectedFile).getAbsolutePath());
		}
	}

	private void selectCSV(boolean select) {
		selectDocument("csv", select);
	}

	private void selectPDF(boolean select) {
		selectDocument("pdf", select);
	}

	private void selectDocument(String extension, boolean select) {
		if (select) {
			selectedFiles.add(extension);
		}
		else {
			selectedFiles.remove(extension);
		}
	}

	private String getFileName() {
		String docName = context.getDictionary().get("quiz");
		String date = dateFormat.format(new Date());

		return docName + "-" + date;
	}
}
