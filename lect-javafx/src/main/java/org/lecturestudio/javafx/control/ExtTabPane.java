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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;

import org.lecturestudio.javafx.collections.UnmodifiableListSet;

@DefaultProperty("tabs")
public class ExtTabPane extends Control {

	private static final StyleablePropertyFactory<ExtTabPane> FACTORY = new StyleablePropertyFactory<>(Control.getClassCssMetaData());

	private static final PseudoClass TOP_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("top");
	private static final PseudoClass BOTTOM_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("bottom");
	private static final PseudoClass LEFT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("left");
	private static final PseudoClass RIGHT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("right");

	private final ObservableList<ExtTab> tabs = FXCollections.observableArrayList();

	private final ObjectProperty<SingleSelectionModel<ExtTab>> selectionModel;

	private final BooleanProperty tabToggle;

	/**
	 * The position to place the tabs in this ExtTabPane. Whenever this changes
	 * the ExtTabPane will immediately update the location of the tabs to reflect
	 * this.
	 */
	private ObjectProperty<Side> side;

	/**
	 * The text orientation in the tabs in this ExtTabPane. Whenever this changes
	 * the ExtTabPane will immediately update the text orientation in the tabs to
	 * reflect this.
	 */
	private ObjectProperty<Orientation> textOrientation;

	/**
	 * The minimum width of the tabs in the ExtTabPane. This can be used to limit
	 * the length of text in tabs to prevent truncation. Setting the min equal
	 * to the max will fix the width of the tab. The default value is set to 0.
	 *
	 * This value can also be set via CSS using {@code -fx-tab-min-width}.
	 */
	private final StyleableObjectProperty<Number> tabMinWidth;

	/**
	 * Specifies the maximum width of a tab. This can be used to limit
	 * the length of text in tabs. If the tab text is longer than the maximum
	 * width the text will be truncated. Setting the max equal to the min will
	 * fix the width of the tab. The default value is set to Double.MAX_VALUE.
	 *
	 * This value can also be set via CSS using {@code -fx-tab-max-width}.
	 */
	private final StyleableObjectProperty<Number> tabMaxWidth;

	/**
	 * The minimum height of the tabs in the ExtTabPane. This can be used to limit
	 * the height in tabs. Setting the min equal to the max will fix the height
	 * of the tab. The default value is set to 0.
	 *
	 * This value can also be set via CSS using {@code -fx-tab-min-height}.
	 */
	private final StyleableObjectProperty<Number> tabMinHeight;

	/**
	 * The maximum height if the tabs in the ExtTabPane. This can be used to limit
	 * the height in tabs. Setting the max equal to the min will fix the height
	 * of the tab. The default value is set to Double.MAX_VALUE.
	 *
	 * This value can also be set via CSS using {@code -fx-tab-max-height}.
	 */
	private final StyleableObjectProperty<Number> tabMaxHeight;


	/**
	 * Constructs a new ExtTabPane.
	 */
	public ExtTabPane() {
		selectionModel = new SimpleObjectProperty<>(this, "selectionModel");
		tabToggle = new SimpleBooleanProperty(false);

		tabMinWidth = (StyleableObjectProperty<Number>) FACTORY.createStyleableNumberProperty(this, "tabMinWidth", "-fx-tab-min-width", s -> s.tabMinWidth, 0);
		tabMaxWidth = (StyleableObjectProperty<Number>) FACTORY.createStyleableNumberProperty(this, "tabMaxWidth", "-fx-tab-max-width", s -> s.tabMaxWidth, Double.MAX_VALUE);
		tabMinHeight = (StyleableObjectProperty<Number>) FACTORY.createStyleableNumberProperty(this, "tabMinHeight", "-fx-tab-min-height", s -> s.tabMinHeight, 0);
		tabMaxHeight = (StyleableObjectProperty<Number>) FACTORY.createStyleableNumberProperty(this, "tabMaxHeight", "-fx-tab-max-height", s -> s.tabMaxHeight, Double.MAX_VALUE);

		initialize();
	}

	/**
	 * <p>The tabs to display in this ExtTabPane. Changing this ObservableList will
	 * immediately result in the ExtTabPane updating to display the new contents
	 * of this ObservableList.</p>
	 *
	 * <p>If the tabs ObservableList changes, the selected tab will remain the previously
	 * selected tab, if it remains within this ObservableList. If the previously
	 * selected tab is no longer in the tabs ObservableList, the selected tab will
	 * become the first tab in the ObservableList.</p>
	 *
	 * @return the list of tabs.
	 */
	public final ObservableList<ExtTab> getTabs() {
		return tabs;
	}

