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

package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationParameter;

public interface DocumentTemplateSettingsView extends SettingsBaseView {

	void setChatMessagePage(Page page, PresentationParameter parameter);

	void setHallMessagePage(Page page, PresentationParameter parameter);

	void setQuizPage(Page page, PresentationParameter parameter);

	void setWhiteboardPage(Page page, PresentationParameter parameter);

	void bindChatMessageBounds(ObjectProperty<Rectangle2D> bounds);

	void bindHallMessageBounds(ObjectProperty<Rectangle2D> bounds);

	void bindQuizBounds(ObjectProperty<Rectangle2D> bounds);

	void setOnSelectChatMessageTemplatePath(Action action);

	void setOnResetChatMessageTemplatePath(Action action);

	void setOnSelectHallMessageTemplatePath(Action action);

	void setOnResetHallMessageTemplatePath(Action action);

	void setOnSelectQuizTemplatePath(Action action);

	void setOnResetQuizTemplatePath(Action action);

	void setOnSelectWhiteboardTemplatePath(Action action);

	void setOnResetWhiteboardTemplatePath(Action action);

}
