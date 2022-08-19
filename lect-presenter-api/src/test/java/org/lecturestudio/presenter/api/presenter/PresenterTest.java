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

import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.NotificationPresenter;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.command.NotificationCommand;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.lecturestudio.presenter.audio.DummyAudioSystemProvider;

abstract class PresenterTest {

	Path testPath;

	PresenterContext context;

	AudioSystemProvider audioSystemProvider;

	ViewContextFactory viewFactory;

	AtomicReference<NotificationMockView> notifyViewRef;


	@BeforeEach
	void setupPresenterTest() throws IOException, URISyntaxException {
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

		audioSystemProvider = new DummyAudioSystemProvider();
	}

	@AfterEach
	void destroyPresenterTest() throws IOException {
		for (Document doc : context.getDocumentService().getDocuments().getPdfDocuments()) {
			doc.close();
		}

		deletePath(Path.of(new File("AppData").getAbsolutePath()));
		deletePath(testPath);
	}

	private void deletePath(Path path) throws IOException {
		if (!Files.exists(path)) {
			return;
		}

		Files.walkFileTree(path, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.deleteIfExists(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@SuppressWarnings("unchecked")
	<T> T createProxy(Class<T> cls) {
		return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { cls }, (proxy, method, args) -> {
			return proxy;
		});
	}

	Path getResourcePath(String path) throws URISyntaxException {
		return Path.of(Objects.requireNonNull(
				getClass().getClassLoader().getResource(path)).toURI());
	}



	class ViewContextMockFactory implements ViewContextFactory {

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getInstance(Class<T> cls) {
			if (cls == NotificationView.class) {
				return (T) new NotificationMockView();
			}
			if (Presenter.class.isAssignableFrom(cls)) {
				for (var ctor : cls.getDeclaredConstructors()) {
					Class<?>[] paramTypes = ctor.getParameterTypes();
					Object[] input = new Object[paramTypes.length];

					for (int i = 0; i < paramTypes.length; i++) {
						Class<?> type = paramTypes[i];

						if (type.equals(NotificationView.class)) {
							input[i] = new NotificationMockView();
						}
						else if (type.equals(ApplicationContext.class)) {
							input[i] = context;
						}
					}

					try {
						ctor.setAccessible(true);
						return (T) ctor.newInstance(input);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { cls }, (proxy, method, args) -> {
				return proxy;
			});
		}

		@Override
		public FileChooserView createFileChooserView() {
			return new FileChooserMockView();
		}

		@Override
		public DirectoryChooserView createDirectoryChooserView() {
			return new DirectoryChooserMockView();
		}

	}



	static class FileChooserMockView implements FileChooserView {

		String description;

		String[] extensions;

		String initialFileName;

		File directory;

		View parent;


		@Override
		public void addExtensionFilter(String description, String... extensions) {
			this.description = description;
			this.extensions = extensions;
		}

		@Override
		public void setInitialDirectory(File directory) {
			this.directory = directory;
		}

		@Override
		public void setInitialFileName(String name) {
			initialFileName = name;
		}

		@Override
		public File showOpenFile(View parent) {
			this.parent = parent;
			return null;
		}

		@Override
		public File showSaveFile(View parent) {
			this.parent = parent;
			return null;
		}
	}



	static class DirectoryChooserMockView implements DirectoryChooserView {

		File directory;

		String title;

		View parent;


		@Override
		public void setInitialDirectory(File directory) {
			this.directory = directory;
		}

		@Override
		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public File show(View parent) {
			this.parent = parent;
			return null;
		}
	}



	static class NotificationMockView implements NotificationView {

		NotificationType type;

		String title;

		String message;


		@Override
		public void setType(NotificationType type) {
			this.type = type;
		}

		@Override
		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public void setMessage(String message) {
			this.message = message;
		}

		@Override
		public void setOnClose(Action action) {

		}
	}

}