	/**
	 * Sets the model used for tab selection.  By changing the model you can alter
	 * how the tabs are selected and which tabs are first or last.
	 *
	 * @param value the selection model
	 */
	public final void setSelectionModel(SingleSelectionModel<ExtTab> value) {
		selectionModel.set(value);
	}

	/**
	 * Gets the model used for tab selection.
	 *
	 * @return the model used for tab selection
	 */
	public final SingleSelectionModel<ExtTab> getSelectionModel() {
		return selectionModel.get();
	}

	/**
	 * The selection model used for selecting tabs.
	 *
	 * @return selection model property
	 */
	public final ObjectProperty<SingleSelectionModel<ExtTab>> selectionModelProperty() {
		return selectionModel;
	}

	/**
	 * Gets the tab toggle mode.
	 *
	 * @return the tab toggle mode.
	 */
	public final boolean getTabToggle() {
		return tabToggle.get();
	}

	/**
	 * Sets the tab toggle mode.
	 *
	 * @param value true to enable tab toggle, false to disable.
	 */
	public final void setTabToggle(boolean value) {
		tabToggle.set(value);
	}

	/**
	 * The tab toggle mode.
	 *
	 * @return tab toggle mode property
	 */
	public final BooleanProperty tabToggleProperty() {
		return tabToggle;
	}

	/**
	 * @param value the side
	 */
	public final void setSide(Side value) {
		sideProperty().set(value);
	}

	/**
	 * The current position of the tabs in the ExtTabPane. The default position
	 * for the tabs is Side.Top.
	 *
	 * @return The current position of the tabs in the ExtTabPane.
	 */
	public final Side getSide() {
		return side == null ? Side.TOP : side.get();
	}

	/**
	 * The position of the tabs in the ExtTabPane.
	 *
	 * @return the side property
	 */
	public final ObjectProperty<Side> sideProperty() {
		if (side == null) {
			side = new ObjectPropertyBase<>(Side.TOP) {

				@Override
				protected void invalidated() {
					updatePseudoClassState(get());
				}

				@Override
				public Object getBean() {
					return ExtTabPane.this;
				}

				@Override
				public String getName() {
					return "side";
				}
			};
		}
		return side;
	}

	/**
	 * @param value the orientation of the text.
	 */
	public final void setTextOrientation(Orientation value) {
		textOrientationProperty().set(value);
	}

	/**
	 * The current text orientation of the tabs in the ExtTabPane. The default orientation
	 * of the text in the tabs is Orientation.VERTICAL.
	 *
	 * @return The current orientation of the text in the tabs.
	 */
	public final Orientation getTextOrientation() {
		return textOrientation == null ? Orientation.VERTICAL : textOrientation.get();
	}

	/**
	 * The text orientation of the tabs in the ExtTabPane.
	 *
	 * @return the text orientation property
	 */
	public final ObjectProperty<Orientation> textOrientationProperty() {
		if (textOrientation == null) {
			textOrientation = new ObjectPropertyBase<>(Orientation.VERTICAL) {

				@Override
				public Object getBean() {
					return ExtTabPane.this;
				}

				@Override
				public String getName() {
					return "textOrientation";
				}
			};
		}
		return textOrientation;
	}

	/**
	 * @param value the minimum width of the tabs
	 */
	public final void setTabMinWidth(double value) {
		tabMinWidth.set(value);
	}

	/**
	 * The minimum width of the tabs in the ExtTabPane.
	 *
	 * @return The minimum width of the tabs
	 */
	public final double getTabMinWidth() {
		return tabMinWidth.get().doubleValue();
	}

	/**
	 * The minimum width of the tabs in the ExtTabPane.
	 *
	 * @return the minimum width property
	 */
	public final ObservableValue<Number> tabMinWidthProperty() {
		return tabMinWidth;
	}

	public final void setTabMaxWidth(double value) {
		tabMaxWidth.set(value);
	}

	/**
	 * The maximum width of the tabs in the ExtTabPane.
	 *
	 * @return The maximum width of the tabs
	 */
	public final double getTabMaxWidth() {
		return tabMaxWidth.get().doubleValue();
	}

	/**
	 * The maximum width of the tabs in the ExtTabPane.
	 *
	 * @return the maximum width property
	 */
	public final ObservableValue<Number> tabMaxWidthProperty() {
		return tabMaxWidth;
	}

	/**
	 * @param value the minimum height of the tabs
	 */
	public final void setTabMinHeight(double value) {
		tabMinHeight.set(value);
	}

	/**
	 * The minimum height of the tabs in the ExtTabPane.
	 *
	 * @return the minimum height of the tabs
	 */
	public final double getTabMinHeight() {
		return tabMinHeight.get().doubleValue();
	}

