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

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.View;

public class SwingFxDirectoryChooserView extends SwingFxChooserView implements DirectoryChooserView {

	private File selectedDirectory;

	private String chooserTitle;


	public SwingFxDirectoryChooserView() {
		super();
	}

	@Override
	public void setInitialDirectory(File directory) {
		selectedDirectory = directory;
	}

	@Override
	public void setTitle(String title) {
		chooserTitle = title;
	}

	@Override
	public File show(View parent) {
		return openDialogBlocked(parent, Type.OPEN);
	}

	@Override
	protected File chooseFile(Stage stage, Type type) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle(chooserTitle);
		dirChooser.setInitialDirectory(selectedDirectory);

		return dirChooser.showDialog(stage);
	}
}
