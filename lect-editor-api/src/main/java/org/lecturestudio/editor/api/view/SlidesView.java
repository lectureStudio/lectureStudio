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

import java.util.Collection;

import org.bytedeco.javacv.Frame;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.input.ScrollHandler;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.stylus.StylusHandler;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.View;

/**
 * Interface representing a view for slides in the editor application.
 * This view is responsible for displaying and managing documents, pages,
 * and their contents in a slide-based presentation interface.
 *
 * @author Alex Andres
 */
public interface SlidesView extends View {

	/**
	 * Adds a document to the slides view.
	 *
	 * @param doc     The document to add.
	 * @param context The application context.
	 */
	void addDocument(Document doc, ApplicationContext context);

	/**
	 * Removes a document from the slides view.
	 *
	 * @param doc The document to remove.
	 */
	void removeDocument(Document doc);

	/**
	 * Selects a document in the slides view.
	 *
	 * @param doc The document to select.
	 */
	void selectDocument(Document doc);

	/**
	 * Paints a video frame in the slides view.
	 *
	 * @param frame       The frame to paint.
	 * @param contentSize The size of the content area excluding the borders.
	 */
	void paintFrame(Frame frame, Dimension2D contentSize);

	/**
	 * Repaints the slides view.
	 */
	void repaint();

	/**
	 * Gets the current page being displayed.
	 *
	 * @return The current page.
	 */
	Page getPage();

	/**
	 * Sets the current page to display with presentation parameters.
	 *
	 * @param page      The page to set.
	 * @param parameter The presentation parameters.
	 */
	void setPage(Page page, PresentationParameter parameter);

	/**
	 * Sets the page renderer for the slides view.
	 *
	 * @param pageRenderer The page renderer to set.
	 */
	void setPageRenderer(RenderController pageRenderer);

	/**
	 * Sets the scroll handler for the slides view.
	 *
	 * @param scrollHandler The scroll handler to set.
	 */
	void setScrollHandler(ScrollHandler scrollHandler);

	/**
	 * Sets the key event handler for the slides view.
	 *
	 * @param action The action to perform when a key event occurs.
	 */
	void setOnKeyEvent(ConsumerAction<KeyEvent> action);

	/**
	 * Sets the action to perform when a document is selected.
	 *
	 * @param action The action to perform when a document is selected.
	 */
	void setOnSelectDocument(ConsumerAction<Document> action);

	/**
	 * Sets the action to perform when a page is deleted.
	 *
	 * @param action The action to perform when a page is deleted.
	 */
	void setOnDeletePage(ConsumerAction<Page> action);

	/**
	 * Sets the action to perform when a page is selected.
	 *
	 * @param action The action to perform when a page is selected.
	 */
	void setOnSelectPage(ConsumerAction<Page> action);

	/**
	 * Sets the action to perform when the view is transformed.
	 *
	 * @param action The action to perform when the view is transformed.
	 */
	void setOnViewTransform(ConsumerAction<Matrix> action);

	/**
	 * Removes all page object views from the slides view.
	 */
	void removeAllPageObjectViews();

	/**
	 * Gets all page object views in the slides view.
	 *
	 * @return A collection of page object views.
	 */
	Collection<PageObjectView<?>> getPageObjectViews();

	/**
	 * Adds a page object view to the slides view.
	 *
	 * @param objectView The page object view to add.
	 */
	void addPageObjectView(PageObjectView<Shape> objectView);

	/**
	 * Removes a page object view from the slides view.
	 *
	 * @param objectView The page object view to remove.
	 */
	void removePageObjectView(PageObjectView<? extends Shape> objectView);

	/**
	 * Sets the stylus handler for the slides view.
	 *
	 * @param handler The stylus handler to set.
	 */
	void setStylusHandler(StylusHandler handler);

	/**
	 * Binds the seek property to the slides view.
	 *
	 * @param seekProperty The seek property to bind.
	 */
	void bindSeekProperty(BooleanProperty seekProperty);
}
