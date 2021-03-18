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

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;

import org.lecturestudio.javafx.behavior.ExtTabPaneBehavior;

/**
 * Default skin implementation for the {@link ExtTabPane} control.
 *
 * @see ExtTabPane
 */
public class ExtTabPaneSkin extends SkinBase<ExtTabPane> {

	private static final PseudoClass SELECTED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("selected");
	private static final PseudoClass TOP_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("top");
	private static final PseudoClass BOTTOM_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("bottom");
	private static final PseudoClass LEFT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("left");
	private static final PseudoClass RIGHT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("right");
	private static final PseudoClass DISABLED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("disabled");

	private final ExtTabPaneBehavior behavior;

	private ObservableList<TabContentPane> tabContentPanes;

	private StackPane headerPane;


	/**
	 * Constructor for all SkinBase instances.
	 *
	 * @param control The control for which this Skin should attach to.
	 */
	protected ExtTabPaneSkin(ExtTabPane control) {
		super(control);

		behavior = new ExtTabPaneBehavior(control);

		initLayout(control);
		initSwipeHandlers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();

		if (nonNull(behavior)) {
			behavior.dispose();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double computeBaselineOffset(double topInset, double rightInset, double bottomInset, double leftInset) {
		Side tabPosition = getSkinnable().getSide();

		if (tabPosition == Side.TOP) {
			return headerPane.getBaselineOffset() + topInset;
		}

		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		// The TabPane can only be as wide as it widest content width.
		double maxWidth = 0.0;

		for (TabContentPane contentPane : tabContentPanes) {
			if (!contentPane.isVisible()) {
				continue;
			}
			maxWidth = Math.max(maxWidth, snapSizeX(contentPane.prefWidth(-1)));
		}

		final boolean isHorizontal = getSkinnable().getSide().isHorizontal();
		final double tabHeaderSize = isHorizontal ? snapSizeX(headerPane.prefWidth(-1)) : snapSizeY(headerPane.prefHeight(-1));
		double prefWidth = isHorizontal ? Math.max(maxWidth, tabHeaderSize) : maxWidth + tabHeaderSize;

		return snapSizeX(prefWidth) + rightInset + leftInset;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		// The TabPane can only be as high as it highest content height.
		double maxHeight = 0.0;

		for (TabContentPane contentPane : tabContentPanes) {
			if (!contentPane.isVisible()) {
				continue;
			}
			maxHeight = Math.max(maxHeight, snapSizeY(contentPane.prefHeight(-1)));
		}

		final boolean isHorizontal = getSkinnable().getSide().isHorizontal();
		final double tabHeaderSize = isHorizontal ? snapSizeY(headerPane.prefHeight(-1)) : snapSizeX(headerPane.prefWidth(-1));
		double prefHeight = isHorizontal ? maxHeight + snapSizeY(tabHeaderSize) : Math.max(maxHeight, tabHeaderSize);

		return snapSizeY(prefHeight) + topInset + bottomInset;
	}

	@Override
	protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		ExtTabPane tabPane = getSkinnable();
		ExtTab selectedTab = tabPane.getSelectionModel().getSelectedItem();

		final boolean isHorizontal = getSkinnable().getSide().isHorizontal();

		if (isNull(selectedTab) && !isHorizontal) {
			return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
		}
		else {
			return super.computeMaxWidth(height, topInset, rightInset, bottomInset, leftInset);
		}
	}

	@Override
	protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		ExtTabPane tabPane = getSkinnable();
		ExtTab selectedTab = tabPane.getSelectionModel().getSelectedItem();

		final boolean isHorizontal = getSkinnable().getSide().isHorizontal();

		if (isNull(selectedTab) && isHorizontal) {
			return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
		}
		else {
			return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
		}
	}

