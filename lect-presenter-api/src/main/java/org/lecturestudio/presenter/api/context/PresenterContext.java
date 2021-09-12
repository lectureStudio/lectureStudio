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

package org.lecturestudio.presenter.api.context;

import java.io.File;
import java.util.List;

import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.configuration.ConfigurationService;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.presenter.api.config.PresenterConfigService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.model.Bookmarks;
import org.lecturestudio.presenter.api.service.BookmarkService;
import org.lecturestudio.presenter.api.service.QuizDataSource;
import org.lecturestudio.presenter.api.service.QuizService;
import org.lecturestudio.web.api.model.quiz.Quiz;

public class PresenterContext extends ApplicationContext {

	public static final String SLIDES_CONTEXT = "Slides";
	public static final String SLIDES_TO_PDF_CONTEXT = "SlidesToPDF";
	public static final String SLIDES_EXTENSION = "pdf";

	public static final String RECORDING_CONTEXT = "Recording";
	public static final String RECORDING_EXTENSION = "presenter";

	private final BooleanProperty streamStarted = new BooleanProperty();

	private final BooleanProperty hasRecordedChanges = new BooleanProperty();

	private final BooleanProperty showOutline = new BooleanProperty();

	private final File configFile;

	private final String recordingDir;

	private final BookmarkService bookmarkService;

	private final QuizService quizService;

    private Quiz lastQuiz;


	public PresenterContext(AppDataLocator dataLocator, File configFile, Configuration config, Dictionary dict, EventBus eventBus, EventBus audioBus) {
		super(dataLocator, config, dict, eventBus, audioBus);

		File quizFile = new File(dataLocator.toAppDataPath("quiz.txt"));

		bookmarkService = new BookmarkService(getDocumentService());
		quizService = new QuizService(new QuizDataSource(quizFile), getDocumentService());

		this.configFile = configFile;
		this.recordingDir = getDataLocator().toAppDataPath("recording");
	}

	@Override
	public void saveConfiguration() throws Exception {
		ConfigurationService<PresenterConfiguration> configService = new PresenterConfigService();
		configService.save(configFile, (PresenterConfiguration) getConfiguration());
	}

	public void setHasRecordedChanges(boolean changes) {
		hasRecordedChanges.set(changes);
	}

	public boolean hasRecordedChanges() {
		return hasRecordedChanges.get();
	}

	public BooleanProperty hasRecordedChangesProperty() {
		return hasRecordedChanges;
	}

	public void setStreamStarted(boolean started) {
		streamStarted.set(started);
	}

	public boolean getStreamStarted() {
		return streamStarted.get();
	}

	public BooleanProperty streamStartedProperty() {
		return streamStarted;
	}

	public void setShowOutline(boolean show) {
		showOutline.set(show);
	}

	public BooleanProperty showOutlineProperty() {
		return showOutline;
	}

	public String getRecordingDirectory() {
		return recordingDir;
	}

	public void setLastQuiz(Quiz quiz) {
        this.lastQuiz = quiz;
    }

    public Quiz getLastQuiz() {
        return lastQuiz;
    }

    public BookmarkService getBookmarkService() {
		return bookmarkService;
	}

    public QuizService getQuizService() {
		return quizService;
	}

    public List<Quiz> getQuizzes() {
		try {
			return quizService.getQuizzes();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

    public List<Quiz> getQuizzes(Document doc) {
		try {
			return quizService.getQuizzes(doc);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Bookmarks getBookmarks() {
		return bookmarkService.getBookmarks();
	}

}
