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

package org.lecturestudio.presenter.api.config;

import org.lecturestudio.core.beans.StringProperty;

public class TemplateConfiguration {

	private final StringProperty quizTemplatePath = new StringProperty();

	private final StringProperty chatMessageTemplatePath = new StringProperty();

	private final StringProperty hallMessageTemplatePath = new StringProperty();

	private final StringProperty whiteboardTemplatePath = new StringProperty();


	public String getQuizTemplatePath() {
		return quizTemplatePath.get();
	}

	public void setQuizTemplatePath(String path) {
		quizTemplatePath.set(path);
	}

	public StringProperty quizTemplatePathProperty() {
		return quizTemplatePath;
	}

	public String getChatMessageTemplatePath() {
		return chatMessageTemplatePath.get();
	}

	public void setChatMessageTemplatePath(String path) {
		chatMessageTemplatePath.set(path);
	}

	public StringProperty chatMessageTemplatePathProperty() {
		return chatMessageTemplatePath;
	}

	public String getHallMessageTemplatePath() {
		return hallMessageTemplatePath.get();
	}

	public void setHallMessageTemplatePath(String path) {
		hallMessageTemplatePath.set(path);
	}

	public StringProperty hallMessageTemplatePathProperty() {
		return hallMessageTemplatePath;
	}

	public String getWhiteboardTemplatePath() {
		return whiteboardTemplatePath.get();
	}

	public void setWhiteboardTemplatePath(String path) {
		whiteboardTemplatePath.set(path);
	}

	public StringProperty whiteboardTemplatePathProperty() {
		return whiteboardTemplatePath;
	}
}
