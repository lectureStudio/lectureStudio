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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.IOUtils;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.config.DefaultConfiguration;
import org.lecturestudio.editor.api.config.EditorConfiguration;
import org.lecturestudio.editor.api.view.VideoSettingsView;

public class VideoSettingsPresenter extends Presenter<VideoSettingsView> {

	private final ViewContextFactory viewFactory;


	@Inject
	VideoSettingsPresenter(ApplicationContext context, VideoSettingsView view,
			ViewContextFactory viewFactory) {
		super(context, view);

		this.viewFactory = viewFactory;
	}

	@Override
	public void initialize() throws IOException {
		EditorConfiguration config = (EditorConfiguration) context.getConfiguration();
		String exportPath = config.getVideoExportPath();

		if (isNull(exportPath) || exportPath.isEmpty()) {
			exportPath = System.getProperty("user.home");
			config.setVideoExportPath(exportPath);
		}

		view.bindTargetDirectory(config.videoExportPathProperty());
		view.setFreeDiskSpace(IOUtils.formatSize(getUsableSpace(config.getVideoExportPath())));
		view.setOnSelectTargetDirectory(this::selectTargetDir);
		view.setOnReset(this::reset);

		config.videoExportPathProperty().addListener((o, oldValue, newValue) -> {
			try {
				view.setFreeDiskSpace(IOUtils.formatSize(getUsableSpace(newValue)));
			}
			catch (IOException e) {
				logException(e, "Get usable space failed");
			}
		});
	}

	private void selectTargetDir() {
		EditorConfiguration config = (EditorConfiguration) context.getConfiguration();
		File initDirectory = new File(config.getVideoExportPath());

		DirectoryChooserView dirChooser = viewFactory.createDirectoryChooserView();
		dirChooser.setInitialDirectory(initDirectory);

		File selectedFile = dirChooser.show(view);

		if (nonNull(selectedFile)) {
			config.setVideoExportPath(selectedFile.getAbsolutePath());
		}
	}

	private void reset() {
		EditorConfiguration config = (EditorConfiguration) context.getConfiguration();
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		config.setVideoExportPath(defaultConfig.getVideoExportPath());
	}

	private long getUsableSpace(String targetPath) throws IOException {
		Path path = Paths.get(targetPath);
		FileStore store = Files.getFileStore(path.toRealPath());

		return store.getUsableSpace();
	}
}
