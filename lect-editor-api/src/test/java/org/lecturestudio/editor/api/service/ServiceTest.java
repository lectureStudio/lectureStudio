package org.lecturestudio.editor.api.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.editor.api.config.DefaultConfiguration;
import org.lecturestudio.editor.api.context.EditorContext;

public abstract class ServiceTest extends org.lecturestudio.core.service.ServiceTest {

	protected EditorContext context;
	protected GuiceInjector injector;

	@BeforeEach
	protected void setUpServiceTest() throws IOException, URISyntaxException {
		Path root = getResourcePath(".");
		testPath = root.resolve("AppData");
		new File(testPath.toUri()).mkdirs();

		Configuration config = new DefaultConfiguration();

		EventBus eventBus = new EventBus();
		EventBus audioBus = new EventBus();

		Document document = new Document();
		document.createPage();

		AppDataLocator locator = new AppDataLocator(testPath.toString());
		context = new EditorContext(locator, null, config, dict, eventBus, audioBus) {

			@Override
			public void saveConfiguration() {

			}
		};

		context.getEventBus().register(new Object() {

		});
	}

	protected abstract void setupInjector() throws Exception;


	@Override
	protected ApplicationContext getApplicationContext() {
		return context;
	}
}
