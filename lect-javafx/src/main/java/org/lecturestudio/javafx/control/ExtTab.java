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

import static javafx.scene.control.Tab.SELECTION_CHANGED_EVENT;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tooltip;

import org.lecturestudio.javafx.internal.event.EventHandlerManager;
import org.lecturestudio.javafx.internal.util.ControlAcceleratorSupport;

@DefaultProperty("content")
public class ExtTab implements EventTarget, Styleable {

	private static final String DEFAULT_STYLE_CLASS = "ext-tab";

	private static final Object USER_DATA_KEY = new Object();

	/**
	 * Called when the tab becomes selected or unselected.
	 */
	//public static final EventType<Event> SELECTION_CHANGED_EVENT = new EventType<Event>(Event.ANY, "SELECTION_CHANGED_EVENT");

	private final InvalidationListener parentDisabledChangedListener = valueModel -> {
		updateDisabled();
	};

	private final ObservableList<String> styleClass = FXCollections.observableArrayList();

	private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

	private StringProperty id;

	private StringProperty style;

	private StringProperty text;

	private ObjectProperty<Node> graphic;

	private ObjectProperty<Node> content;

	private ObjectProperty<ContextMenu> contextMenu;

	private ObjectProperty<EventHandler<Event>> onSelectionChanged;

	private ObjectProperty<Tooltip> tooltip;

	/* A map containing a set of properties for this Tab. */
	private ObservableMap<Object, Object> properties;

	private BooleanProperty disable;

	private ReadOnlyBooleanWrapper disabled;

	private ReadOnlyBooleanWrapper selected;

	private ReadOnlyObjectWrapper<ExtTabPane> tabPane;


	/**
	 * Creates a tab with no title.
	 */
	public ExtTab() {
		this(null);
	}

	/**
	 * Creates a tab with a text title.
	 *
	 * @param text The title of the tab.
	 */
	public ExtTab(String text) {
		this(text, null);
	}

	/**
	 * Creates a tab with a text title and the specified content node.
	 *
	 * @param text    The title of the tab.
	 * @param content The content of the tab.
	 */
	public ExtTab(String text, Node content) {
		setText(text);
		setContent(content);

		styleClass.addAll(DEFAULT_STYLE_CLASS);
	}

	/**
	 * Sets the id of this tab. This simple string identifier is useful for
	 * finding a specific Tab within the {@code TabPane}. The default value is {@code null}.
	 *
	 * @param value the id of this tab
	 */
	public final void setId(String value) {
		idProperty().set(value);
	}

	/**
	 * The id of this tab.
	 *
	 * @return The id of the tab.
	 */
	@Override
	public final String getId() {
		return id == null ? null : id.get();
	}

	/**
	 * The id of this tab.
	 *
	 * @return the id property of this tab
	 */
	public final StringProperty idProperty() {
		if (id == null) {
			id = new SimpleStringProperty(this, "id");
		}
		return id;
	}

	/**
	 * A string representation of the CSS style associated with this
	 * tab. This is analogous to the "style" attribute of an
	 * HTML element. Note that, like the HTML style attribute, this
	 * variable contains style properties and values and not the
	 * selector portion of a style rule.
	 * <p>
	 * Parsing this style might not be supported on some limited
	 * platforms. It is recommended to use a standalone CSS file instead.
	 *
	 * @param value the style string
	 */
	public final void setStyle(String value) {
		styleProperty().set(value);
	}

	/**
	 * The CSS style string associated to this tab.
	 *
	 * @return The CSS style string associated to this tab.
	 */
	@Override
	public final String getStyle() {
		return style == null ? null : style.get();
	}

	/**
	 * The CSS style string associated to this tab.
	 *
	 * @return the CSS style string property associated to this tab
	 */
	public final StringProperty styleProperty() {
		if (style == null) {
			style = new SimpleStringProperty(this, "style");
		}
		return style;
	}

	/**
	 * Sets the text to show in the tab to allow the user to differentiate between
	 * the function of each tab. The text is always visible
	 *
	 *
	 * @param value the text string
	 */
	public final void setText(String value) {
		textProperty().set(value);
	}

	/**
	 * The text shown in the tab.
	 *
	 * @return The text shown in the tab.
	 */
	public final String getText() {
		return text == null ? null : text.get();
	}

	/**
	 * The text shown in the tab.
	 *
	 * @return the text property
	 */
	public final StringProperty textProperty() {
		if (text == null) {
			text = new SimpleStringProperty(this, "text");
		}
		return text;
	}

