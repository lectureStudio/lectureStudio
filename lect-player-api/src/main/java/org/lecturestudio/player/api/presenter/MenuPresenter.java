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

package org.lecturestudio.player.api.presenter;

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.command.CloseApplicationCommand;
import org.lecturestudio.core.presenter.command.FullscreenCommand;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.service.RecentDocumentService;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.media.recording.RecordingFileService;
import org.lecturestudio.player.api.view.MenuView;

public class MenuPresenter extends Presenter<MenuView> {

	private final EventBus eventBus;

	private final ViewContextFactory viewFactory;

	private final RecentDocumentService recentDocumentService;

	private final RecordingFileService recordingService;


	@Inject
	MenuPresenter(ApplicationContext context, MenuView view,
				  ViewContextFactory viewFactory,
				  RecentDocumentService recentDocumentService,
				  RecordingFileService recordingService) {
		super(context, view);

		this.eventBus = context.getEventBus();
		this.viewFactory = viewFactory;
		this.recentDocumentService = recentDocumentService;
		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		eventBus.register(this);

		view.setDocument(null);
		view.setRecentDocuments(recentDocumentService.getRecentDocuments());

		view.setOnOpenRecording(this::selectNewRecording);
		view.setOnOpenRecording(this::openRecording);
		view.setOnCloseRecording(this::closeSelectedRecording);
		view.setOnExit(this::exit);
		view.setOnSettings(this::showSettingsView);
		view.setOnFullscreen(this::setFullscreen);
		view.setOnOpenLog(this::showLog);
		view.setOnOpenAbout(this::showAboutView);
	}

	@Override
	public void destroy() {
		eventBus.unregister(this);
	}

	@Subscribe
	public void onEvent(final DocumentEvent event) {
		if (event.created() || event.closed()) {
			view.setRecentDocuments(recentDocumentService.getRecentDocuments());
		}

		Document doc = event.closed() ? null : event.getDocument();

		view.setDocument(doc);
	}

	public void openRecording(File file) {
		recordingService.openRecording(file)
			.thenRun(() -> {
				RecentDocument recentDoc = new RecentDocument();
				recentDoc.setDocumentName(FileUtils.stripExtension(file.getName()));
				recentDoc.setDocumentPath(file.getAbsolutePath());
				recentDoc.setLastModified(new Date());

				recentDocumentService.add(recentDoc);
			})
			.exceptionally(throwable -> {
				handleException(throwable, "Open recording failed", "open.recording.error", file.getPath());
				return null;
			});
	}

	public void closeSelectedRecording() {
		recordingService.closeSelectedRecording();
	}

	public void exit() {
		eventBus.post(new CloseApplicationCommand());
	}

	public void showSettingsView() {
//		eventBus.post(new ShowPresenterCommand<>(SettingsPresenter.class));
	}

	public void setFullscreen(boolean enable) {
		eventBus.post(new FullscreenCommand(enable));
	}

	public void showLog() {
		try {
			Desktop.getDesktop().open(new File(
					context.getDataLocator().getAppDataPath()));
		}
		catch (IOException e) {
			handleException(e, "Open log path failed", "generic.error");
		}
	}

	public void showAboutView() {
		eventBus.post(new ShowPresenterCommand<>(AboutPresenter.class));
	}

	private void selectNewRecording() {
		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.addExtensionFilter("Lecture Recordings", "presenter", "plr");

		File selectedFile = fileChooser.showOpenFile(view);

		if (nonNull(selectedFile)) {
			openRecording(selectedFile);
		}
	}
}
