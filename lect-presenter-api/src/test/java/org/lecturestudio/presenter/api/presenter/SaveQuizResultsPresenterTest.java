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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.net.LocalBroadcaster;
import org.lecturestudio.presenter.api.service.WebService;
import org.lecturestudio.presenter.api.service.WebServiceInfo;
import org.lecturestudio.presenter.api.view.SaveQuizResultsView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaveQuizResultsPresenterTest extends PresenterTest {

	private String defaultSavePath;


	@BeforeEach
	void setup() {
		defaultSavePath = System.getProperty("user.home") + File.separator + getFileName();
	}

	@Test
	void testInit() {
		AtomicReference<String> pathRef = new AtomicReference<>();

		SaveQuizResultsMockView view = new SaveQuizResultsMockView();

		SaveQuizResultsPresenter presenter = new SaveQuizResultsPresenter(context, view, viewFactory, null);
		presenter.initialize();

		assertEquals(defaultSavePath, pathRef.get());
	}

	@Test
	void testSelectPath() {
		AtomicReference<FileChooserMockView> chooserRef = new AtomicReference<>();
		AtomicReference<String> pathRef = new AtomicReference<>();

		File initFile = new File(defaultSavePath);
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

		SaveQuizResultsMockView view = new SaveQuizResultsMockView();

		SaveQuizResultsPresenter presenter = new SaveQuizResultsPresenter(context, view, viewFactory, null);
		presenter.initialize();

		view.selectPathAction.execute();

		assertEquals("PDF Files", chooserRef.get().description);
		assertArrayEquals(new String[] { "*.pdf" }, chooserRef.get().extensions);
		assertEquals(initFile.getName(), chooserRef.get().initialFileName);
		assertEquals(initFile.getParentFile(), chooserRef.get().directory);
		assertEquals(FileUtils.stripExtension(selectedFile).getPath(), pathRef.get());
	}

	@Test
	void testSaveError() throws InterruptedException {
		AtomicReference<String> errorRef = new AtomicReference<>();
		CountDownLatch errorLatch = new CountDownLatch(1);

		SaveQuizResultsMockView view = new SaveQuizResultsMockView();

		SaveQuizResultsPresenter presenter = new SaveQuizResultsPresenter(context, view, viewFactory, null);
		presenter.initialize();

		view.saveAction.execute();

		errorLatch.await();

		assertEquals("quiz.save.error", errorRef.get());
	}

	@Test
	void testNoQuizResult() throws InterruptedException, IOException {
		AtomicReference<String> errorRef = new AtomicReference<>();
		CountDownLatch errorLatch = new CountDownLatch(1);

		SaveQuizResultsMockView view = new SaveQuizResultsMockView();

		LocalBroadcaster localBroadcaster = new LocalBroadcaster(context);

		Properties streamProps = new Properties();
		streamProps.load(getClass().getClassLoader()
				.getResourceAsStream("resources/stream.properties"));

		WebServiceInfo webServiceInfo = new WebServiceInfo(streamProps);

		WebService webService = new WebService(context, context.getDocumentService(), localBroadcaster, webServiceInfo);

		SaveQuizResultsPresenter presenter = new SaveQuizResultsPresenter(context, view, viewFactory, webService);
		presenter.initialize();

		view.saveAction.execute();

		errorLatch.await();

		assertEquals("quiz.save.error", errorRef.get());
	}

	private String getFileName() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		String docName = context.getDictionary().get("quiz");
		String date = dateFormat.format(new Date());

		return docName + "-" + date;
	}



	private static class SaveQuizResultsMockView implements SaveQuizResultsView {

		private Action selectPathAction;

		private Action saveAction;


		@Override
		public void setSavePath(StringProperty path) {

		}

		@Override
		public void selectCsvOption(boolean select) {

		}

		@Override
		public void selectPdfOption(boolean select) {

		}

		@Override
		public void setOnCsvSelection(ConsumerAction<Boolean> action) {

		}

		@Override
		public void setOnPdfSelection(ConsumerAction<Boolean> action) {

		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnSave(Action action) {
			saveAction = action;
		}

		@Override
		public void setOnSelectPath(Action action) {
			selectPathAction = action;
		}
	}
}