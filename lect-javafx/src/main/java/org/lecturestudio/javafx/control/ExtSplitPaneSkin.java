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

import static java.util.Objects.nonNull;

import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.GridPane;

/**
 * Skin implementation for the {@link ExtSplitPane} control.
 *
 * @see ExtSplitPane
 */
public class ExtSplitPaneSkin extends SkinBase<ExtSplitPane> {

	private Orientation orientation;

	private DividerNode dividerNode;


	/**
	 * Constructor for all SkinBase instances.
	 *
	 * @param control The control for which this Skin should attach to.
	 */
	protected ExtSplitPaneSkin(ExtSplitPane control) {
		super(control);

		initLayout(control);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void layoutChildren(final double x, final double y, final double w, final double h) {
		final ExtSplitPane pane = getSkinnable();
		final double dividerPos = pane.getDividerPosition();

		if (orientation == Orientation.HORIZONTAL) {
			final Node leftNode = pane.getLeftNode();
			final Node rightNode = pane.getRightNode();

			final double dividerWidth = dividerNode.computePrefWidth(-1) / 2;

			final double leftWidth = w * dividerPos - dividerWidth;
			final double rightWidth = w - leftWidth - dividerWidth;

			if (nonNull(leftNode) && leftNode.isManaged()) {
				layoutInArea(leftNode, x, y, leftWidth, h, -1, HPos.CENTER, VPos.CENTER);
			}

			layoutInArea(dividerNode, x + leftWidth, y, dividerWidth * 2, h, -1, HPos.CENTER, VPos.CENTER);

			if (nonNull(rightNode) && rightNode.isManaged()) {
				layoutInArea(rightNode,x + leftWidth + dividerWidth * 2, y, rightWidth, h, -1, HPos.CENTER, VPos.CENTER);
			}
		}
		else if (orientation == Orientation.VERTICAL) {
			final Node topNode = pane.getTopNode();
			final Node bottomNode = pane.getBottomNode();

			final double topHeight = h * dividerPos;
			final double bottomHeight = h - topHeight;

			if (nonNull(topNode) && topNode.isManaged()) {
				layoutInArea(topNode, x, y, w, topHeight, -1, HPos.CENTER, VPos.CENTER);
			}
			if (nonNull(bottomNode) && bottomNode.isManaged()) {
				layoutInArea(bottomNode, x, y + topHeight, w, bottomHeight, -1, HPos.CENTER, VPos.CENTER);
			}
		}
	}

	private void initLayout(ExtSplitPane control) {
		orientation = control.getOrientation();

		if (orientation == Orientation.HORIZONTAL) {
			addNode(control.getLeftNode());
			addNode(control.getRightNode());
		}
		else if (orientation == Orientation.VERTICAL) {
			addNode(control.getTopNode());
			addNode(control.getBottomNode());
		}

		dividerNode = new DividerNode(orientation);

		addNode(dividerNode);

		registerChangeListener(control.orientationProperty(), e -> {
			orientation = control.getOrientation();

			removeNode(dividerNode);

			dividerNode = new DividerNode(orientation);

			addNode(dividerNode);

			getSkinnable().requestLayout();
		});
		registerChangeListener(control.getDivider().positionProperty(), e -> {
			getSkinnable().requestLayout();
		});

		control.leftNodeProperty().addListener((observable, oldValue, newValue) -> {
			updateChildren(oldValue, newValue);
		});
		control.rightNodeProperty().addListener((observable, oldValue, newValue) -> {
			updateChildren(oldValue, newValue);
		});
	}

	private void updateChildren(Node oldValue, Node newValue) {
		removeNode(oldValue);
		addNode(newValue);

		getSkinnable().requestLayout();
	}

	private void addNode(Node node) {
		if (nonNull(node)) {
			getChildren().add(node);
		}
	}

	private void removeNode(Node node) {
		if (nonNull(node)) {
			getChildren().remove(node);
		}
	}



	private static class DividerNode extends GridPane {

		private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");
		private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");


		DividerNode(Orientation orientation) {
			getStyleClass().setAll("ext-split-pane-divider");

			if (orientation == Orientation.HORIZONTAL) {
				setCursor(Cursor.H_RESIZE);
				pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, true);
			}
			else if (orientation == Orientation.VERTICAL) {
				setCursor(Cursor.V_RESIZE);
				pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, true);
			}
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
			return snappedLeftInset() + snappedRightInset();
		}

		@Override
		protected double computePrefHeight(double width) {
			return snappedTopInset() + snappedBottomInset();
		}

	}
}
