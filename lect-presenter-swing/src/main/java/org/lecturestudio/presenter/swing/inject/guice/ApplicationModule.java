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

package org.lecturestudio.presenter.swing.inject.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.LocaleProvider;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.configuration.ConfigurationService;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.bus.AudioBus;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.camera.CameraDriver;
import org.lecturestudio.core.controller.PresentationController;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.service.DisplayService;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.AggregateBundle;
import org.lecturestudio.core.util.DirUtils;
import org.lecturestudio.core.view.PresentationViewFactory;
import org.lecturestudio.media.webrtc.WebRtcDriver;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfigService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;
import org.lecturestudio.presenter.api.service.QuizDataSource;
import org.lecturestudio.swing.AwtPresentationViewFactory;
import org.lecturestudio.swing.DefaultRenderContext;
import org.lecturestudio.swing.service.AwtDisplayService;

public class ApplicationModule extends AbstractModule {

	private final static Logger LOG = LogManager.getLogger(ApplicationModule.class);

	private static final AppDataLocator LOCATOR = new AppDataLocator("lecturePresenter");

	private static final File CONFIG_FILE = new File(LOCATOR.toAppDataPath("config.json"));


	@Override
	protected void configure() {
		bind(ApplicationContext.class).to(PresenterContext.class);
		bind(CameraDriver.class).to(WebRtcDriver.class);
		bind(ToolController.class).asEagerSingleton();
	}

	@Provides
	@Singleton
	FileLectureRecorder createFileLectureRecorder(DocumentService documentService, ApplicationContext context) throws IOException {
		return new FileLectureRecorder(
				documentService,
				context.getConfiguration().getAudioConfig(),
				((PresenterContext) context).getRecordingDirectory());
	}

	@Provides
	@Singleton
	QuizDataSource createQuizDataSource(ApplicationContext context) {
		return new QuizDataSource(new File(context.getDataLocator().toAppDataPath("quiz.txt")));
	}

	@Provides
	@Singleton
	RenderController createRenderController(ApplicationContext context) {
		return new RenderController(context, new DefaultRenderContext());
	}

	@Provides
	@Singleton
	PresentationController createPresentationController(ApplicationContext context) {
		// Handle presentation views with AWT to avoid slow FX scene graph updates.
		DisplayService displayService = new AwtDisplayService();
		PresentationViewFactory viewFactory = new AwtPresentationViewFactory();

		return new PresentationController(context, displayService, viewFactory);
	}

	@Provides
	@Singleton
	ResourceBundle createResourceBundle(Configuration config) throws Exception {
		LocaleProvider localeProvider = new LocaleProvider();
		Locale locale = localeProvider.getBestSupported(config.getLocale());

		return new AggregateBundle(locale, "resources.i18n.core", "resources.i18n.dict");
	}

	@Provides
	@Singleton
	AggregateBundle createAggregateBundle(ResourceBundle resourceBundle) {
		return (AggregateBundle) resourceBundle;
	}

	@Provides
	@Singleton
	DocumentService createDocumentService(ApplicationContext context) {
		return context.getDocumentService();
	}

	@Provides
	@Singleton
	PresenterContext createApplicationContext(Configuration config, Dictionary dict) {
		EventBus eventBus = ApplicationBus.get();
		EventBus audioBus = AudioBus.get();

		return new PresenterContext(LOCATOR, CONFIG_FILE, config, dict, eventBus, audioBus);
	}

	@Provides
	@Singleton
	ConfigurationService<PresenterConfiguration> provideConfigurationService() {
		ConfigurationService<PresenterConfiguration> configService = null;

		try {
			configService = new PresenterConfigService();
		}
		catch (Exception e) {
			LOG.error("Create configuration service failed", e);
		}

		return configService;
	}

	@Provides
	@Singleton
	Configuration provideConfiguration(ConfigurationService<PresenterConfiguration> configService) {
		Configuration configuration = null;

		try {
			DirUtils.createIfNotExists(Paths.get(LOCATOR.getAppDataPath()));

			if (!CONFIG_FILE.exists()) {
				// Create configuration with default values.
				configuration = new DefaultConfiguration();

				configService.save(CONFIG_FILE, (DefaultConfiguration) configuration);
			}
			else {
				configuration = configService.load(CONFIG_FILE, PresenterConfiguration.class);
			}

			// Set system default locale.
			LocaleProvider localeProvider = new LocaleProvider();
			configuration.setLocale(localeProvider.getBestSupported(configuration.getLocale()));
		}
		catch (Exception e) {
			LOG.error("Create configuration failed", e);
		}

		return configuration;
	}

	@Provides
	@Singleton
	Dictionary provideDictionary(ResourceBundle resourceBundle) {
		return new Dictionary() {

			@Override
			public String get(String key) throws NullPointerException {
				return resourceBundle.getString(key);
			}

			@Override
			public boolean contains(String key) {
				return resourceBundle.containsKey(key);
			}
		};
	}

}
