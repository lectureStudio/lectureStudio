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

package org.lecturestudio.presenter.javafx.control;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.css.PseudoClass;

import org.lecturestudio.javafx.control.ExtButton;
import org.lecturestudio.javafx.control.SvgIcon;

public class StartButton extends ExtButton {

	private static final String DEFAULT_STYLE_CLASS = "start-button";

	private static final PseudoClass STARTED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("started");

	private final ReadOnlyBooleanWrapper started = new ReadOnlyBooleanWrapper() {

		@Override
		protected void invalidated() {
			pseudoClassStateChanged(STARTED_PSEUDOCLASS_STATE, get());
		}

		@Override
		public Object getBean() {
			return StartButton.this;
		}

		@Override
		public String getName() {
			return STARTED_PSEUDOCLASS_STATE.getPseudoClassName();
		}
	};


	public StartButton() {
		super();

		initialize();
	}

	/**
	 * Indicates that the button has been set to the recording state.
	 */
	public final ReadOnlyBooleanProperty startedProperty() {
		return started.getReadOnlyProperty();
	}

	public void setStarted(boolean value) {
		started.set(value);
	}

	public final boolean isStarted() {
		return startedProperty().get();
	}

	/** {@inheritDoc} */
	@Override
	public void fire() {
		setStarted(!isStarted());

		super.fire();
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/start-button.css").toExternalForm();
	}

	private void initialize() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);

		setGraphic(new SvgIcon());
	}

}
