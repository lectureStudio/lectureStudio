/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.editor.javafx.view;

import static java.util.Objects.nonNull;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.editor.api.view.ReplacePageView;
import org.lecturestudio.javafx.control.SlideView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "page-replace")
public class FxReplacePageView extends StackPane implements ReplacePageView {

	private final ApplicationContext context;

	private final RenderController pageRenderer;

	private ConsumerAction<Integer> pageNumberAction;

	@FXML
	private SlideView currentPageView;

	@FXML
	private SlideView newPageView;

	@FXML
	private Pane controlsContainer;

	@FXML
	private Button prevPageButton;

	@FXML
	private Button nextPageButton;

	@FXML
	private TextField pageNumberField;

	@FXML
	private Button cancelButton;

	@FXML
	private Button replaceButton;


	@Inject
	FxReplacePageView(ApplicationContext context, RenderController pageRenderer) {
		super();

		this.context = context;
		this.pageRenderer = pageRenderer;
	}

	@Override
	public void setCurrentPage(Page page) {
		FxUtils.invoke(() -> {
			currentPageView.parameterChanged(page, getPresentationParameter(page));
			currentPageView.setPage(page);
		});
	}

	@Override
	public void setNewPage(Page page) {
		Document document = page.getDocument();
		int pageCount = document.getPageCount();
		int pageNumber = page.getPageNumber();

		FxUtils.invoke(() -> {
			newPageView.parameterChanged(page, getPresentationParameter(page));
			newPageView.setPage(page);

			pageNumberField.setText(String.valueOf(page.getPageNumber() + 1));

			prevPageButton.setDisable(pageNumber < 1);
			nextPageButton.setDisable(pageNumber >= pageCount - 1);
		});
	}

	@Override
	public void setOnPreviousPage(Action action) {
		FxUtils.bindAction(prevPageButton, action);
	}

	@Override
	public void setOnNextPage(Action action) {
		FxUtils.bindAction(nextPageButton, action);
	}

	@Override
	public void setOnPageNumber(ConsumerAction<Integer> action) {
		pageNumberAction = action;
	}

	@Override
	public void setOnCancel(Action action) {
		FxUtils.bindAction(cancelButton, action);
	}

	@Override
	public void setOnReplace(Action action) {
		FxUtils.bindAction(replaceButton, action);
	}

	@FXML
	private void initialize() {
		currentPageView.setPageRenderer(pageRenderer);
		newPageView.setPageRenderer(pageRenderer);

		pageNumberField.setOnAction(event -> {
			if (nonNull(pageNumberAction)) {
				try {
					int pageNumber = Integer.parseInt(pageNumberField.getText());
					pageNumberAction.execute(pageNumber - 1);
				}
				catch (Exception e) {
					// Ignore parsing errors.
				}
			}
		});
	}

	private PresentationParameter getPresentationParameter(Page page) {
		return new PresentationParameter(context.getConfiguration(), page);
	}
}