	/**
	 * <p>Sets the graphic to show in the tab to allow the user to differentiate
	 * between the function of each tab. By default the graphic does not rotate
	 * based on the TabPane.tabPosition value, but it can be set to rotate by
	 * setting TabPane.rotateGraphic to true.</p>
	 *
	 * @param value the graphic node
	 */
	public final void setGraphic(Node value) {
		graphicProperty().set(value);
	}

	/**
	 * The graphic shown in the tab.
	 *
	 * @return The graphic shown in the tab.
	 */
	public final Node getGraphic() {
		return graphic == null ? null : graphic.get();
	}

	/**
	 * The graphic in the tab.
	 *
	 * @return The graphic in the tab.
	 */
	public final ObjectProperty<Node> graphicProperty() {
		if (graphic == null) {
			graphic = new SimpleObjectProperty<Node>(this, "graphic");
		}
		return graphic;
	}

	/**
	 * The content to show within the main TabPane area. The content
	 * can be any Node such as UI controls or groups of nodes added
	 * to a layout container.
	 *
	 * @param value the content node
	 */
	public final void setContent(Node value) {
		contentProperty().set(value);
	}

	/**
	 * The content associated with the tab.
	 *
	 * @return The content associated with the tab.
	 */
	public final Node getContent() {
		return content == null ? null : content.get();
	}

	/**
	 * The content associated with the tab.
	 *
	 * @return the content property
	 */
	public final ObjectProperty<Node> contentProperty() {
		if (content == null) {
			content = new SimpleObjectProperty<Node>(this, "content");
		}
		return content;
	}

	/**
	 * Specifies the context menu to show when the user right-clicks on the tab.
	 *
	 * @param value the context menu
	 */
	public final void setContextMenu(ContextMenu value) {
		contextMenuProperty().set(value);
	}

	/**
	 * The context menu associated with the tab.
	 *
	 * @return The context menu associated with the tab.
	 */
	public final ContextMenu getContextMenu() {
		return contextMenu == null ? null : contextMenu.get();
	}

	/**
	 * The context menu associated with the tab.
	 *
	 * @return the context menu property
	 */
	public final ObjectProperty<ContextMenu> contextMenuProperty() {
		if (contextMenu == null) {
			contextMenu = new SimpleObjectProperty<ContextMenu>(this, "contextMenu") {

				private WeakReference<ContextMenu> contextMenuRef;

				@Override
				protected void invalidated() {
					ContextMenu oldMenu = contextMenuRef == null ? null : contextMenuRef.get();

					if (oldMenu != null) {
						ControlAcceleratorSupport.removeAcceleratorsFromScene(oldMenu.getItems(), ExtTab.this);
					}

					ContextMenu ctx = get();
					contextMenuRef = new WeakReference<>(ctx);

					if (ctx != null) {
						// if a context menu is set, we need to install any accelerators
						// belonging to its menu items ASAP into the scene that this
						// Control is in (if the control is not in a Scene, we will need
						// to wait until it is and then do it).
						ControlAcceleratorSupport.addAcceleratorsIntoScene(ctx.getItems(), ExtTab.this);
					}
				}
			};
		}
		return contextMenu;
	}

	/**
	 * Defines a function to be called when a selection changed has occurred on the tab.
	 *
	 * @param value the on selection changed event handler
	 */
	public final void setOnSelectionChanged(EventHandler<Event> value) {
		onSelectionChangedProperty().set(value);
	}

	/**
	 * The event handler that is associated with a selection on the tab.
	 *
	 * @return The event handler that is associated with a tab selection.
	 */
	public final EventHandler<Event> getOnSelectionChanged() {
		return onSelectionChanged == null ? null : onSelectionChanged.get();
	}

	/**
	 * The event handler that is associated with a selection on the tab.
	 *
	 * @return the on selection changed event handler property
	 */
	public final ObjectProperty<EventHandler<Event>> onSelectionChangedProperty() {
		if (onSelectionChanged == null) {
			onSelectionChanged = new ObjectPropertyBase<>() {

				@Override
				protected void invalidated() {
					setEventHandler(SELECTION_CHANGED_EVENT, get());
				}

				@Override
				public Object getBean() {
					return ExtTab.this;
				}

				@Override
				public String getName() {
					return "onSelectionChanged";
				}
			};
		}
		return onSelectionChanged;
	}

	/**
	 * Specifies the tooltip to show when the user hovers over the tab.
	 *
	 * @param value the tool tip value
	 */
	public final void setTooltip(Tooltip value) {
		tooltipProperty().setValue(value);
	}