	@Override
	protected void layoutChildren(double x, double y, double w, double h) {
		final Side tabPosition = getSkinnable().getSide();

		final double headerHeight = tabPosition.isHorizontal() ?
				snapSizeY(headerPane.prefHeight(-1)) :
				snapSizeX(headerPane.prefHeight(-1));

		final double tabsStartX = tabPosition.equals(Side.RIGHT) ? x + w - headerHeight : x;
		final double tabsStartY = tabPosition.equals(Side.BOTTOM) ? y + h - headerHeight : y;

		double contentStartX = 0;
		double contentStartY = 0;
		double contentWidth = w - (tabPosition.isHorizontal() ? 0 : headerHeight);
		double contentHeight = h - (tabPosition.isHorizontal() ? headerHeight: 0);

		double leftInset = snappedLeftInset();
		double topInset = snappedTopInset();

		if (tabPosition.equals(Side.TOP)) {
			contentStartX = x;
			contentStartY = y + headerHeight;

			headerPane.resize(w, headerHeight);
			headerPane.relocate(tabsStartX, tabsStartY);
			headerPane.getTransforms().clear();
		}
		else if (tabPosition.equals(Side.RIGHT)) {
			contentStartX = x + leftInset;
			contentStartY = y;

			headerPane.resize(h, headerHeight);
			headerPane.relocate(tabsStartX, y - headerHeight);
			headerPane.getTransforms().clear();
			headerPane.getTransforms().add(new Rotate(getRotation(Side.RIGHT), 0, headerHeight));
		}
		else if (tabPosition.equals(Side.BOTTOM)) {
			contentStartX = x;
			contentStartY = y + topInset;

			headerPane.resize(w, headerHeight);
			headerPane.relocate(w + leftInset, tabsStartY - headerHeight);
			headerPane.getTransforms().clear();
			headerPane.getTransforms().add(new Rotate(getRotation(Side.BOTTOM), 0, headerHeight));
		}
		else if (tabPosition.equals(Side.LEFT)) {
			contentStartX = x + headerHeight;
			contentStartY = y;

			headerPane.resize(h, headerHeight);
			headerPane.relocate(tabsStartX + headerHeight, h - headerHeight + topInset);
			headerPane.getTransforms().clear();
			headerPane.getTransforms().add(new Rotate(getRotation(Side.LEFT), 0, headerHeight));
		}

		for (int i = 0; i < tabContentPanes.size(); i++) {
			TabContentPane content = tabContentPanes.get(i);

			if (isNull(content)) {
				continue;
			}

			content.setAlignment(Pos.TOP_LEFT);

			// We need to size all tabs, even if they aren't visible. For example,
			// see RT-29167.
			content.resize(contentWidth, contentHeight);
			content.relocate(contentStartX, contentStartY);
		}
	}

	private void initLayout(ExtTabPane control) {
		tabContentPanes = FXCollections.observableArrayList();

		headerPane = new TabHeaderPane();

		for (ExtTab tab : control.getTabs()) {
			addTab(tab);
		}

		getChildren().add(headerPane);

		if (control.getSelectionModel().getSelectedIndex() == -1) {
			control.getSelectionModel().selectFirst();
		}

		control.requestLayout();
	}

	private void initSwipeHandlers() {
		if (Platform.isSupported(ConditionalFeature.INPUT_TOUCH)) {
			getSkinnable().addEventHandler(SwipeEvent.SWIPE_LEFT, t -> behavior.selectNextTab());
			getSkinnable().addEventHandler(SwipeEvent.SWIPE_RIGHT, t -> behavior.selectPreviousTab());
		}
	}

	private void addTab(ExtTab tab) {
		TabHeader tabHeader = new TabHeader(tab);

		TabContentPane tabContent = new TabContentPane(tab);
		tabContentPanes.add(tabContent);

		getChildren().add(0, tabContent);

		headerPane.getChildren().add(tabHeader);
	}

	private void removeTab(ExtTab tab) {
		for (TabContentPane tabContent : tabContentPanes) {
			if (tabContent.getTab().equals(tab)) {
				tabContent.removeListeners();
				getChildren().remove(tabContent);
				tabContentPanes.remove(tabContent);
				break;
			}
		}
	}

