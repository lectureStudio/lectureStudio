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
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.editor.api.view.InsertPageView;
import org.lecturestudio.javafx.control.SlideView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "page-insert")
public class FxInsertPageView extends StackPane implements InsertPageView {

	private final ApplicationContext context;

	private final RenderController pageRenderer;

	private ConsumerAction<Integer> pageNumberNewDocAction;

	@FXML
	private ResourceBundle resources;

	@FXML
	private SlideView currentPageView;

	@FXML
	private SlideView newPageView;

	@FXML
	private Button prevPageNewDocButton;

	@FXML
	private Button nextPageNewDocButton;

	@FXML
	private Label totalPagesNewDocLabel;

	@FXML
	private TextField pageNumberNewDocField;

	@FXML
	private TextField pageNumberCurrentDocField;

	@FXML
	private Button abortButton;

	@FXML
	private Button insertButton;


	@Inject
	FxInsertPageView(ApplicationContext context, RenderController pageRenderer) {
		super();

		this.context = context;
		this.pageRenderer = pageRenderer;
	}

	@Override
	public void setPageCurrentDoc(Page page) {
		FxUtils.invoke(() -> {
			currentPageView.parameterChanged(page, getPresentationParameter(page));
			currentPageView.setPage(page);

			pageNumberCurrentDocField.setText(String.valueOf(page.getPageNumber() + 1));
		});
	}

	@Override
	public void setPageNewDoc(Page page) {
		Document document = page.getDocument();
		final int pageCount = document.getPageCount();
		final int pageNumber = page.getPageNumber();

		FxUtils.invoke(() -> {
			newPageView.parameterChanged(page, getPresentationParameter(page));
			newPageView.setPage(page);

			pageNumberNewDocField.setText(String.valueOf(page.getPageNumber() + 1));

			prevPageNewDocButton.setDisable(pageNumber < 1);
			nextPageNewDocButton.setDisable(pageNumber >= pageCount - 1);
		});
	}

	@Override
	public void setTotalPagesNewDocLabel(int pages) {
		FxUtils.invoke(() -> totalPagesNewDocLabel.setText(" / " + pages));
	}

	@Override
	public void setOnPreviousPageNewDoc(Action action) {
		FxUtils.bindAction(prevPageNewDocButton, action);
	}

	@Override
	public void setOnNextPageNewDoc(Action action) {
		FxUtils.bindAction(nextPageNewDocButton, action);
	}

	@Override
	public void setOnPageNumberNewDoc(ConsumerAction<Integer> action) {
		pageNumberNewDocAction = action;
	}

	@Override
	public void setOnAbort(Action action) {
		FxUtils.bindAction(abortButton, action);
	}

	@Override
	public void setOnInsert(Action action) {
		FxUtils.bindAction(insertButton, action);
	}

	@FXML
	private void initialize() {
		currentPageView.setPageRenderer(pageRenderer);
		newPageView.setPageRenderer(pageRenderer);

		pageNumberNewDocField.setOnAction(event -> {
			if (nonNull(pageNumberNewDocAction)) {
				try {
					int pageNumber = Integer.parseInt(pageNumberNewDocField.getText());
					pageNumberNewDocAction.execute(pageNumber - 1);
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
