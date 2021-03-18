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

package org.lecturestudio.presenter.javafx.control;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.PageSelectListener;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.javafx.control.ThumbnailPanel;
import org.lecturestudio.javafx.util.FxUtils;

public class EditableThumbnailPanel extends VBox {

	@FXML
	private ThumbnailPanel thumbnailPanel;

	@FXML
	private Pane bottomButtonPanel;

	@FXML
	private Button addPageButton;

	@FXML
	private Button deletePageButton;


	public EditableThumbnailPanel(ResourceBundle resources) {
		super();

		load(resources);
	}

	public void setPageRenderer(RenderController pageRenderer) {
		thumbnailPanel.setPageRenderer(pageRenderer);
	}

	public void selectPage(Page page) {
		thumbnailPanel.selectPage(page);
	}

	public void addSelectListener(PageSelectListener listener) {
		thumbnailPanel.addSelectListener(listener);
	}

	public void removeSelectListener(PageSelectListener listener) {
		thumbnailPanel.removeSelectListener(listener);
	}

	public Document getDocument() {
		return thumbnailPanel.getDocument();
	}

	public void setDocument(Document doc, PresentationParameterProvider ppProvider) {
		bottomButtonPanel.setManaged(doc.isWhiteboard());
		bottomButtonPanel.setVisible(doc.isWhiteboard());

		thumbnailPanel.setDocument(doc, ppProvider);
	}

	public void setOnNewPage(Action action) {
		FxUtils.bindAction(addPageButton, action);
	}

	public void setOnDeletePage(Action action) {
		FxUtils.bindAction(deletePageButton, action);
	}

	private void load(ResourceBundle resources) {
		FxUtils.load("/resources/fxml/editable-thumbnail-panel.fxml", resources, this, this);
	}
}
