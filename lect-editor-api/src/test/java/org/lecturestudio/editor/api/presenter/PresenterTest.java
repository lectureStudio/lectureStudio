package org.lecturestudio.editor.api.presenter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.audio.DummyAudioSystemProvider;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.editor.api.config.DefaultConfiguration;
import org.lecturestudio.editor.api.context.EditorContext;

public abstract class PresenterTest extends org.lecturestudio.core.presenter.PresenterTest {

	@BeforeEach
	void setupPresenterTest() throws IOException, URISyntaxException {
		Path root = getResourcePath(".");
		testPath = root.resolve("AppData");

		notifyViewRef = new AtomicReference<>();

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

		viewFactory = new ViewContextMockFactory();

		audioSystemProvider = new DummyAudioSystemProvider();
	}
}
