package org.lecturestudio.core.presenter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.lecturestudio.core.CoreTest;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewContextFactory;

public abstract class PresenterTest extends CoreTest {

	protected ViewContextFactory viewFactory;
	protected AtomicReference<NotificationMockView> notifyViewRef;

	@AfterEach
	void destroyPresenterTest() throws IOException {
		ApplicationContext context = getApplicationContext();
		if (context != null) {
			for (Document doc : context.getDocumentService().getDocuments().getPdfDocuments()) {
				doc.close();
			}
		}

		deletePath(Path.of(new File("AppData").getAbsolutePath()));
		deletePath(testPath);
	}



	@SuppressWarnings("unchecked")
	protected <T> T createProxy(Class<T> cls) {
		return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{cls}, (proxy, method, args) -> {
			return proxy;
		});
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
							input[i] = getApplicationContext();
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


	public static class FileChooserMockView implements FileChooserView {

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
			return new File(directory, initialFileName);
		}
	}


	public static class DirectoryChooserMockView implements DirectoryChooserView {

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


	public static class NotificationMockView implements NotificationView {

		public NotificationType type;

		public String title;

		public String message;

		public Action closeAction;


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
			this.closeAction = action;
		}
	}

}
