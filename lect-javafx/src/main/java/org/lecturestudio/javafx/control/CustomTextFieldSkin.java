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

import javafx.scene.Node;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.text.HitInfo;

public class CustomTextFieldSkin extends TextFieldSkin {

	private Node left;

	private Node right;


	protected CustomTextFieldSkin(CustomTextField control) {
		super(control);

		initLayout(control);
	}

	@Override
	protected void layoutChildren(final double contentX, final double contentY,
								  final double contentWidth, final double contentHeight) {
		final double leftWidth = getNodeWidth(left, contentHeight);
		final double rightWidth = getNodeWidth(right, contentHeight);

		final double spacing = getNodeSpacing();

		final double textStartX = snapPositionX(contentX) + snapSizeX(leftWidth) + spacing;
		final double textWidth = contentWidth - snapSizeX(leftWidth) - snapSizeX(rightWidth) - spacing * 2;

		super.layoutChildren(textStartX, contentY, textWidth, contentHeight);

		if (nonNull(left)) {
			left.resizeRelocate(contentX, contentY, leftWidth, contentHeight);
		}
		if (nonNull(right)) {
			final double rightX = contentWidth - rightWidth + snappedLeftInset();
			right.resizeRelocate(rightX, contentY, rightWidth, contentHeight);
		}
	}

	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		final double width = super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
		final double spacing = getNodeSpacing() * 2;

		return width + spacing + getNodeWidth(left, height) + getNodeWidth(right, height);
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		final double height = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
		final double leftHeight = getNodeHeight(left, width);
		final double rightHeight = getNodeHeight(right, width);

		return Math.max(height, Math.max(leftHeight, rightHeight));
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		final double width = super.computeMinWidth(height, topInset, rightInset, bottomInset, leftInset);
		final double spacing = getNodeSpacing() * 2;

		return width + spacing + getNodeWidth(left, height) + getNodeWidth(right, height);
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		final double height = super.computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
		final double leftHeight = getNodeHeight(left, width);
		final double rightHeight = getNodeHeight(right, width);

		return Math.max(height, Math.max(leftHeight, rightHeight));
	}

	@Override
	public HitInfo getIndex(double x, double y) {
		final double leftWidth = getNodeWidth(left, getSkinnable().getHeight());
		return super.getIndex(x - leftWidth - getNodeSpacing(), y);
	}

	private void initLayout(CustomTextField control) {
		left = control.getLeftNode();
		right = control.getRightNode();

		manageNode(null, left);
		manageNode(null, right);

		control.leftNodeProperty().addListener((observable, oldValue, newValue) -> {
			left = newValue;
			manageNode(oldValue, newValue);
		});
		control.rightNodeProperty().addListener((observable, oldValue, newValue) -> {
			right = newValue;
			manageNode(oldValue, newValue);
		});
	}

	private void manageNode(Node oldNode, Node newNode) {
		if (nonNull(oldNode)) {
			getChildren().remove(oldNode);
		}
		if (nonNull(newNode)) {
			newNode.setManaged(false);
			getChildren().add(newNode);
		}
	}

	private double getNodeHeight(Node node, double width) {
		return nonNull(node) ? snapSizeY(node.prefHeight(width)) : 0;
	}

	private double getNodeWidth(Node node, double height) {
		return nonNull(node) ? snapSizeX(node.prefWidth(height)) : 0;
	}

	private double getNodeSpacing() {
		CustomTextField control = (CustomTextField) getSkinnable();

		return snapPositionX(control.getNodeSpacing());
	}
}
