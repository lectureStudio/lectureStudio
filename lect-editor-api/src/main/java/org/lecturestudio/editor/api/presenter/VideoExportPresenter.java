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

package org.lecturestudio.editor.api.presenter;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.service.RecentDocumentService;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.config.EditorConfiguration;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.view.VideoExportView;
import org.lecturestudio.media.config.RenderConfiguration;

public class VideoExportPresenter extends Presenter<VideoExportView> {

	private final ViewContextFactory viewFactory;

	private final RecentDocumentService recentDocumentService;

	private final RenderConfiguration videoRenderConfig;

	private StringProperty targetName;

	private StringProperty targetDirectory;


	@Inject
	VideoExportPresenter(ApplicationContext context, VideoExportView view,
			ViewContextFactory viewFactory,
			RecentDocumentService recentDocumentService) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.recentDocumentService = recentDocumentService;
		this.videoRenderConfig = ((EditorContext) context).getRenderConfiguration();
	}

	@Override
	public void initialize() {
		EditorConfiguration config = (EditorConfiguration) context.getConfiguration();
		RecentDocument recentRecording = recentDocumentService.getRecentDocuments().get(0);
		String recordingName = Path.of(recentRecording.getDocumentPath()).getFileName().toString();
		String fileName = FileUtils.stripExtension(recordingName);

		targetName = new StringProperty(fileName);
		targetDirectory = new StringProperty(config.getVideoExportPath());

		view.bindTargetName(targetName);
		view.bindTargetDirectory(targetDirectory);
		view.bindVideo(videoRenderConfig.videoExportProperty());
		view.bindVectorPlayer(videoRenderConfig.webVectorExportProperty());
		view.bindVideoPlayer(videoRenderConfig.webVideoExportProperty());
		view.setOnSelectTargetDirectory(this::selectTargetDir);
		view.setOnCancel(this::close);
		view.setOnCreate(this::create);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	private void selectTargetDir() {
		File initDirectory = new File(targetDirectory.get());

		DirectoryChooserView dirChooser = viewFactory.createDirectoryChooserView();
		dirChooser.setInitialDirectory(initDirectory);

		File selectedFile = dirChooser.show(view);

		if (nonNull(selectedFile)) {
			targetDirectory.set(selectedFile.getAbsolutePath());
		}
	}

	private void create() {
		String name = targetName.get();
		String extension = videoRenderConfig.getFileFormat();
		File outputFolder = new File(targetDirectory.get());

		if (videoRenderConfig.getWebVectorExport() || videoRenderConfig.getWebVideoExport()) {
			outputFolder = new File(outputFolder, name);
		}
		if (!outputFolder.exists()) {
			if (!outputFolder.mkdirs()) {
				logException(new IOException(), "Create output folder failed");
			}
		}

		videoRenderConfig.setOutputFile(new File(outputFolder, name + "." + extension));

		close();

		context.getEventBus().post(new ShowPresenterCommand<>(VideoExportProgressPresenter.class));
	}

}
