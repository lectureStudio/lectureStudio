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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.PopupWindow;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class TextSearchFieldSkin<T> extends ClearableTextFieldSkin {

	private final TextSearchField<T> searchField;

	private PopupControl popup;

	private ListView<T> listView;


	protected TextSearchFieldSkin(TextSearchField<T> control) {
		super(control);

		searchField = control;

		initLayout(control);
	}

	@Override
	public void dispose() {
		unregisterChangeListeners(searchField.textProperty());
		unregisterChangeListeners(searchField.cellFactoryProperty());
		unregisterChangeListeners(searchField.converterProperty());
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		reconfigurePopup();
		return 50;
	}

	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		reconfigurePopup();
		return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		reconfigurePopup();
		return super.computeMaxWidth(height, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		reconfigurePopup();
		return super.computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		reconfigurePopup();
		return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
	}

	@Override
	protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		reconfigurePopup();
		return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
	}

	protected void show() {
		if (popup.isShowing() || listView.getItems().isEmpty()) {
			return;
		}

		positionAndShowPopup();
	}

	protected void hide() {
		if (popup.isShowing()) {
			popup.hide();
		}
	}

	private void initLayout(TextSearchField<T> control) {
		createListView();
		createPopup();
		updateCellFactory();

		SvgIcon searchIcon = new SvgIcon();
		searchIcon.getStyleClass().addAll("search-icon");

		control.setLeftNode(searchIcon);
		control.getSuggestions().addListener((InvalidationListener) observable -> {
			ObservableList<T> suggestions = control.getSuggestions();

			if (suggestions.isEmpty()) {
				hide();
				return;
			}
			if (suggestions.size() == 1 && suggestions.get(0).equals(searchField.getText())) {
				return;
			}

			listView.getItems().setAll(suggestions);
			listView.getSelectionModel().select(-1);

			show();
		});

		registerChangeListener(control.textProperty(), o -> {
			String text = (String) o.getValue();

			if (isNull(text) || text.isEmpty() || text.isBlank()) {
				hide();
			}
		});
		registerChangeListener(control.cellFactoryProperty(), e -> updateCellFactory());
		registerChangeListener(control.converterProperty(), e -> {
			listView.getItems().setAll(control.getSuggestions());
			control.requestLayout();
		});
	}

	private void createPopup() {
		popup = new PopupControl() {

			@Override
			public Styleable getStyleableParent() {
				return TextSearchFieldSkin.this.getSkinnable();
			}

			{
				setSkin(new Skin<>() {

					@Override
					public Skinnable getSkinnable() {
						return TextSearchFieldSkin.this.getSkinnable();
					}

					@Override
					public Node getNode() {
						return listView;
					}

					@Override
					public void dispose() {
					}
				});
			}
		};
		popup.getScene().getStylesheets().add(getClass().getResource("/resources/css/text-search-field.css").toExternalForm());
		popup.getStyleClass().add("text-search-field-popup");
		popup.setConsumeAutoHidingEvents(false);
		popup.setAutoHide(true);
		popup.setAutoFix(true);
		popup.setHideOnEscape(true);
		popup.setOnAutoHide(e -> hide());
		popup.getScene().nodeOrientationProperty().bind(searchField.effectiveNodeOrientationProperty());
		popup.addEventHandler(MouseEvent.MOUSE_CLICKED, t -> {
			// Listen to mouse input that is received by the popup
			// but that is not consumed, and assume that this is due to the mouse
			// clicking outside of the node, but in areas such as the
			// dropshadow.
			hide();
		});
		popup.addEventHandler(WindowEvent.WINDOW_HIDDEN, t -> {
			// Make sure the accessibility focus returns to the search field
			// after the window closes.
			searchField.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
		});

		InvalidationListener layoutPosListener = o -> {
			reconfigurePopup();
		};
		searchField.layoutXProperty().addListener(layoutPosListener);
		searchField.layoutYProperty().addListener(layoutPosListener);
		searchField.widthProperty().addListener(layoutPosListener);
		searchField.heightProperty().addListener(layoutPosListener);
		searchField.sceneProperty().addListener(o -> {
			if (isNull(searchField.getScene())) {
				hide();
			}
		});
	}

	private void createListView() {
		listView = new ListView<>() {

			@Override
			protected double computeMinHeight(double width) {
				return 30;
			}

			@Override
			protected double computePrefWidth(double height) {
				return searchField.getWidth();
			}

			@Override
			protected double computePrefHeight(double width) {
				PopupListViewSkin<?> skin = (PopupListViewSkin<?>) listView.getSkin();
				final int maxRows = listView.getItems().size();
				final double prefWidth = computePrefWidth(-1);
				final double flowWidth = skin.getVirtualFlowWidth(maxRows);
				double scrollbarHeight = 0;

				if (flowWidth > prefWidth) {
					scrollbarHeight = skin.getScrollbarHeight();
				}

				return skin.getVirtualFlowHeight(maxRows) + scrollbarHeight;
			}

			@Override
			protected Skin<?> createDefaultSkin() {
				return new PopupListViewSkin<>(this);
			}
		};

		listView.setId("list-view");
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listView.setFocusTraversable(false);
		listView.addEventFilter(MouseEvent.MOUSE_RELEASED, t -> {
			EventTarget target = t.getTarget();
			if (target instanceof Parent) {
				List<String> s = ((Parent) target).getStyleClass();
				if (s.contains("thumb") ||
					s.contains("track") ||
					s.contains("decrement-arrow") ||
					s.contains("increment-arrow")) {
					return;
				}
			}

			hide();
		});
		listView.setOnKeyPressed(t -> {
			if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.ESCAPE) {
				hide();
			}
		});
		listView.getSelectionModel().selectedIndexProperty().addListener(o -> {
			T item = listView.getSelectionModel().getSelectedItem();

			if (nonNull(item)) {
				final StringConverter<T> c = searchField.getConverter();
				String s = isNull(c) ? item.toString() : c.toString(item);

				listView.requestLayout();

				searchField.setText(s);
				searchField.positionCaret(s.length());

				listView.requestLayout();
				listView.requestFocus();
			}
		});
	}

	private void updateCellFactory() {
		Callback<ListView<T>, ListCell<T>> cf = searchField.getCellFactory();

		if (isNull(cf)) {
			cf = getDefaultCellFactory();
		}

		listView.setCellFactory(cf);
	}

	private Callback<ListView<T>, ListCell<T>> getDefaultCellFactory() {
		return new Callback<>() {

			@Override
			public ListCell<T> call(ListView<T> listView) {
				return new ListCell<>() {

					@Override
					public void updateItem(T item, boolean empty) {
						super.updateItem(item, empty);

						if (empty) {
							setGraphic(null);
							setText(null);
						}
						else if (item instanceof Node) {
							Node currentNode = getGraphic();
							Node newNode = (Node) item;

							if (currentNode == null || !currentNode.equals(newNode)) {
								setText(null);
								setGraphic(newNode);
							}
						}
						else {
							final StringConverter<T> c = searchField.getConverter();
							String s = isNull(c) ?
									(isNull(item) ? null : item.toString()) :
									c.toString(item);

							setText(s);
							setGraphic(null);
						}
					}
				};
			}
		};
	}

	private Point2D getPrefPopupPosition() {
		Bounds bounds = searchField.localToScreen(searchField.getLayoutBounds());

		return new Point2D(bounds.getMinX(), bounds.getMaxY());
	}

	private void sizePopup() {
		double prefHeight = snapSizeY(listView.prefHeight(0));
		double minHeight = snapSizeY(listView.minHeight(0));
		double maxHeight = snapSizeY(listView.maxHeight(0));
		double h = snapSizeY(Math.min(Math.max(prefHeight, minHeight),
									  Math.max(minHeight, maxHeight)));

		double prefWidth = snapSizeX(listView.prefWidth(h));
		double minWidth = snapSizeX(listView.minWidth(h));
		double maxWidth = snapSizeX(listView.maxWidth(h));
		double w = snapSizeX(Math.min(Math.max(prefWidth, minWidth),
									  Math.max(minWidth, maxWidth)));

		listView.resize(w, h);
	}

	private void positionAndShowPopup() {
		if (searchField.getScene() == null) {
			return;
		}

		if (searchField.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
			popup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_RIGHT);
		}
		else {
			popup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
		}

		sizePopup();

		Point2D p = getPrefPopupPosition();

		reconfigurePopup();

		popup.show(searchField.getScene().getWindow(),
				   snapPositionX(p.getX()),
				   snapPositionY(p.getY()));

		listView.requestFocus();

		// Enable proper sizing _after_ the popup has been displayed.
		sizePopup();
	}

	private void reconfigurePopup() {
		if (!popup.isShowing()) {
			return;
		}

		final Point2D point = getPrefPopupPosition();

		final double minWidth = listView.prefWidth(Region.USE_COMPUTED_SIZE);
		final double minHeight = listView.prefHeight(Region.USE_COMPUTED_SIZE);

		if (point.getX() > -1) popup.setAnchorX(point.getX());
		if (point.getY() > -1) popup.setAnchorY(point.getY());
		if (minWidth > -1) popup.setMinWidth(minWidth);
		if (minHeight > -1) popup.setMinHeight(minHeight);

		final Bounds bounds = listView.getLayoutBounds();
		final double currentWidth = bounds.getWidth();
		final double currentHeight = bounds.getHeight();
		final double newWidth  = Math.max(currentWidth, minWidth);
		final double newHeight = Math.max(currentHeight, minHeight);

		if (newWidth != currentWidth || newHeight != currentHeight) {
			listView.resize(newWidth, newHeight);
			listView.setMinSize(newWidth, newHeight);
			listView.setPrefSize(newWidth, newHeight);
		}
	}



	private static class PopupListViewSkin<T> extends ListViewSkin<T> {

		final ListView<T> control;

		double scrollbarHeight;


		PopupListViewSkin(ListView<T> control) {
			super(control);

			this.control = control;

			control.heightProperty().addListener(o -> {

				for (Node node : control.lookupAll("VirtualScrollBar")) {
					if (node instanceof ScrollBar) {
						ScrollBar bar = (ScrollBar) node;

						if (bar.getOrientation().equals(Orientation.HORIZONTAL)) {
							scrollbarHeight = bar.prefHeight(-1);
						}
					}
				}
			});
		}

		@Override
		protected VirtualFlow<ListCell<T>> createVirtualFlow() {
			return new VirtualFlow<>() {

				{
					getChildren().remove(getVbar());
				}

			};
		}

		double getScrollbarHeight() {
			return scrollbarHeight;
		}

		double getVirtualFlowHeight(int rows) {
			double height = 1.0;

			for (int i = 0; i < rows && i < getItemCount(); i++) {
				height += getCellHeight(i);
			}

			return height + snappedTopInset() + snappedBottomInset();
		}

		double getVirtualFlowWidth(int rows) {
			double width = 1.0;

			for (int i = 0; i < rows && i < getItemCount(); i++) {
				width = Math.max(width, getCellWidth(i));
			}

			return width + snappedLeftInset() + snappedRightInset();
		}

		double getCellHeight(int index) {
			if (fixedCellSizeEnabled()) {
				return control.getFixedCellSize();
			}

			ListCell<T> cell = getVirtualFlow().getCell(index);

			return getCellHeight(cell);
		}

		double getCellWidth(int index) {
			ListCell<T> cell = getVirtualFlow().getCell(index);

			return getCellWidth(cell);
		}

		double getCellHeight(ListCell<T> cell) {
			if (isNull(cell)) {
				return 0;
			}
			if (fixedCellSizeEnabled()) {
				return control.getFixedCellSize();
			}

			return getVirtualFlow().isVertical() ?
					cell.getLayoutBounds().getHeight() :
					cell.getLayoutBounds().getWidth();
		}

		double getCellWidth(ListCell<T> cell) {
			if (isNull(cell)) {
				return 0;
			}

			return getVirtualFlow().isVertical() ?
					cell.getLayoutBounds().getWidth() :
					cell.getLayoutBounds().getHeight();
		}

		boolean fixedCellSizeEnabled() {
			return control.getFixedCellSize() > 0;
		}
	}
}