	private static int getRotation(Side pos) {
		switch (pos) {
			case TOP:
				return 0;
			case BOTTOM:
				return 180;
			case LEFT:
				return -90;
			case RIGHT:
				return 90;
			default:
				return 0;
		}
	}



	private class TabHeader extends StackPane {

		private final ExtTab tab;

		private Label label;


		TabHeader(final ExtTab tab) {
			this.tab = tab;

			initialize();
		}

		@Override
		protected void layoutChildren() {
			final double width = getWidth();
			final double height = getHeight();

			double w = label.prefWidth(-1);
			double h = label.prefHeight(-1);

			Side side = getSkinnable().getSide();
			Orientation orientation = getSkinnable().getTextOrientation();

			if ((side == Side.LEFT || side == Side.RIGHT) && orientation == Orientation.HORIZONTAL) {
				label.relocate((width - w) / 2, w / 2 - getInsets().getTop());
			}
			else {
				label.relocate((width - w) / 2, (height - h) / 2);
			}

			label.resize(w, h);
		}

		@Override
		protected double computeMinWidth(double height) {
			return computePrefWidth(height);
		}

		@Override
		protected double computeMinHeight(double width) {
			return computePrefHeight(width);
		}

		@Override
		protected double computePrefWidth(double height) {
			Side side = getSkinnable().getSide();
			Orientation orientation = getSkinnable().getTextOrientation();

			double prefWidth;

			if ((side == Side.LEFT || side == Side.RIGHT) && orientation == Orientation.HORIZONTAL) {
				prefWidth = snapSizeX(label.getHeight());

				double minWidth = snapSizeX(getSkinnable().getTabMinHeight());
				double maxWidth = snapSizeX(getSkinnable().getTabMaxHeight());

				if (prefWidth > maxWidth) {
					prefWidth = maxWidth;
				}
				else if (prefWidth < minWidth) {
					prefWidth = minWidth;
				}

				prefWidth += snappedTopInset() + snappedBottomInset();
			}
			else {
				prefWidth = snapSizeX(label.prefWidth(-1));

				double minWidth = snapSizeX(getSkinnable().getTabMinWidth());
				double maxWidth = snapSizeX(getSkinnable().getTabMaxWidth());

				if (prefWidth > maxWidth) {
					prefWidth = maxWidth;
				}
				else if (prefWidth < minWidth) {
					prefWidth = minWidth;
				}

				prefWidth += snappedRightInset() + snappedLeftInset();
			}

			return prefWidth;
		}

		@Override
		protected double computePrefHeight(double width) {
			Side side = getSkinnable().getSide();
			Orientation orientation = getSkinnable().getTextOrientation();

			double prefHeight;

			if ((side == Side.LEFT || side == Side.RIGHT) && orientation == Orientation.HORIZONTAL) {
				prefHeight = snapSizeX(label.getWidth());

				double minHeight = snapSizeX(getSkinnable().getTabMinWidth());
				double maxHeight = snapSizeX(getSkinnable().getTabMaxWidth());

				if (prefHeight > maxHeight) {
					prefHeight = maxHeight;
				}
				else if (prefHeight < minHeight) {
					prefHeight = minHeight;
				}

				prefHeight += snappedRightInset() + snappedLeftInset();
			}
			else {
				prefHeight = snapSizeX(label.prefHeight(width));

				double minHeight = snapSizeX(getSkinnable().getTabMinHeight());
				double maxHeight = snapSizeX(getSkinnable().getTabMaxHeight());

				if (prefHeight > maxHeight) {
					prefHeight = maxHeight;
				}
				else if (prefHeight < minHeight) {
					prefHeight = minHeight;
				}

				prefHeight += snappedTopInset() + snappedBottomInset();
			}

			return prefHeight;
		}

		private void rotate() {
			double rotate = 0.0;

			Side side = getSkinnable().getSide();
			Orientation orientation = getSkinnable().getTextOrientation();

			if ((side == Side.LEFT || side == Side.RIGHT) && orientation == Orientation.HORIZONTAL) {
				rotate = -getRotation(side);
			}
			else if (side == Side.BOTTOM) {
				rotate = getRotation(side);
			}

			label.setRotate(rotate);
		}

