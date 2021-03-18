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

package org.lecturestudio.swing.ui.dialog;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.lecturestudio.core.app.dictionary.Dictionary;

public class CFileChooser extends JFileChooser {

	private static final long serialVersionUID = 1024938139885246151L;

	private final Dictionary dict;
	
	
	public CFileChooser(Dictionary dict) {
		this.dict = dict;
	}
	
	@Override
	public void approveSelection() {
		if (getDialogType() == SAVE_DIALOG) {
			File selectedFile = getSelectedFile();
			
			if (selectedFile != null) {
				if (selectedFile.exists()) {
					int response = JOptionPane.showConfirmDialog(
							this,
							selectedFile.getName() + " " + dict.get("overwrite.question"),
							dict.get("overwrite.file"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);

					if (response != JOptionPane.YES_OPTION)
						return;
				}

				File parent = selectedFile.getParentFile();
				if (!parent.canWrite()) {
					JOptionPane.showConfirmDialog(this,
							dict.get("file.permission.denied"),
							dict.get("error"), JOptionPane.CLOSED_OPTION,
							JOptionPane.ERROR_MESSAGE);

					return;
				}
			}
		}

		super.approveSelection();
	}

	public void setFileFilters(List<FileFilter> filters) {
		for (FileFilter filter : filters) {
			addChoosableFileFilter(filter);
		}
	}
	
}