	/**
	 * The minimum height of the tab.
	 *
	 * @return the minimum height property
	 */
	public final ObservableValue<Number> tabMinHeightProperty() {
		return tabMinHeight;
	}

	public final void setTabMaxHeight(double value) {
		tabMaxHeight.set(value);
	}

	/**
	 * The maximum height of the tabs in the ExtTabPane.
	 *
	 * @return The maximum height of the tabs
	 */
	public final double getTabMaxHeight() {
		return tabMaxHeight.get().doubleValue();
	}

	/**
	 * The maximum height of the tabs in the ExtTabPane.
	 *
	 * @return the maximum height of the tabs
	 */
	public final ObservableValue<Number> tabMaxHeightProperty() {
		return tabMaxHeight;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/ext-tab-pane.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new ExtTabPaneSkin(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node lookup(String selector) {
		Node n = super.lookup(selector);

		if (isNull(n)) {
			for (ExtTab tab : tabs) {
				n = tab.lookup(selector);

				if (nonNull(n)) {
					break;
				}
			}
		}
		return n;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Node> lookupAll(String selector) {
		if (isNull(selector)) {
			return null;
		}

		final List<Node> results = new ArrayList<>(super.lookupAll(selector));

		for (ExtTab tab : tabs) {
			results.addAll(tab.lookupAll(selector));
		}

		return new UnmodifiableListSet<>(results);
	}

	/**
	 * @return The CssMetaData associated with this class, which may include the
	 * CssMetaData of its superclasses.
	 */
	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return FACTORY.getCssMetaData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return getClassCssMetaData();
	}

	private void initialize() {
		getStyleClass().setAll("ext-tab-pane");
		setAccessibleRole(AccessibleRole.TAB_PANE);

		tabs.addListener((ListChangeListener<ExtTab>) c -> {
			while (c.next()) {
				for (ExtTab tab : c.getRemoved()) {
					if (tab != null && !getTabs().contains(tab)) {
						tab.setTabPane(null);
					}
				}
				for (ExtTab tab : c.getAddedSubList()) {
					if (tab != null) {
						tab.setTabPane(this);
					}
				}
			}
		});

		setSelectionModel(new TabPaneSelectionModel(this));

		updatePseudoClassState(getSide());
	}

	private void updatePseudoClassState(Side edge) {
		pseudoClassStateChanged(TOP_PSEUDOCLASS_STATE, (edge == Side.TOP || edge == null));
		pseudoClassStateChanged(RIGHT_PSEUDOCLASS_STATE, (edge == Side.RIGHT));
		pseudoClassStateChanged(BOTTOM_PSEUDOCLASS_STATE, (edge == Side.BOTTOM));
		pseudoClassStateChanged(LEFT_PSEUDOCLASS_STATE, (edge == Side.LEFT));
	}



	private static class TabPaneSelectionModel extends SingleSelectionModel<ExtTab> {

		private final ExtTabPane tabPane;


		TabPaneSelectionModel(ExtTabPane tabPane) {
			this.tabPane = tabPane;
		}

		@Override
		public void clearSelection() {
			ExtTab tab = getSelectedItem();

			if (nonNull(tab)) {
				tab.setSelected(false);
			}

			setSelectedIndex(-1);
			setSelectedItem(null);
		}

		@Override
		public void select(int index) {
			if (index == -1) {
				clearSelection();
				return;
			}

			final int itemCount = getItemCount();

			if (itemCount == 0 || index < 0 || index >= itemCount) {
				return;
			}

			int currentIndex = getSelectedIndex();

			if (currentIndex == index && tabPane.getTabToggle()) {
				// Toggle tab visibility.
				tabPane.getTabs().get(currentIndex).setSelected(false);

				clearSelection();

				tabPane.requestLayout();
				return;
			}

			// Deselect the old tab.
			if (currentIndex >= 0 && currentIndex < tabPane.getTabs().size()) {
				tabPane.getTabs().get(currentIndex).setSelected(false);
			}

			setSelectedIndex(index);
			setSelectedItem(getModelItem(index));

			currentIndex = getSelectedIndex();

			// Select the new tab.
			if (currentIndex >= 0 && currentIndex < tabPane.getTabs().size()) {
				tabPane.getTabs().get(currentIndex).setSelected(true);
			}
		}

		@Override
		protected ExtTab getModelItem(int index) {
			final ObservableList<ExtTab> items = tabPane.getTabs();

			if (isNull(items)) {
				return null;
			}
			if (index < 0 || index >= items.size()) {
				return null;
			}

			return items.get(index);
		}

		@Override
		protected int getItemCount() {
			final ObservableList<ExtTab> items = tabPane.getTabs();
			return isNull(items) ? 0 : items.size();
		}
	}

}
