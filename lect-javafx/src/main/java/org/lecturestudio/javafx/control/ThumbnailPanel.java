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

package org.lecturestudio.javafx.control;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.model.listener.PageSelectListener;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.javafx.factory.PageCellFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ThumbnailPanel extends ListView<Page> {

	private final DocumentChangeListener docChangeListener = new DocumentChangeListener() {

		@Override
		public void documentChanged(Document document) {
			Platform.runLater(() -> {
				onDocumentChanged(document);
			});
		}

		@Override
		public void pageRemoved(final Page page) {
			Platform.runLater(() -> {
				onPageRemoved(page);
			});
		}

		@Override
		public void pageAdded(final Page page) {
			Platform.runLater(() -> {
				onPageAdded(page);
			});
		}
	};

	private final List<PageSelectListener> selectListeners = new ArrayList<>();

	private Document document;

	private RenderController pageRenderer;


	public ThumbnailPanel() {
		super();

		initialize();
	}

	public void setPageRenderer(RenderController pageRenderer) {
		this.pageRenderer = pageRenderer;
	}

	public void selectPage(Page page) {
		if (isNull(page)) {
			return;
		}

		Document doc = page.getDocument();

		if (doc.equals(document)) {
			setSelectedThumbnail(doc.getCurrentPage());
		}
	}

	public void addSelectListener(PageSelectListener listener) {
		selectListeners.add(listener);
	}

	public void removeSelectListener(PageSelectListener listener) {
		selectListeners.remove(listener);
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document doc, PresentationParameterProvider ppProvider) {
		setDocument(doc, ppProvider, null);
	}

	public void setDocument(Document doc, PresentationParameterProvider ppProvider, ContextMenu contextMenu) {
		if (nonNull(document)) {
			document.removeChangeListener(docChangeListener);
		}
		if (nonNull(doc)) {
			document = doc;

			if (nonNull(ppProvider)) {
				setCellFactory(new PageCellFactory(pageRenderer, ppProvider, this::onSelectPage, contextMenu));
			}

			getItems().setAll(doc.getPages());

			setSelectedThumbnail(doc.getCurrentPage());

			document.addChangeListener(docChangeListener);
		}
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/thumbnail-panel.css").toExternalForm();
	}

	private void resizeThumbnails() {
		if (isNull(document)) {
			return;
		}

		if (!document.isScreenCapture()) {
			Page page = getDocument().getPage(0);
			PageMetrics metrics = page.getPageMetrics();

			double width = getWidth();
			double height = metrics.getHeight(width);

			setFixedCellSize(height);

			scrollToSelected();
		}
	}

	private void onDocumentChanged(Document document) {
		getItems().setAll(document.getPages());
	}

	private void onPageAdded(Page page) {
		getItems().add(page);
	}

	private void onPageRemoved(Page page) {
		getItems().remove(page);
	}

	private void setSelectedThumbnail(Page page) {
		getSelectionModel().select(page);

		scrollToSelected();
	}

	private void onSelectPage(Page page) {
		if (isNull(page)) {
			return;
		}

		for (PageSelectListener listener : selectListeners) {
			listener.pageSelected(page);
		}
	}

	private void scrollToSelected() {
		int index = getSelectionModel().getSelectedIndex();

		if (index < getItems().size() - 1) {
			// Scroll to the next page.
			index++;
		}

		scrollTo(index);
	}

	private void initialize() {
		getStyleClass().add("thumbnail-panel");

		layoutBoundsProperty().addListener(observable -> {
			resizeThumbnails();
		});
	}
}
