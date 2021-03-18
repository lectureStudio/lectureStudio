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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.view.SaveDocumentOptionView;
import org.lecturestudio.presenter.api.view.SaveDocumentsView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaveDocumentsPresenterTest extends PresenterTest {

	@BeforeEach
	void setup() throws IOException {
		Document document = new Document();
		document.createPage();
		document.createPage();
		document.createPage();

		DocumentService documentService = context.getDocumentService();
		documentService.addDocument(document);
	}

	@Test
	void testInit() {
		AtomicInteger optionViewCount = new AtomicInteger(0);
		AtomicReference<String> pathRef = new AtomicReference<>();

		SaveDocumentsMockView view = new SaveDocumentsMockView() {
			@Override
			public void addDocumentOptionView(SaveDocumentOptionView optionView) {
				optionViewCount.incrementAndGet();
			}

			@Override
			public void setSavePath(StringProperty path) {
				pathRef.set(path.get());
			}
		};

		SaveDocumentsPresenter presenter = new SaveDocumentsPresenter(context, view, viewFactory, context.getDocumentService());
		presenter.initialize();

		String savePath = System.getProperty("user.home") + File.separator + getFileName();

		assertEquals(2, optionViewCount.get());
		assertEquals(savePath, pathRef.get());
	}

	@Test
	void testSelectPath() {
		AtomicReference<FileChooserMockView> chooserRef = new AtomicReference<>();
		AtomicReference<String> pathRef = new AtomicReference<>();

		String savePath = System.getProperty("user.home") + File.separator + getFileName();
		File initFile = new File(savePath);
		File selectedFile = new File(initFile.getParentFile(), "random.pdf");

		viewFactory = new ViewContextMockFactory() {
			@Override
			public FileChooserView createFileChooserView() {
				FileChooserMockView view = new FileChooserMockView() {
					@Override
					public File showSaveFile(View parent) {
						super.showSaveFile(parent);
						return selectedFile;
					}
				};
				chooserRef.set(view);
				return view;
			}
		};

		SaveDocumentsMockView view = new SaveDocumentsMockView() {
			@Override
			public void setSavePath(StringProperty path) {
				pathRef.set(path.get());
			}
		};

		SaveDocumentsPresenter presenter = new SaveDocumentsPresenter(context, view, viewFactory, context.getDocumentService());
		presenter.initialize();

		view.selectPathAction.execute();

		assertEquals("PDF Files", chooserRef.get().description);
		assertArrayEquals(new String[] { "*.pdf" }, chooserRef.get().extensions);
		assertEquals(initFile.getName(), chooserRef.get().initialFileName);
		assertEquals(initFile.getParentFile(), chooserRef.get().directory);
		assertEquals(selectedFile.getPath(), pathRef.get());
	}

	@Test
	void testEmptyMerge() throws InterruptedException {
		AtomicReference<String> errorRef = new AtomicReference<>();
		CountDownLatch errorLatch = new CountDownLatch(1);

		SaveDocumentsMockView view = new SaveDocumentsMockView();

		SaveDocumentsPresenter presenter = new SaveDocumentsPresenter(context, view, viewFactory, context.getDocumentService());
		presenter.initialize();

		view.mergeAction.execute();

		errorLatch.await();

		assertEquals("document.save.error", errorRef.get());
	}

	@Test
	void testMerge() throws InterruptedException, IOException {
		AtomicBoolean shownProgress = new AtomicBoolean(false);
		AtomicBoolean success = new AtomicBoolean(false);
		AtomicBoolean gotProgress = new AtomicBoolean(false);
		CountDownLatch saveLatch = new CountDownLatch(1);

		Path selectedPath = testPath.resolve("test.pdf");
		File selectedFile = selectedPath.toFile();

		viewFactory = new ViewContextMockFactory() {
			@Override
			@SuppressWarnings("unchecked")
			public <T> T getInstance(Class<T> cls) {
				if (cls == SaveDocumentOptionView.class) {
					return (T) new SaveDocumentOptionMockView();
				}
				return super.getInstance(cls);
			}

			@Override
			public FileChooserView createFileChooserView() {
				return new FileChooserMockView() {

					@Override
					public File showSaveFile(View parent) {
						super.showSaveFile(parent);
						return selectedFile;
					}
				};
			}
		};

		SaveDocumentsMockView view = new SaveDocumentsMockView() {
			@Override
			public void addDocumentOptionView(SaveDocumentOptionView optionView) {
				optionView.select();
			}
		};

		SaveDocumentsPresenter presenter = new SaveDocumentsPresenter(context, view, viewFactory, context.getDocumentService());
		presenter.initialize();

		view.selectPathAction.execute();
		view.mergeAction.execute();

		saveLatch.await();

		assertTrue(shownProgress.get());
		assertTrue(gotProgress.get());
		assertTrue(success.get());

		Document savedDoc = new Document(selectedFile);

		assertEquals(4, savedDoc.getPageCount());

		savedDoc.close();
	}

	@Test
	void testSaveIndividual() throws InterruptedException, IOException {
		CountDownLatch saveLatch = new CountDownLatch(1);

		List<SaveDocumentOptionMockView> optionViews = new ArrayList<>();

		Path selectedPath = testPath.resolve("test.pdf");
		File selectedFile = selectedPath.toFile();

		viewFactory = new ViewContextMockFactory() {
			@Override
			@SuppressWarnings("unchecked")
			public <T> T getInstance(Class<T> cls) {
				if (cls == SaveDocumentOptionView.class) {
					return (T) new SaveDocumentOptionMockView();
				}
				return super.getInstance(cls);
			}

			@Override
			public FileChooserView createFileChooserView() {
				return new FileChooserMockView() {

					@Override
					public File showSaveFile(View parent) {
						super.showSaveFile(parent);
						return selectedFile;
					}
				};
			}
		};

		SaveDocumentsMockView view = new SaveDocumentsMockView() {
			@Override
			public void addDocumentOptionView(SaveDocumentOptionView optionView) {
				optionViews.add((SaveDocumentOptionMockView) optionView);
			}
		};

		SaveDocumentsPresenter presenter = new SaveDocumentsPresenter(context, view, viewFactory, context.getDocumentService());
		presenter.initialize();

		assertEquals(2, optionViews.size());

		optionViews.get(1).saveAction.execute();

		saveLatch.await();

		Document savedDoc = new Document(selectedFile);

		assertEquals(3, savedDoc.getPageCount());

		savedDoc.close();
	}

	private String getFileName() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		String docName = context.getDictionary().get("document.save.lecture");
		String date = dateFormat.format(new Date());

		return docName + "-" + date + ".pdf";
	}



	private static class SaveDocumentsMockView implements SaveDocumentsView {

		Action mergeAction;

		Action selectPathAction;


		@Override
		public void addDocumentOptionView(SaveDocumentOptionView optionView) {

		}

		@Override
		public void setSavePath(StringProperty path) {

		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnMerge(Action action) {
			mergeAction = action;
		}

		@Override
		public void setOnSelectPath(Action action) {
			selectPathAction = action;
		}
	}



	private static class SaveDocumentOptionMockView implements SaveDocumentOptionView {

		private Action selectAction;

		private Action deselectAction;

		private Action saveAction;


		@Override
		public void select() {
			selectAction.execute();
		}

		@Override
		public void deselect() {
			deselectAction.execute();
		}

		@Override
		public String getDocumentTitle() {
			return null;
		}

		@Override
		public void setDocumentTitle(String docTitle) {

		}

		@Override
		public void setOnSaveDocument(Action action) {
			saveAction = action;
		}

		@Override
		public void setOnSelectDocument(Action action) {
			selectAction = action;
		}

		@Override
		public void setOnDeselectDocument(Action action) {
			deselectAction = action;
		}
	}
}