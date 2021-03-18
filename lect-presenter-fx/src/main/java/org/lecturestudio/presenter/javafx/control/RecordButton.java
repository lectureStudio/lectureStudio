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
import javafx.scene.control.Skin;

import org.lecturestudio.javafx.control.ExtButton;

public class RecordButton extends ExtButton {

	public static final PseudoClass BLINK_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("blink");

	public static final PseudoClass RECORDING_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("recording");

	private static final String DEFAULT_STYLE_CLASS = "record-button";

	private final ReadOnlyBooleanWrapper blink = new ReadOnlyBooleanWrapper() {

		@Override
		protected void invalidated() {
			pseudoClassStateChanged(BLINK_PSEUDOCLASS_STATE, get());
		}

		@Override
		public Object getBean() {
			return RecordButton.this;
		}

		@Override
		public String getName() {
			return BLINK_PSEUDOCLASS_STATE.getPseudoClassName();
		}
	};

	private final ReadOnlyBooleanWrapper recording = new ReadOnlyBooleanWrapper() {

		@Override
		protected void invalidated() {
			pseudoClassStateChanged(RECORDING_PSEUDOCLASS_STATE, get());
		}

		@Override
		public Object getBean() {
			return RecordButton.this;
		}

		@Override
		public String getName() {
			return RECORDING_PSEUDOCLASS_STATE.getPseudoClassName();
		}
	};


	public RecordButton() {
		super();

		initialize();
	}

	/**
	 * Indicates that the button is blinking.
	 */
	public final ReadOnlyBooleanProperty blinkProperty() {
		return blink.getReadOnlyProperty();
	}

	public void setBlink(boolean value) {
		blink.set(value);
	}

	public final boolean isBlinking() {
		return blinkProperty().get();
	}

	/**
	 * Indicates that the button has been set to the recording state.
	 */
	public final ReadOnlyBooleanProperty recordingProperty() {
		return recording.getReadOnlyProperty();
	}

	public void setRecording(boolean value) {
		recording.set(value);
	}

	public final boolean isRecording() {
		return recordingProperty().get();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new RecordButtonSkin(this);
	}

	private void initialize() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}

}
