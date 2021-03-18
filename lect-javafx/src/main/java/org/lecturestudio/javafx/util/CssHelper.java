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

import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;

/**
 * Helper class to create {@code StyleableProperty} and {@code CssMetaData} instances.
 * 
 * @author Alex Andres
 */
public class CssHelper {

	/**
	 * Creates a StyleableObjectProperty instance with the given parameters.
	 * 
	 * @param metaData The CssMetaData instance.
	 * @param styleable The Styleable (control) that is associated with the returned property.
	 * @param propertyName The name of the property field in the Styleable (control) class.
	 * 
	 * @param <S> Type of the Styleable control.
	 * @param <V> Type of the property.
	 * 
	 * @return a new instance of a StyleableProperty.
	 *
	 * @see SimpleStyleableObjectProperty
	 */
	public static <S extends Styleable, V> StyleableObjectProperty<V> createProperty(CssMetaData<S, V> metaData, S styleable, String propertyName) {
		return new SimpleStyleableObjectProperty<V>(metaData, styleable, propertyName, metaData.getInitialValue(styleable));
	}

}