		private void initialize() {
			getStyleClass().setAll(tab.getStyleClass());
			setId(tab.getId());
			setStyle(tab.getStyle());
			setAccessibleRole(AccessibleRole.TAB_ITEM);

			label = new Label(tab.getText(), tab.getGraphic());
			label.getStyleClass().setAll("ext-tab-label");

			rotate();

			getChildren().add(label);

			Tooltip tooltip = tab.getTooltip();

			if (tooltip != null) {
				Tooltip.install(this, tooltip);
			}

			tab.selectedProperty().addListener(observable -> {
				pseudoClassStateChanged(SELECTED_PSEUDOCLASS_STATE, tab.isSelected());

				// Need to request a layout pass for inner because if the width
				// and height didn't not change the label or close button may have
				// changed.
				requestLayout();
			});
			tab.textProperty().addListener(observable -> label.setText(tab.getText()));
			tab.graphicProperty().addListener(observable -> label.setGraphic(tab.getGraphic()));
			tab.tooltipProperty().addListener((observable, oldTooltip, newTooltip) -> {
				if (oldTooltip != null) {
					Tooltip.uninstall(this, oldTooltip);
				}
				if (newTooltip != null) {
					// install new tooltip and save as old tooltip.
					Tooltip.install(this, newTooltip);
				}
			});
			tab.disableProperty().addListener(observable -> {
				pseudoClassStateChanged(DISABLED_PSEUDOCLASS_STATE, tab.isDisable());
				requestLayout();
			});
			tab.styleProperty().addListener(observable -> setStyle(tab.getStyle()));
			tab.getStyleClass().addListener((ListChangeListener<? super String>) c -> getStyleClass().setAll(tab.getStyleClass()));

			getSkinnable().sideProperty().addListener(observable -> {
				final Side side = getSkinnable().getSide();

				pseudoClassStateChanged(TOP_PSEUDOCLASS_STATE, (side == Side.TOP));
				pseudoClassStateChanged(RIGHT_PSEUDOCLASS_STATE, (side == Side.RIGHT));
				pseudoClassStateChanged(BOTTOM_PSEUDOCLASS_STATE, (side == Side.BOTTOM));
				pseudoClassStateChanged(LEFT_PSEUDOCLASS_STATE, (side == Side.LEFT));

				rotate();
			});
			getSkinnable().textOrientationProperty().addListener(observable -> {
				rotate();
				requestLayout();
				getSkinnable().requestLayout();
			});
			getSkinnable().tabMinWidthProperty().addListener(observable -> {
				requestLayout();
				getSkinnable().requestLayout();
			});
			getSkinnable().tabMaxWidthProperty().addListener(observable -> {
				requestLayout();
				getSkinnable().requestLayout();
			});
			getSkinnable().tabMinHeightProperty().addListener(observable -> {
				requestLayout();
				getSkinnable().requestLayout();
			});
			getSkinnable().tabMaxHeightProperty().addListener(observable -> {
				requestLayout();
				getSkinnable().requestLayout();
			});

			getProperties().put(ExtTab.class, tab);
			getProperties().put(ContextMenu.class, tab.getContextMenu());

			setOnContextMenuRequested((ContextMenuEvent event) -> {
				if (tab.getContextMenu() != null) {
					tab.getContextMenu().show(label, event.getScreenX(), event.getScreenY());
					event.consume();
				}
			});
			setOnMousePressed(event -> {
				if (tab.isDisable()) {
					return;
				}
				if (event.getButton().equals(MouseButton.PRIMARY)) {
					behavior.selectTab(tab);
				}
			});

			final Side side = getSkinnable().getSide();

			pseudoClassStateChanged(SELECTED_PSEUDOCLASS_STATE, tab.isSelected());
			pseudoClassStateChanged(DISABLED_PSEUDOCLASS_STATE, tab.isDisable());
			pseudoClassStateChanged(TOP_PSEUDOCLASS_STATE, (side == Side.TOP));
			pseudoClassStateChanged(RIGHT_PSEUDOCLASS_STATE, (side == Side.RIGHT));
			pseudoClassStateChanged(BOTTOM_PSEUDOCLASS_STATE, (side == Side.BOTTOM));
			pseudoClassStateChanged(LEFT_PSEUDOCLASS_STATE, (side == Side.LEFT));
		}
	}