	/**
	 * The tooltip associated with this tab.
	 *
	 * @return The tooltip associated with this tab.
	 */
	public final Tooltip getTooltip() {
		return tooltip == null ? null : tooltip.getValue();
	}

	/**
	 * The tooltip associated with this tab.
	 *
	 * @return the tool tip property
	 */
	public final ObjectProperty<Tooltip> tooltipProperty() {
		if (tooltip == null) {
			tooltip = new SimpleObjectProperty<Tooltip>(this, "tooltip");
		}
		return tooltip;
	}

	/**
	 * Returns an observable map of properties on this Tab for use primarily
	 * by application developers.
	 *
	 * @return an observable map of properties on this Tab for use primarily
	 * by application developers
	 */
	public final ObservableMap<Object, Object> getProperties() {
		if (properties == null) {
			properties = FXCollections.observableMap(new HashMap<Object, Object>());
		}
		return properties;
	}

	/**
	 * Tests if this Tab has properties.
	 *
	 * @return true if this tab has properties.
	 */
	public boolean hasProperties() {
		return properties != null && !properties.isEmpty();
	}

	/**
	 * Convenience method for setting a single Object property that can be
	 * retrieved at a later date. This is functionally equivalent to calling
	 * the getProperties().put(Object key, Object value) method. This can later
	 * be retrieved by calling {@link ExtTab#getUserData()}.
	 *
	 * @param value The value to be stored - this can later be retrieved by calling
	 *              {@link ExtTab#getUserData()}.
	 */
	public void setUserData(Object value) {
		getProperties().put(USER_DATA_KEY, value);
	}

	/**
	 * Returns a previously set Object property, or null if no such property
	 * has been set using the {@link ExtTab#setUserData(java.lang.Object)} method.
	 *
	 * @return The Object that was previously set, or null if no property
	 * has been set or if null was set.
	 */
	public Object getUserData() {
		return getProperties().get(USER_DATA_KEY);
	}

	/**
	 * Sets the disabled state of this tab.
	 *
	 * @param value the state to set this tab
	 *
	 * @defaultValue false
	 */
	public final void setDisable(boolean value) {
		disableProperty().set(value);
	}

	/**
	 * Returns {@code true} if this tab is disable.
	 *
	 * @return true if this tab is disable
	 */
	public final boolean isDisable() {
		return disable != null && disable.get();
	}

	/**
	 * Sets the disabled state of this tab. A disable tab is no longer interactive
	 * or traversable, but the contents remain interactive.  A disable tab
	 * can be selected using {@link ExtTabPane#getSelectionModel()}.
	 *
	 * @return the disable property
	 *
	 * @defaultValue false
	 */
	public final BooleanProperty disableProperty() {
		if (disable == null) {
			disable = new BooleanPropertyBase(false) {

				@Override
				protected void invalidated() {
					updateDisabled();
				}

				@Override
				public Object getBean() {
					return ExtTab.this;
				}

				@Override
				public String getName() {
					return "disable";
				}
			};
		}
		return disable;
	}

	private final void setDisabled(boolean value) {
		disabledPropertyImpl().set(value);
	}

	/**
	 * Returns true when the {@code Tab} {@link #disableProperty disable} is set to
	 * {@code true} or if the {@code TabPane} is disabled.
	 *
	 * @return true if the TabPane is disabled
	 */
	public final boolean isDisabled() {
		return disabled != null && disabled.get();
	}

	/**
	 * Indicates whether or not this {@code Tab} is disabled.  A {@code Tab}
	 * will become disabled if {@link #disableProperty disable} is set to {@code true} on either
	 * itself or if the {@code TabPane} is disabled.
	 *
	 * @return the disabled property
	 *
	 * @defaultValue false
	 */
	public final ReadOnlyBooleanProperty disabledProperty() {
		return disabledPropertyImpl().getReadOnlyProperty();
	}

	private ReadOnlyBooleanWrapper disabledPropertyImpl() {
		if (disabled == null) {
			disabled = new ReadOnlyBooleanWrapper() {

				@Override
				public Object getBean() {
					return ExtTab.this;
				}

				@Override
				public String getName() {
					return "disabled";
				}
			};
		}
		return disabled;
	}

	private void updateDisabled() {
		boolean disabled = isDisable() || (getTabPane() != null && getTabPane().isDisabled());

		setDisabled(disabled);

		// Fix for RT-24658 - content should be disabled if the tab is disabled
		Node content = getContent();

		if (content != null) {
			content.setDisable(disabled);
		}
	}

