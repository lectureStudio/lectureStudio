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

package org.lecturestudio.javafx.layout;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Set;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.ResizeFeatures;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.Pair;

public class DynamicResizePolicy<S> implements Callback<ResizeFeatures<S>, Boolean> {

	/**
	 * ColumnConstraints instances to explicitly control individual column sizing and layout
	 * behavior.
	 */
	private final ObservableList<ColumnSizeConstraints> columnConstraints = FXCollections.observableArrayList();

	private final ObjectProperty<TableView<S>> tableView = new SimpleObjectProperty<>();

	private ColumnWidthBinding tableWidthBinding;

	private ScrollBar scrollbar;

	private double staticWidth;


	public final ObjectProperty<TableView<S>> tableViewProperty() {
		return tableView;
	}

	public TableView<S> getTableView() {
		return tableView.get();
	}

	public void setTableView(TableView<S> tableView) {
		this.tableView.set(tableView);

		tableWidthBinding = new ColumnWidthBinding();

		tableView.skinProperty().addListener((value, oldSkin, newSkin) -> {
			scrollbar = getVerticalScrollbar();

			if (nonNull(scrollbar)) {
				scrollbar.visibleProperty().addListener((observable, oldValue, newValue) -> {
					// Notify bindings.
					Platform.runLater(() -> tableWidthBinding.invalidate());
				});
			}

			// Keep track of TableHeaderRow height changes.
			Pane headerRow = (Pane) tableView.lookup("TableHeaderRow");
			Region nestedHeader = (Region) headerRow.lookup("NestedTableColumnHeader");

			nestedHeader.heightProperty().addListener(observable -> {
				Set<Node> nodes = nestedHeader.lookupAll("TableColumnHeader > Label");

				updateColumnTexts(nodes);
			});
		});

		tableView.getColumns().addListener((ListChangeListener<TableColumn<S, ?>>) c -> {
			updateColumnWidths();
		});
	}

	/**
	 * Returns list of column constraints. Column constraints can be added to explicitly
	 * control individual column sizing behavior. If not set, column sizing behavior is
	 * computed based on content.
	 * <p>
	 * Index in the ObservableList denotes the column number, so the column constraint
	 * for the first column is at the position of 0.
	 * 
	 * @return the column constraints.
	 */
	public final ObservableList<ColumnSizeConstraints> getColumnConstraints() {
		return columnConstraints;
	}

	@Override
	public Boolean call(ResizeFeatures<S> param) {
		return TableView.UNCONSTRAINED_RESIZE_POLICY.call(param);
	}

	private void bindPercentageColumns(ObservableList<TableColumn<S, ?>> columns) {
		int columnCount = Math.min(columns.size(), columnConstraints.size());

		for (int i = 0; i < columnCount; i++) {
			ColumnSizeConstraints constraints = columnConstraints.get(i);

			if (constraints.getPercentWidth() > 0) {
				TableColumn<S, ?> column = columns.get(i);
				column.prefWidthProperty().unbind();
				column.prefWidthProperty().bind(tableWidthBinding.multiply(constraints.percentWidthProperty()));
			}
		}
	}

	private double getStaticColumnsWidth(ObservableList<TableColumn<S, ?>> columns) {
		int columnCount = Math.min(columns.size(), columnConstraints.size());

		double width = getTableView().snappedLeftInset() + getTableView().snappedRightInset();

		for (int i = 0; i < columnCount; i++) {
			ColumnSizeConstraints constraints = columnConstraints.get(i);

			if (constraints.getPercentWidth() > 0) {
				continue;
			}

			TableColumn<S, ?> column = columns.get(i);
			column.setPrefWidth(constraints.getPrefWidth());

			width += constraints.getPrefWidth();
		}

		return width;
	}

	private Pair<TableColumn<S, ?>, ColumnSizeConstraints> getColumnConstraints(ObservableList<TableColumn<S, ?>> columns, String text) {
		for (int i = 0; i < columns.size(); i++) {
			TableColumn<S, ?> column = columns.get(i);

			if (column.getText().equals(text) && i < getColumnConstraints().size()) {
				ColumnSizeConstraints constraints = getColumnConstraints().get(i);

				return new Pair<>(column, constraints);
			}
		}

		return null;
	}

	private ScrollBar getVerticalScrollbar() {
		ScrollBar vBar = null;

		for (Node node : getTableView().lookupAll(".scroll-bar")) {
			if (node instanceof ScrollBar) {
				ScrollBar scrollBar = (ScrollBar) node;

				if (scrollBar.getOrientation() == Orientation.VERTICAL) {
					vBar = scrollBar;
					break;
				}
			}
		}

		return vBar;
	}

	private double getScrollbarWidth() {
		if (nonNull(scrollbar) && scrollbar.isVisible()) {
			return Math.ceil(scrollbar.getWidth());
		}
		return 0;
	}

	private void updateColumnWidths() {
		TableView<S> table = getTableView();

		if (isNull(table)) {
			return;
		}

		if (columnConstraints.isEmpty()) {
			return;
		}

		ObservableList<TableColumn<S, ?>> columns = table.getColumns();

		staticWidth = getStaticColumnsWidth(columns);

		bindPercentageColumns(columns);
	}

	private void updateColumnTexts(Set<Node> nodes) {
		ObservableList<TableColumn<S, ?>> columns = getTableView().getColumns();

		// Calculate new total width of static columns.
		staticWidth = getTableView().snappedLeftInset() + getTableView().snappedRightInset();

		int i = 0;

		for (TableColumn<S, ?> column : columns) {
			Node node = column.getStyleableNode();

			ColumnSizeConstraints constraints = columnConstraints.get(i++);

			if (constraints.getPercentWidth() > 0) {
				continue;
			}

			staticWidth += node.prefWidth(-1);
		}

		// Notify bindings.
		Platform.runLater(() -> tableWidthBinding.invalidate());
	}


	private class ColumnWidthBinding extends DoubleBinding {

		ColumnWidthBinding() {
			bind(getTableView().widthProperty());
		}

		@Override
		protected double computeValue() {
			return getTableView().getWidth() - staticWidth - getScrollbarWidth();
		}

	}

}
