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

package org.lecturestudio.editor.api.view;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.View;

public interface SlidesView extends View {

	void addDocument(Document doc, ApplicationContext context);

	void removeDocument(Document doc);

	void selectDocument(Document doc);

	void repaint();

	Page getPage();

	void setPage(Page page, PresentationParameter parameter);

	void setPageRenderer(RenderController pageRenderer);

	void setOnKeyEvent(ConsumerAction<KeyEvent> action);

	void setOnSelectDocument(ConsumerAction<Document> action);

	void setOnDeletePage(ConsumerAction<Page> action);

	void setOnSelectPage(ConsumerAction<Page> action);

	void setOnViewTransform(ConsumerAction<Matrix> action);

}
