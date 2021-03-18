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

import java.util.Arrays;
import java.util.Collection;

import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

@DefaultProperty("content")
public class ContentPane extends VBox {

	private final StringProperty title = new SimpleStringProperty(this, "title");

	private final ObservableList<Node> content = FXCollections.observableArrayList();


	/**
	 * Creates a ContentPane with no title.
	 */
	public ContentPane() {
		this(null);
	}

	/**
	 * Creates a ContentPane with a text title.
	 *
	 * @param title The title of the tab.
	 */
	public ContentPane(String title) {
		this(title, (Node) null);
	}

	/**
	 * Creates a ContentPane with a text title and the specified content node.
	 *
	 * @param title The title of the pane.
	 * @param content The content of the pane.
	 */
	public ContentPane(String title, Node... content) {
		super();

		setTitle(title);
		setContent(Arrays.asList(content));

		initialize();
	}

	public String getTitle() {
		return title.get();
	}

	public void setTitle(String value) {
		title.set(value);
	}

	public StringProperty titleProperty() {
		return title;
	}

	/**
	 * The content to show within the main content area. The content
	 * can be any Node such as UI controls or groups of nodes added
	 * to a layout container.
	 *
	 * @param value the content node
	 */
	public final void setContent(Collection<Node> value) {
		content.setAll(value);
	}

	/**
	 * The content associated with the pane.
	 *
	 * @return The content associated with the pane.
	 */
	public final ObservableList<Node> getContent() {
		return content;
	}

	/**
	 * The content associated with the pane.
	 *
	 * @return the content property.
	 */
	public final ObservableList<Node> contentProperty() {
		return content;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/content-pane.css").toExternalForm();
	}

	private void initialize() {
		getStyleClass().add("content-pane");

		Label titleLabel = new Label(getTitle());
		titleLabel.getStyleClass().add("title");
		titleLabel.textProperty().bind(titleProperty());

		getChildren().add(titleLabel);

		contentProperty().addListener((ListChangeListener<? super Node>) change -> {
			while (change.next()) {
				for (Node node : change.getRemoved()) {
					getChildren().remove(node);
				}
				for (Node node : change.getAddedSubList()) {
					getChildren().add(node);
				}
			}

			for (var node : lookupAll(".scroll-pane")) {
				ScrollPane pane = (ScrollPane) node;
				pane.skinProperty().addListener(new InvalidationListener() {

					@Override
					public void invalidated(Observable observable) {
						pane.skinProperty().removeListener(this);

						var viewport = pane.lookup(".viewport");
						viewport.setCache(false);
					}
				});
			}
		});
	}

}
