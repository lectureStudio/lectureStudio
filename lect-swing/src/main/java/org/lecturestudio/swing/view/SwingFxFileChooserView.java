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

package org.lecturestudio.swing.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.View;

public class SwingFxFileChooserView extends SwingFxChooserView implements FileChooserView {

	private final List<FileChooser.ExtensionFilter> extensionFilters;

	private File selectedDirectory;

	private String selectedFileName;


	public SwingFxFileChooserView() {
		super();

		extensionFilters = new ArrayList<>();
	}

	@Override
	public void addExtensionFilter(String description, String... extensions) {
		List<String> list = Arrays.stream(extensions).map(s -> "*." + s)
				.collect(Collectors.toList());

		extensionFilters.add(new FileChooser.ExtensionFilter(description, list));
	}

	@Override
	public void setInitialDirectory(File directory) {
		selectedDirectory = directory;
	}

	@Override
	public void setInitialFileName(String name) {
		selectedFileName = name;
	}

	@Override
	public File showOpenFile(View parent) {
		return openDialogBlocked(parent, Type.OPEN);
	}

	@Override
	public File showSaveFile(View parent) {
		return openDialogBlocked(parent, Type.SAVE);
	}

	@Override
	protected File chooseFile(Stage stage, Type type) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(extensionFilters);
		fileChooser.setInitialDirectory(selectedDirectory);
		fileChooser.setInitialFileName(selectedFileName);

		return switch (type) {
			case OPEN -> fileChooser.showOpenDialog(stage);
			case SAVE -> fileChooser.showSaveDialog(stage);
		};
	}
}
