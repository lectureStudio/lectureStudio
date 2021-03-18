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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class ExtSplitPane extends Control {

	private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");
	private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");

	private static final String DEFAULT_STYLE_CLASS = "ext-split-pane";

	private final Divider divider;

	private final ObjectProperty<Node> topLeftNode = new SimpleObjectProperty<>();

	private final ObjectProperty<Node> bottomRightNode = new SimpleObjectProperty<>();

	/**
	 * This property controls how the ExtSplitPane should be displayed to the
	 * user. {@link javafx.geometry.Orientation#HORIZONTAL} will result in
	 * two nodes being placed next to each other horizontally, whilst
	 * {@link javafx.geometry.Orientation#VERTICAL} will result in the nodes
	 * being stacked vertically.
	 */
	private ObjectProperty<Orientation> orientation;


	public ExtSplitPane() {
		this.divider = new Divider();

		initialize();
	}

	/**
	 * Returns the divider in this ExtSplitPane.
	 *
	 * @return the divider.
	 */
	public Divider getDivider() {
		return divider;
	}

	/**
	 * Returns the divider position.
	 *
	 * @return the divider position.
	 */
	public double getDividerPosition() {
		return divider.getPosition();
	}

	/**
	 * Sets the position of the divider.
	 *
	 * @param position the divider position, between 0.0 and 1.0 (inclusive).
	 */
	public void setDividerPosition(int dividerIndex, double position) {
		divider.setPosition(position);
	}

	public Node getTopNode() {
		return topLeftNode.get();
	}

	public void setTopNode(Node node) {
		topLeftNode.set(node);
	}

	public ObjectProperty<Node> topNodeProperty() {
		return topLeftNode;
	}

	public Node getBottomNode() {
		return bottomRightNode.get();
	}

	public void setBottomNode(Node node) {
		bottomRightNode.set(node);
	}

	public ObjectProperty<Node> bottomNodeProperty() {
		return bottomRightNode;
	}

	public Node getLeftNode() {
		return topLeftNode.get();
	}

	public void setLeftNode(Node node) {
		topLeftNode.set(node);
	}

	public ObjectProperty<Node> leftNodeProperty() {
		return topLeftNode;
	}

	public Node getRightNode() {
		return bottomRightNode.get();
	}

	public void setRightNode(Node node) {
		bottomRightNode.set(node);
	}

	public ObjectProperty<Node> rightNodeProperty() {
		return bottomRightNode;
	}

	/**
	 * Sets the orientation for the ExtSplitPane.
	 *
	 * @param value The orientation value.
	 */
	public final void setOrientation(Orientation value) {
		orientationProperty().set(value);
	};

	/**
	 * The orientation for the ExtSplitPane.
	 *
	 * @return The orientation for the ExtSplitPane.
	 */
	public final Orientation getOrientation() {
		return orientation == null ? Orientation.HORIZONTAL : orientation.get();
	}

	/**
	 * The orientation for the ExtSplitPane.
	 *
	 * @return the orientation property for the ExtSplitPane.
	 */
	public final ObjectProperty<Orientation> orientationProperty() {
		if (orientation == null) {
			orientation = new StyleableObjectProperty<>(Orientation.HORIZONTAL) {

				@Override
				public void invalidated() {
					final boolean isVertical = (get() == Orientation.VERTICAL);
					pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE,    isVertical);
					pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, !isVertical);
				}

				@Override
				public CssMetaData<ExtSplitPane, Orientation> getCssMetaData() {
					return StyleableProperties.ORIENTATION;
				}

				@Override
				public Object getBean() {
					return this;
				}

				@Override
				public String getName() {
					return "orientation";
				}
			};
		}
		return orientation;
	}

	/**
	 * @return The CssMetaData associated with this class, which may include the
	 * CssMetaData of its superclasses.
	 */
	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.STYLEABLES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return getClassCssMetaData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Skin<?> createDefaultSkin() {
		return new ExtSplitPaneSkin(this);
	}

	/**
	 * Returns the initial focus traversable state of this control, for use
	 * by the JavaFX CSS engine to correctly set its initial value. This method
	 * is overridden as by default UI controls have focus traversable set to true,
	 * but that is not appropriate for this control.
	 */
	@Override
	protected Boolean getInitialFocusTraversable() {
		return Boolean.FALSE;
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);

		// Initialize pseudo-class state.
		pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, true);
	}



	/**
	 * Represents the divider in the ExtSplitPane.
	 */
	public static class Divider {

		/**
		 * Represents the location where the divider should ideally be
		 * positioned, between 0.0 and 1.0 (inclusive). 0.0 represents the
		 * left- or top-most point, and 1.0 represents the right- or bottom-most
		 * point (depending on the horizontal property). The ExtSplitPane will attempt
		 * to get the divider to the point requested, but it must take into account
		 * the minimum width/height of the nodes contained within it.
		 *
		 * @defaultValue 0.5
		 */
		private DoubleProperty position;


		/**
		 * Creates a default Divider instance.
		 */
		public Divider() {
			position = new SimpleDoubleProperty(this, "position", 0.5);
		}

		public final void setPosition(double value) {
			positionProperty().set(value);
		}

		public final double getPosition() {
			return position == null ? 0.5 : position.get();
		}

		public final DoubleProperty positionProperty() {
			return position;
		}
	}



	private static class StyleableProperties {

		private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

		private static final CssMetaData<ExtSplitPane, Orientation> ORIENTATION =
				new CssMetaData<>("-fx-orientation", new EnumConverter<>(Orientation.class), Orientation.HORIZONTAL) {

					@Override
					public Orientation getInitialValue(ExtSplitPane node) {
						return node.getOrientation();
					}

					@Override
					public boolean isSettable(ExtSplitPane node) {
						return node.orientation == null || !node.orientation.isBound();
					}

					@Override
					public StyleableProperty<Orientation> getStyleableProperty(ExtSplitPane node) {
						return (StyleableProperty<Orientation>)(WritableValue<Orientation>)node.orientationProperty();
					}
				};

		static {
			final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
			styleables.add(ORIENTATION);

			STYLEABLES = Collections.unmodifiableList(styleables);
		}
	}

}
