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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.eventbus.Subscribe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.NotificationPresenter;
import org.lecturestudio.core.presenter.command.NotificationCommand;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;

abstract class PresenterTest extends org.lecturestudio.core.presenter.PresenterTest {

	protected FileLectureRecorder recorder = null;
	protected ApplicationContext context;

	@BeforeEach
	protected void setupPresenterTest() throws IOException, URISyntaxException {
		Path root = getResourcePath(".");
		testPath = root.resolve("AppData");

		notifyViewRef = new AtomicReference<>();

		Configuration config = new DefaultConfiguration();

		EventBus eventBus = new EventBus();
		EventBus audioBus = new EventBus();

		Dictionary dict = new Dictionary() {

			@Override
			public String get(String key) throws NullPointerException {
				return key;
			}

			@Override
			public boolean contains(String key) {
				return true;
			}
		};

		Document document = new Document();
		document.createPage();

		AppDataLocator locator = new AppDataLocator(testPath.toString());

		context = new PresenterContext(locator, null, config, dict, eventBus, audioBus, null) {

			public String getRecordingDirectory() {
				return testPath.resolve("recording").toString();
			}

			@Override
			public void saveConfiguration() {

			}
		};
		context.getEventBus().register(new Object() {

			@Subscribe
			void onNotificationContext(NotificationCommand command) {
				NotificationPresenter presenter = viewFactory.getInstance(command.getPresenterClass());

				command.execute(presenter);

				notifyViewRef.set((NotificationMockView) presenter.getView());
			}

		});
		context.getDocumentService().addDocument(document);
		context.getDocumentService().selectDocument(document);

		viewFactory = new ViewContextMockFactory();
	}

	@AfterEach
	protected void destroyRecorder() {
		try {
			recorder.stop();
		}
		catch (Exception ignored) {
		}
	}

	protected String getRecordingDirectory() {
		return ((PresenterContext) context).getRecordingDirectory();
	}


	@Override
	protected ApplicationContext getApplicationContext() {
		return context;
	}

}

