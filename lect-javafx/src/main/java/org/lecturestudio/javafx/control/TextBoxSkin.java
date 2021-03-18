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

import java.util.Set;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.transform.Transform;

import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.beans.converter.FontConverter;

public class TextBoxSkin extends PageObjectSkin<TextBox> {

	private LectStringProperty shapeTextProperty;

	private TextArea textArea;

	private Text textHolder;

	private double fontSize;


	TextBoxSkin(TextBox control) {
		super(control);
	}

	@Override
	protected Node createContent() {
		TextBox textBox = getSkinnable();
		textBox.fontProperty().set(FontConverter.INSTANCE.to(textBox.getPageShape().getFont()));

		fontSize = textBox.getPageShape().getFont().getSize();

		shapeTextProperty = new LectStringProperty(textBox.getPageShape().textProperty());

		textArea = new CustomTextArea();
		textArea.setEditable(true);
		textArea.setWrapText(false);
		textArea.textProperty().addListener(observable -> Platform.runLater(this::resize));
		textArea.fontProperty().bind(textBox.fontProperty());
		textArea.fontProperty().addListener((observable, oldFont, newFont) -> textArea.requestLayout());
		textArea.boundsInLocalProperty().addListener(new ChangeListener<>() {

			@Override
			public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
				if (textArea.getLayoutX() == 0 || textArea.getLayoutY() == 0) {
					return;
				}

				textArea.boundsInLocalProperty().removeListener(this);

				updateFontSize();
			}
		});

		textHolder = new Text();
		textHolder.fontProperty().bind(textArea.fontProperty());
		textHolder.textProperty().bind(textArea.textProperty());

		return textArea;
	}

	@Override
	protected void onParentChange(Node parent) {
		// Detached / removed from the scene graph.
		if (isNull(parent)) {
			// Remove bindings.
			textArea.textProperty().unbindBidirectional(shapeTextProperty);
			shapeTextProperty.unbind();
		}
	}

	@Override
	protected void onPostLayout() {
		updateFontSize();

		textArea.textProperty().bindBidirectional(shapeTextProperty);
	}

	@Override
	protected void onRelocateShape(Point2D location) {
		TextShape shape = getSkinnable().getPageShape();
		shape.setLocation(location);
	}

	@Override
	protected void onTransform() {
		updateFontSize();
	}

	private void updateFontSize() {
		TextBox textBox = getSkinnable();
		Transform transform = textBox.getPageTransform();
		Bounds textBounds = textArea.getBoundsInParent();
		Rectangle2D shapeRect = textBox.getPageShape().getBounds();

		Insets padding = textBox.getPadding();

		double xOffset = textBounds.getMinX() + padding.getLeft() + 1;
		double yOffset = textBounds.getMinY() + padding.getTop() + 1;

		double s = transform.getMxx();
		double tx = transform.getTx();
		double ty = transform.getTy();
		double x = Math.ceil((shapeRect.getX() + tx) * s) - xOffset;
		double y = Math.ceil((shapeRect.getY() + ty) * s) - yOffset;

		Font font = textBox.getPageShape().getFont().clone();
		font.setSize(fontSize * s);

		textBox.fontProperty().set(FontConverter.INSTANCE.to(font));
		textBox.relocate(x, y);

		resize();
	}



	private class CustomTextArea extends TextArea {

		private ScrollPane scrollPane;


		CustomTextArea() {
			getChildren().addListener(new InvalidationListener() {

				@Override
				public void invalidated(Observable observable) {
					getChildren().removeListener(this);

					scrollPane = (ScrollPane) getChildren().get(0);
				}
			});

			setCache(false);
			setCacheShape(false);
		}

		@Override
		protected void layoutChildren() {
			super.layoutChildren();

			Set<Node> textNodes = scrollPane.getContent().lookupAll(".text");

			// Remove style in order to place the TextShape right underneath
			// the TextArea text nodes.
			for (Node textNode : textNodes) {
				if (textNode instanceof Text) {
					Text text = (Text) textNode;
					text.setBoundsType(TextBoundsType.LOGICAL);
					text.fillProperty().unbind();
					text.setFill(Color.TRANSPARENT);
				}
			}

			// Hide scrollbars.
			ScrollBar scrollBarv = (ScrollBar) this.lookup(".scroll-bar:vertical");
			if (scrollBarv != null) {
				((ScrollPane) scrollBarv.getParent()).setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			}

			ScrollBar scrollBarh = (ScrollBar) this.lookup(".scroll-bar:horizontal");
			if (scrollBarh != null) {
				((ScrollPane) scrollBarh.getParent()).setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			}
		}

		@Override
		protected double computePrefWidth(double width) {
			return computeMinWidth(width);
		}

		@Override
		protected double computePrefHeight(double height) {
			return computeMinHeight(height);
		}

		@Override
		protected double computeMaxWidth(double width) {
			return computeMinWidth(width);
		}

		@Override
		protected double computeMaxHeight(double height) {
			return computeMinHeight(height);
		}

		@Override
		protected double computeMinWidth(double width) {
			Bounds bounds = textHolder.getLayoutBounds();
			Insets padding = getPadding();

			double paddingWidth = padding.getLeft() + padding.getRight();

			padding = scrollPane.getPadding();

			paddingWidth += padding.getLeft() + padding.getRight();

			return bounds.getWidth() + paddingWidth + 1;
		}

		@Override
		protected double computeMinHeight(double height) {
			Bounds bounds = textHolder.getLayoutBounds();
			Insets padding = getPadding();

			double paddingHeight = padding.getTop() + padding.getBottom();

			padding = scrollPane.getPadding();

			paddingHeight += padding.getTop() + padding.getBottom();

			return bounds.getHeight() + paddingHeight;
		}
	}

}
