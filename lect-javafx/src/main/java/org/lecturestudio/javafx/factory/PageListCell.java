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

package org.lecturestudio.javafx.factory;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.function.Consumer;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.javafx.control.SlideView;

public class PageListCell extends ListCell<Page> {

	private final PresentationParameterProvider ppProvider;

	private final SlideView slideView;


	public PageListCell(RenderController pageRenderer,
						PresentationParameterProvider ppProvider,
						Consumer<Page> selectionCallback,
						ContextMenu contextMenu) {
		this.ppProvider = ppProvider;

		slideView = new SlideView();
		slideView.setPageRenderer(pageRenderer);
		slideView.setViewType(ViewType.Preview);
		slideView.setMouseTransparent(true);

		if (nonNull(contextMenu)) {
			addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
				if (event.isSecondaryButtonDown()) {
					contextMenu.show(slideView, event.getScreenX(), event.getScreenY());
					event.consume();
				}
			});
		}

		addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			if (!getItem().equals(getListView().getSelectionModel().getSelectedItem())) {
				selectionCallback.accept(getItem());
				event.consume();
			}
		});
		layoutBoundsProperty().addListener(observable -> {
			if (slideView.getPage() != null) {
				Bounds bounds = getLayoutBounds();
				Insets insets = getInsets();

				double paddingX = insets.getLeft() + insets.getRight();
				double paddingY = insets.getTop() + insets.getBottom();

				PageMetrics metrics = slideView.getPage().getPageMetrics();
				Dimension2D slideBounds = metrics.convert(bounds.getWidth() - paddingX, bounds.getHeight() - paddingY);

				final double prefW = (slideBounds.getWidth());
				final double prefH = (slideBounds.getHeight());

				slideView.setPrefSize(prefW, prefH);
				slideView.setMaxSize(prefW, prefH);
			}
		});

		setText(null);
	}

	@Override
	protected void updateItem(Page page, boolean empty) {
		super.updateItem(page, empty);

		if (isNull(page) || empty) {
			setGraphic(null);
		}
		else {
			PresentationParameter parameter = ppProvider.getParameter(page);

			slideView.parameterChanged(page, parameter);
			slideView.setPage(page);

			setGraphic(slideView);
		}
	}
}
