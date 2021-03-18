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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.StringConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import org.lecturestudio.javafx.util.CssHelper;

public class MessageView extends Control {

	private static final String DEFAULT_STYLE_CLASS = "message-view";

	private final ObjectProperty<Date> date = new SimpleObjectProperty<>();

	private final StringProperty host = new SimpleStringProperty();

	private final StringProperty message = new SimpleStringProperty();

	private StyleableObjectProperty<String> dateFormat;


	public MessageView() {
		initialize();
	}

	/**
	 * @return the date
	 */
	public ObjectProperty<Date> dateProperty() {
		return date;
	}

	public Date getDate() {
		return dateProperty().get();
	}

	public void setDate(Date date) {
		dateProperty().set(date);
	}

	/**
	 * @return the host
	 */
	public StringProperty hostProperty() {
		return host;
	}

	public String getHost() {
		return hostProperty().get();
	}

	public void setHost(String host) {
		hostProperty().set(host);
	}

	/**
	 * @return the message
	 */
	public StringProperty messageProperty() {
		return message;
	}

	public String getMessage() {
		return messageProperty().get();
	}

	public void setMessage(String message) {
		messageProperty().set(message);
	}

	public String getDateFormat() {
		return dateFormat == null ? StyleableProperties.DATE_FORMAT.getInitialValue(MessageView.this) : dateFormat.get();
	}

	public void setDateFormat(String format) {
		dateFormatProperty().set(format);
	}

	public StyleableObjectProperty<String> dateFormatProperty() {
		if (isNull(dateFormat)) {
			dateFormat = CssHelper.createProperty(StyleableProperties.DATE_FORMAT, MessageView.this, "dateFormat");
		}
		return dateFormat;
	}

	@Override
	public String getUserAgentStylesheet() {
		return MessageView.class.getResource("/resources/css/message-view.css").toExternalForm();
	}

	@Override
	public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return getClassCssMetaData();
	}

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.STYLEABLES;
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new MessageViewSkin(this);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
	}


	private static class StyleableProperties {

		private static final CssMetaData<MessageView, String> DATE_FORMAT = new CssMetaData<MessageView, String>("-fx-date-format",
				StringConverter.getInstance(), "H:mm") {

			@Override
			public boolean isSettable(MessageView control) {
				return control.dateFormat == null || !control.dateFormat.isBound();
			}

			@Override
			public StyleableProperty<String> getStyleableProperty(MessageView control) {
				return control.dateFormatProperty();
			}
		};

		private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

		static {
			final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<CssMetaData<? extends Styleable, ?>>(Control.getClassCssMetaData());

			Collections.addAll(styleables, DATE_FORMAT);

			STYLEABLES = Collections.unmodifiableList(styleables);
		}
	}

}
