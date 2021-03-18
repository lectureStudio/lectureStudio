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

package org.lecturestudio.presenter.javafx.factory;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javafx.beans.NamedArg;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.javafx.control.SlideView;
import org.lecturestudio.javafx.internal.util.Utils;

public class SlideViewCellFactory implements Callback<TableColumn<Object, Page>, TableCell<Object, Page>> {

	/** The width of the SlideView within the preview popup. */
	private static final double PREVIEW_WIDTH = 400;

	/** Preferred height. */
	private final Double prefHeight;


	public SlideViewCellFactory(@NamedArg("prefHeight") Double prefHeight) {
		this.prefHeight = prefHeight;
	}

	@Override
	public TableCell<Object, Page> call(TableColumn<Object, Page> param) {
		return new SlideViewCell();
	}



	private class SlideViewCell extends TableCell<Object, Page> {

		private final SlideView slideView;

		private SlideView popupSlideView;

		private Tooltip popup;

		private final EventHandler<MouseEvent> mouseEnterHandler = (event) -> {
			if (isNull(popup) || isNull(popupSlideView)) {
				return;
			}

			Point2D p = getPrefPopupPosition(popupSlideView);
			popup.show(this, p.getX(), p.getY());
		};

		private final EventHandler<MouseEvent> mouseExitHandler = (event) -> {
			if (isNull(popup)) {
				return;
			}

			popup.hide();
		};


		public SlideViewCell() {
			slideView = new SlideView();
			slideView.setFocusTraversable(false);
			slideView.setDisable(true);

			popupSlideView = new SlideView();
			popupSlideView.setFocusTraversable(false);
			popupSlideView.setDisable(true);

			popup = new Tooltip();
			popup.setGraphic(popupSlideView);

			if (isNull(prefHeight)) {
				slideView.prefWidthProperty().bind(prefWidthProperty());
				slideView.maxWidthProperty().bind(prefWidthProperty());
				slideView.prefHeightProperty().bind(prefHeightProperty());
				slideView.maxHeightProperty().bind(prefHeightProperty());
			}
		}

		@Override
		protected void updateItem(Page page, boolean empty) {
			super.updateItem(page, empty);

			if (!empty && nonNull(page)) {
				PageMetrics metrics = page.getPageMetrics();

				popupSlideView.setPrefWidth(PREVIEW_WIDTH);
				popupSlideView.setPrefHeight(metrics.getHeight(PREVIEW_WIDTH));

				if (nonNull(prefHeight)) {
					slideView.setPrefWidth(metrics.getWidth(prefHeight));
					slideView.setMaxWidth(metrics.getWidth(prefHeight));

					slideView.setPrefHeight(prefHeight);
					slideView.setMaxHeight(prefHeight);
				}

				slideView.setPage(page);
				popupSlideView.setPage(page);

				setOnMouseEntered(mouseEnterHandler);
				setOnMouseExited(mouseExitHandler);

				setGraphic(slideView);
			}
			else {
				slideView.setPage(null);
				popupSlideView.setPage(null);

				setOnMouseEntered(null);
				setOnMouseExited(null);

				setGraphic(null);
			}
		}

		private Point2D getPrefPopupPosition(Node content) {
			return Utils.pointRelativeTo(this, content, HPos.CENTER, VPos.BOTTOM, 0, 0, true);
		}

	}

}
