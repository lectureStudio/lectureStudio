package org.lecturestudio.editor.api.presenter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.eventbus.Subscribe;
import org.junit.jupiter.api.BeforeEach;
import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.audio.DummyAudioSystemProvider;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.ConfirmationNotificationPresenter;
import org.lecturestudio.core.presenter.NotificationPresenter;
import org.lecturestudio.core.presenter.command.ConfirmationNotificationCommand;
import org.lecturestudio.core.presenter.command.NotificationCommand;
import org.lecturestudio.editor.api.config.DefaultConfiguration;
import org.lecturestudio.editor.api.context.EditorContext;

public abstract class PresenterTest extends org.lecturestudio.core.presenter.PresenterTest {
	GuiceInjector injector;

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

		context.getEventBus().register(new Object() {

			@Subscribe
			void onConfirmationNotificationContext(ConfirmationNotificationCommand command) {
				ConfirmationNotificationPresenter presenter = injector.getInstance(command.getPresenterClass());

				command.execute(presenter);
			}

			@Subscribe
			void onNotificationContext(NotificationCommand command) {
				NotificationPresenter presenter = injector.getInstance(command.getPresenterClass());

				command.execute(presenter);
			}

		});
	}

	abstract void setupInjector() throws Exception;
}