	private class TabHeaderPane extends StackPane {

		TabHeaderPane() {
			getStyleClass().setAll("ext-tab-header");
		}

		@Override
		protected double computePrefWidth(double height) {
			double width = 0;

			for (Node child : getChildren()) {
				if (child.isVisible()) {
					width += child.prefWidth(height);
				}
			}

			return snapSizeX(width) + snappedLeftInset() + snappedRightInset();
		}

		@Override
		protected double computePrefHeight(double width) {
			double height = 0;

			for (Node child : getChildren()) {
				height = Math.max(height, child.prefHeight(width));
			}

			return snapSizeY(height) + snappedTopInset() + snappedBottomInset();
		}

		@Override
		protected void layoutChildren() {
			Side side = getSkinnable().getSide();
			Orientation orientation = getSkinnable().getTextOrientation();

			double tabX = (side.equals(Side.LEFT) || side.equals(Side.BOTTOM))
					? snapSizeX(getWidth()) - snappedLeftInset()
					: snappedLeftInset();

			boolean sameSize = false;

			double maxTabHeaderWidth = 0;
			double maxTabHeaderHeight = 0;

			if ((side == Side.LEFT || side == Side.RIGHT) && orientation == Orientation.HORIZONTAL) {
				sameSize = true;

				// Find the largest tab.
				for (Node tabHeader : getChildren()) {
					maxTabHeaderWidth = Math.max(maxTabHeaderWidth, snapSizeX(tabHeader.prefWidth(-1)));
					maxTabHeaderHeight = Math.max(maxTabHeaderHeight, snapSizeY(tabHeader.prefHeight(-1)));
				}
			}

			for (Node tabHeader : getChildren()) {
				// Size and position the header relative to the other headers.
				double tabHeaderPrefWidth = sameSize ? maxTabHeaderWidth : snapSizeX(tabHeader.prefWidth(-1));
				double tabHeaderPrefHeight = sameSize ? maxTabHeaderHeight : snapSizeY(tabHeader.prefHeight(-1));

				double tabY = snappedTopInset() + snappedBottomInset();

				tabHeader.resize(tabHeaderPrefWidth, tabHeaderPrefHeight);

				if (side.equals(Side.LEFT) || side.equals(Side.BOTTOM)) {
					tabX -= tabHeaderPrefWidth;
					tabHeader.relocate(tabX, tabY);
				}
				else {
					tabHeader.relocate(tabX, tabY);
					tabX += tabHeaderPrefWidth;
				}
			}
		}
	}



	private static class TabContentPane extends StackPane {

		private ExtTab tab;

		private final InvalidationListener contentListener = valueModel -> updateContent();

		private final InvalidationListener selectedListener = valueModel -> setVisible(tab.isSelected());


		TabContentPane(ExtTab tab) {
			this.tab = tab;

			getStyleClass().setAll("ext-tab-content");
			setManaged(false);
			setVisible(tab.isSelected());

			updateContent();

			tab.selectedProperty().addListener(selectedListener);
			tab.contentProperty().addListener(contentListener);
		}

		private void removeListeners() {
			tab.selectedProperty().removeListener(selectedListener);
			tab.contentProperty().removeListener(contentListener);
		}

		public ExtTab getTab() {
			return tab;
		}

		private void updateContent() {
			Node content = getTab().getContent();

			if (isNull(content)) {
				getChildren().clear();
			}
			else {
				getChildren().setAll(content);
			}
		}
	}
}
