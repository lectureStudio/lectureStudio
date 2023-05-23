package org.lecturestudio.core.presenter;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewContextFactory;

public abstract class PresenterTest {

	protected Path testPath;

	protected ApplicationContext context;

	protected AudioSystemProvider audioSystemProvider;

	protected ViewContextFactory viewFactory;

	protected AtomicReference<NotificationMockView> notifyViewRef;

	protected Dictionary dict;

	@BeforeEach
	public void setUpDictionary() {
		dict = new Dictionary() {

			@Override
			public String get(String key) throws NullPointerException {
				return key;
			}

			@Override
			public boolean contains(String key) {
				return true;
			}
		};
	}


	@AfterEach
	void destroyPresenterTest() throws IOException {
		for (Document doc : context.getDocumentService().getDocuments().getPdfDocuments()) {
			doc.close();
		}

		deletePath(Path.of(new File("AppData").getAbsolutePath()));
		deletePath(testPath);
	}

	protected void deletePath(Path path) throws IOException {
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
	protected <T> T createProxy(Class<T> cls) {
		return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{cls}, (proxy, method, args) -> {
			return proxy;
		});
	}

	protected Path getResourcePath(String path) throws URISyntaxException {
		return Path.of(Objects.requireNonNull(
				getClass().getClassLoader().getResource(path)).toURI());
	}

	public class ViewContextMockFactory implements ViewContextFactory {

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
			return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{cls}, (proxy, method, args) -> {
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


	protected static class FileChooserMockView implements FileChooserView {

		public String description;

		public String[] extensions;

		public String initialFileName;

		public File directory;

		public View parent;


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


	protected static class DirectoryChooserMockView implements DirectoryChooserView {

		public File directory;

		public String title;

		public View parent;


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


	protected static class NotificationMockView implements NotificationView {

		public NotificationType type;

		public String title;

		public String message;


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
