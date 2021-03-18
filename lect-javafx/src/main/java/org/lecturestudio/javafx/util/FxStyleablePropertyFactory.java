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

package org.lecturestudio.javafx.util;

import java.util.List;
import java.util.function.Function;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.css.StyleableStringProperty;
import javafx.scene.paint.Paint;

public class FxStyleablePropertyFactory<S extends Styleable> {

	private final StyleablePropertyFactory<S> factory;


	/**
	 * The constructor is passed the CssMetaData of the parent class of <S>, typically by calling the
	 * static <code>getClassCssMetaData()</code> method of the parent.
	 *
	 * @param parentCssMetaData The CssMetaData of the parent class of <S>, or null.
	 */
	public FxStyleablePropertyFactory(List<CssMetaData<? extends Styleable, ?>> parentCssMetaData) {
		factory = new StyleablePropertyFactory<>(parentCssMetaData);
	}

	/**
	 * Get the CssMetaData for the given Styleable. For a Node other than a Control, this method should be
	 * called from the {@link javafx.css.Styleable#getCssMetaData()} method. For a Control, this method should be called
	 * from the {@link javafx.scene.control.Control#getControlCssMetaData()} method.
	 *
	 * @return the CssMetaData for the given Styleable
	 */
	public final List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
		return factory.getCssMetaData();
	}

	/**
	 * Create a StyleableProperty<Boolean> with initial value. The inherit flag defaults to false.
	 *
	 * @param styleable    The <code>this</code> reference of the returned property. This is also the property bean.
	 * @param propertyName The field name of the StyleableProperty<Boolean>
	 * @param cssProperty  The CSS property name
	 * @param function     A function that returns the StyleableProperty<Boolean> that was created by this method call.
	 * @param initialValue The initial value of the property. CSS may reset the property to this value.
	 *
	 * @return a StyleableProperty created with initial value
	 */
	public final StyleableBooleanProperty createBooleanProperty(S styleable, String propertyName, String cssProperty,
			Function<S, StyleableProperty<Boolean>> function, boolean initialValue) {
		return (StyleableBooleanProperty) factory
				.createStyleableBooleanProperty(styleable, propertyName, cssProperty, function, initialValue, false);
	}

	/**
	 * Create a StyleableProperty<Number> with initial value. The inherit flag defaults to false.
	 *
	 * @param styleable    The <code>this</code> reference of the returned property. This is also the property bean.
	 * @param propertyName The field name of the StyleableProperty<Number>
	 * @param cssProperty  The CSS property name
	 * @param function     A function that returns the StyleableProperty<Number> that was created by this method call.
	 * @param initialValue The initial value of the property. CSS may reset the property to this value.
	 *
	 * @return a StyleableProperty created with initial value and false inherit flag
	 */
	public final StyleableObjectProperty<Number> createNumberProperty(S styleable, String propertyName, String cssProperty,
			Function<S, StyleableProperty<Number>> function, Number initialValue) {
		return (StyleableObjectProperty<Number>) factory
				.createStyleableNumberProperty(styleable, propertyName, cssProperty, function, initialValue, false);
	}

	/**
	 * Create a StyleableProperty<Paint> with initial value. The inherit flag defaults to false.
	 *
	 * @param styleable    The <code>this</code> reference of the returned property. This is also the property bean.
	 * @param propertyName The field name of the StyleableProperty<Paint>
	 * @param cssProperty  The CSS property name
	 * @param function     A function that returns the StyleableProperty<Paint> that was created by this method call.
	 * @param initialValue The initial value of the property. CSS may reset the property to this value.
	 *
	 * @return a StyleableProperty created with initial value and false inherit flag
	 */
	public final StyleableObjectProperty<Paint> createPaintProperty(S styleable, String propertyName, String cssProperty,
			Function<S, StyleableProperty<Paint>> function, Paint initialValue) {
		return (StyleableObjectProperty<Paint>) factory
				.createStyleablePaintProperty(styleable, propertyName, cssProperty, function, initialValue, false);
	}

	/**
	 * Create a StyleableProperty<String> with initial value and inherit flag.
	 *
	 * @param styleable    The <code>this</code> reference of the returned property. This is also the property bean.
	 * @param propertyName The field name of the StyleableProperty<String>
	 * @param cssProperty  The CSS property name
	 * @param function     A function that returns the StyleableProperty<String> that was created by this method call.
	 * @param initialValue The initial value of the property. CSS may reset the property to this value.
	 *
	 * @return a StyleableProperty created with initial value and inherit flag
	 */
	public final StyleableStringProperty createStringProperty(S styleable, String propertyName, String cssProperty,
			Function<S, StyleableProperty<String>> function, String initialValue) {
		return (StyleableStringProperty) factory
				.createStyleableStringProperty(styleable, propertyName, cssProperty, function, initialValue, false);
	}
}
