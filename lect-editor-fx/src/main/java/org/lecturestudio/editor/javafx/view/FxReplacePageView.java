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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.editor.api.view.ReplacePageView;
import org.lecturestudio.javafx.control.ExtRadioButton;
import org.lecturestudio.javafx.control.SlideView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;


@FxmlView(name = "page-replace")
public class FxReplacePageView extends StackPane implements ReplacePageView {

	private final ApplicationContext context;

	private final RenderController pageRenderer;

	private ConsumerAction<Integer> pageNumberNewDocAction;

	private ConsumerAction<Integer> pageNumberCurrentDocAction;

	@FXML
	private SlideView currentPageView;

	@FXML
	private SlideView newPageView;

	@FXML
	private Button prevPageNewDocButton;

	@FXML
	private Button nextPageNewDocButton;

	@FXML
	private Button prevPageCurrentDocButton;

	@FXML
	private Button nextPageCurrentDocButton;

	@FXML
	private Label totalPagesNewDocLabel;

	@FXML
	private Label totalPagesCurrentDocLabel;

	@FXML
	private TextField pageNumberNewDocField;

	@FXML
	private TextField pageNumberCurrentDocField;

	@FXML
	private ExtRadioButton allPagesTypeRadio;

	@FXML
	private ExtRadioButton currentPageTypeRadio;

	@FXML
	private ToggleGroup pageSelectionType;

	@FXML
	private Label allPagesTypeLabel;

	@FXML
	private Button abortButton;

	@FXML
	private Button replaceButton;

	@FXML
	private Button confirmButton;

	private boolean allPagesTypeRadioDisabled = false;


	@Inject
	FxReplacePageView(ApplicationContext context, RenderController pageRenderer) {
		super();

		this.context = context;
		this.pageRenderer = pageRenderer;
	}

	@Override
	public void setPageCurrentDoc(Page page) {
		setPageControls(page, currentPageView, pageNumberCurrentDocField, prevPageCurrentDocButton, nextPageCurrentDocButton);
	}

	@Override
	public void setPageNewDoc(Page page) {
		setPageControls(page, newPageView, pageNumberNewDocField, prevPageNewDocButton, nextPageNewDocButton);
	}

	@Override
	public void setTotalPagesNewDocLabel(int pages) {
		FxUtils.invoke(() -> totalPagesNewDocLabel.setText(" / " + pages));
	}

	@Override
	public void setTotalPagesCurrentDocLabel(int pages) {
		FxUtils.invoke(() -> totalPagesCurrentDocLabel.setText(" / " + pages));
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
	public void setOnPreviousPageCurrentDoc(Action action) {
		FxUtils.bindAction(prevPageCurrentDocButton, action);
	}

	@Override
	public void setOnNextPageCurrentDoc(Action action) {
		FxUtils.bindAction(nextPageCurrentDocButton, action);
	}

	@Override
	public void setOnPageNumberCurrentDoc(ConsumerAction<Integer> action) {
		pageNumberCurrentDocAction = action;
	}

	@Override
	public void setOnAbort(Action action) {
		FxUtils.bindAction(abortButton, action);
	}

	@Override
	public void setOnReplace(Action action) {
		FxUtils.bindAction(replaceButton, action);
	}

	@Override
	public void setOnConfirm(Action action) {
		FxUtils.bindAction(confirmButton, action);
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

		pageNumberCurrentDocField.setOnAction(event -> {
			if (nonNull(pageNumberCurrentDocAction)) {
				try {
					int pageNumber = Integer.parseInt(pageNumberCurrentDocField.getText());
					pageNumberCurrentDocAction.execute(pageNumber - 1);
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


	private void setPageControls(Page page, SlideView pageView, TextField pageNumberField, Button prevPageButton, Button nextPageButton) {
		Document document = page.getDocument();
		int pageCount = document.getPageCount();
		int pageNumber = page.getPageNumber();

		FxUtils.invoke(() -> {
			pageView.parameterChanged(page, getPresentationParameter(page));
			pageView.setPage(page);

			pageNumberField.setText(String.valueOf(page.getPageNumber() + 1));

			prevPageButton.setDisable(pageNumber < 1);
			nextPageButton.setDisable(pageNumber >= pageCount - 1);
		});
	}

	@Override
	public void enableInput() {
		FxUtils.invoke(() -> {
			setDisable(false);
			setDisableAllPagesTypeRadio(allPagesTypeRadioDisabled);
		});
	}

	@Override
	public void disableInput() {
		FxUtils.invoke(() -> setDisable(true));
	}

	@Override
	public void setDisableAllPagesTypeRadio(boolean disable) {
		allPagesTypeRadioDisabled = disable;
		FxUtils.invoke(() -> {
			allPagesTypeRadio.setDisable(disable);
			allPagesTypeLabel.setDisable(disable);
		});
	}

	@Override
	public void setOnReplaceTypeChange(ConsumerAction<String> action) {
		FxUtils.bindAction(pageSelectionType, action);
	}
}
