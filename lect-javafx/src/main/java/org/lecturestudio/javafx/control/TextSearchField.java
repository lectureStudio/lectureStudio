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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class TextSearchField<T> extends ClearableTextField {

	private static final String DEFAULT_STYLE_CLASS = "text-search-field";

	private static <T> StringConverter<T> defaultStringConverter() {
		return new StringConverter<>() {

			@Override
			public String toString(T t) {
				return t == null ? null : t.toString();
			}

			@Override
			public T fromString(String string) {
				return (T) string;
			}
		};
	}

	private final ObservableList<T> suggestions = FXCollections.observableArrayList();

	private final ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory");

	private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<>(this, "converter", defaultStringConverter());


	public TextSearchField() {
		this("");
	}

	public TextSearchField(String text) {
		super(text);

		initialize();
	}

	public final ObservableList<T> getSuggestions() {
		return suggestions;
	}

	public final void setCellFactory(Callback<ListView<T>, ListCell<T>> value) {
		cellFactory.set(value);
	}

	public final Callback<ListView<T>, ListCell<T>> getCellFactory() {
		return cellFactory.get();
	}

	public ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactoryProperty() {
		return cellFactory;
	}

	public final void setConverter(StringConverter<T> value) {
		converter.set(value);
	}

	public final StringConverter<T> getConverter() {
		return converter.get();
	}

	public ObjectProperty<StringConverter<T>> converterProperty() {
		return converter;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/text-search-field.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new TextSearchFieldSkin<>(this);
	}

	private void initialize() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}
}