	final void setSelected(boolean value) {
		selectedPropertyImpl().set(value);
	}

	/**
	 * Represents whether this tab is the currently selected tab.
	 * To change the selected Tab use {@code tabPane.getSelectionModel().select()}
	 *
	 *
	 * @return true if selected
	 */
	public final boolean isSelected() {
		return selected != null && selected.get();
	}

	/**
	 * The currently selected tab.
	 *
	 * @return the selected tab
	 */
	public final ReadOnlyBooleanProperty selectedProperty() {
		return selectedPropertyImpl().getReadOnlyProperty();
	}

	private ReadOnlyBooleanWrapper selectedPropertyImpl() {
		if (selected == null) {
			selected = new ReadOnlyBooleanWrapper() {

				@Override
				protected void invalidated() {
					if (getOnSelectionChanged() != null) {
						Event.fireEvent(ExtTab.this, new Event(SELECTION_CHANGED_EVENT));
					}
				}

				@Override
				public Object getBean() {
					return ExtTab.this;
				}

				@Override
				public String getName() {
					return "selected";
				}
			};
		}
		return selected;
	}

	final void setTabPane(ExtTabPane value) {
		tabPanePropertyImpl().set(value);
	}

	/**
	 * <p>A reference to the TabPane that contains this tab instance.</p>
	 *
	 * @return the TabPane
	 */
	public final ExtTabPane getTabPane() {
		return tabPane == null ? null : tabPane.get();
	}

	/**
	 * The TabPane that contains this tab.
	 *
	 * @return the TabPane property
	 */
	public final ReadOnlyObjectProperty<ExtTabPane> tabPaneProperty() {
		return tabPanePropertyImpl().getReadOnlyProperty();
	}

	private ReadOnlyObjectWrapper<ExtTabPane> tabPanePropertyImpl() {
		if (tabPane == null) {
			tabPane = new ReadOnlyObjectWrapper<>(this, "tabPane") {

				private WeakReference<ExtTabPane> oldParent;

				@Override
				protected void invalidated() {
					if (oldParent != null && oldParent.get() != null) {
						oldParent.get().disabledProperty().removeListener(parentDisabledChangedListener);
					}

					updateDisabled();

					ExtTabPane newParent = get();

					if (newParent != null) {
						newParent.disabledProperty().addListener(parentDisabledChangedListener);
					}

					oldParent = new WeakReference<ExtTabPane>(newParent);

					super.invalidated();
				}
			};
		}
		return tabPane;
	}

	/**
	 * A list of String identifiers which can be used to logically group
	 * Nodes, specifically for an external style engine. This variable is
	 * analogous to the "class" attribute on an HTML element and, as such,
	 * each element of the list is a style class to which this Node belongs.
	 *
	 * @see <a href="http://www.w3.org/TR/css3-selectors/#class-html">CSS3 class selectors</a>
	 */
	@Override
	public ObservableList<String> getStyleClass() {
		return styleClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
		return tail.prepend(eventHandlerManager);
	}

	<E extends Event> void setEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
		eventHandlerManager.setEventHandler(eventType, eventHandler);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return "Tab"
	 */
	@Override
	public String getTypeSelector() {
		return "ExtTab";
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return {@code getTabPane()}
	 */
	@Override
	public Styleable getStyleableParent() {
		return getTabPane();
	}

	/**
	 * {@inheritDoc}
	 */
	public final ObservableSet<PseudoClass> getPseudoClassStates() {
		return FXCollections.emptyObservableSet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
		return getClassCssMetaData();
	}

	/**
	 * @return The CssMetaData associated with this class, which may include the
	 * CssMetaData of its superclasses.
	 */
	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return Collections.emptyList();
	}

	/*
	 * See Node#lookup(String)
	 */
	Node lookup(String selector) {
		if (selector == null) {
			return null;
		}

		Node n = null;

		if (getContent() != null) {
			n = getContent().lookup(selector);
		}
		if (n == null && getGraphic() != null) {
			n = getGraphic().lookup(selector);
		}
		return n;
	}

	/*
	 * See Node#lookupAll(String)
	 */
	List<Node> lookupAll(String selector) {
		final List<Node> results = new ArrayList<>();

		if (getContent() != null) {
			Set set = getContent().lookupAll(selector);
			if (!set.isEmpty()) {
				results.addAll(set);
			}
		}
		if (getGraphic() != null) {
			Set set = getGraphic().lookupAll(selector);
			if (!set.isEmpty()) {
				results.addAll(set);
			}
		}
		return results;
	}
}
