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

package org.lecturestudio.presenter.api.view;

import java.util.List;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.SlideNote;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.stylus.StylusHandler;
import org.lecturestudio.web.api.message.MessengerMessage;

public interface SlidesView extends View {

	void addPageObjectView(PageObjectView<?> objectView);

	void removePageObjectView(PageObjectView<?> objectView);

	void removeAllPageObjectViews();

	List<PageObjectView<?>> getPageObjectViews();

	void addDocument(Document doc, PresentationParameterProvider ppProvider);

	void removeDocument(Document doc);

	void selectDocument(Document doc, PresentationParameterProvider ppProvider);

	Page getPage();

	void setPage(Page page, PresentationParameter parameter);

	void setPageRenderer(RenderController pageRenderer);

	void setPageNotes(List<SlideNote> notes);

	void setOutline(DocumentOutline outline);

	void bindShowOutline(BooleanProperty showProperty);

	void setExtendedFullscreen(boolean extended);

	void setStylusHandler(StylusHandler handler);

	void setLaTeXText(String text);

	void setMessengerState(ExecutableState state);

	void setMessengerMessage(MessengerMessage message);

	void setSelectedToolType(ToolType type);

	void setOnKeyEvent(ConsumerAction<KeyEvent> action);

	void setOnLaTeXCompile(ConsumerAction<String> action);

	void setOnSelectDocument(ConsumerAction<Document> action);

	void setOnSelectPage(ConsumerAction<Page> action);

	void setOnViewTransform(ConsumerAction<Matrix> action);

	void setOnNewPage(Action action);

	void setOnDeletePage(Action action);

	void setOnOutlineItem(ConsumerAction<DocumentOutlineItem> action);

}
