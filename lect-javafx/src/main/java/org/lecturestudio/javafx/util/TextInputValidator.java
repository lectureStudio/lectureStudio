/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import com.sun.javafx.scene.control.LambdaMultiplePropertyChangeListenerHandler;

import java.util.function.Predicate;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.css.PseudoClass;
import javafx.scene.control.TextInputControl;

public abstract class TextInputValidator {

	protected static final PseudoClass ERROR_CLASS = PseudoClass.getPseudoClass("error");

	private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(true);

	private LambdaMultiplePropertyChangeListenerHandler listenerHandler;


	public ReadOnlyBooleanProperty validProperty() {
		return valid.getReadOnlyProperty();
	}

	public void bind(TextInputControl control) {
		listenerHandler.unregisterChangeListeners(control.textProperty());
	}

	protected void bind(TextInputControl control, Predicate<String> predicate) {
		if (listenerHandler == null) {
			listenerHandler = new LambdaMultiplePropertyChangeListenerHandler();
		}

		listenerHandler.registerInvalidationListener(control.textProperty(), text -> {
			boolean isValid = predicate.test(control.getText());

			control.pseudoClassStateChanged(ERROR_CLASS, !isValid);

			valid.set(isValid);
		});
	}
}
