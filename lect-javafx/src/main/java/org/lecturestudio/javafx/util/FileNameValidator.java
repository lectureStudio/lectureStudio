/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.javafx.util;

import java.nio.file.Paths;

import javafx.scene.control.TextInputControl;

public class FileNameValidator extends TextInputValidator {

	public void bind(TextInputControl control) {
		bind(control, text -> {
			if (text.isEmpty() || text.isBlank()) {
				setError("File name is empty");
				return false;
			}

			try {
				Paths.get(text);
				setError("");
				return true;
			}
			catch (Exception e) {
				setError(e.getMessage());
				return false;
			}
		});
	}

}
