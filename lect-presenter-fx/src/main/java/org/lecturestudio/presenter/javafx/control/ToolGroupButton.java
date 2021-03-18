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

import static java.util.Objects.nonNull;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;

import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.javafx.control.ExtToggleButton;

public class ToolGroupButton extends ExtToggleButton {

	private static final String DEFAULT_STYLE_CLASS = "tool-group-button";

	private static final PseudoClass SELECT_GROUP_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("select-group");

	private static final PseudoClass COPY_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("copy");

	private final ReadOnlyBooleanWrapper selectGroup = new ReadOnlyBooleanWrapper() {

		@Override
		protected void invalidated() {
			pseudoClassStateChanged(SELECT_GROUP_PSEUDOCLASS_STATE, get());
		}

		@Override
		public Object getBean() {
			return ToolGroupButton.this;
		}

		@Override
		public String getName() {
			return SELECT_GROUP_PSEUDOCLASS_STATE.getPseudoClassName();
		}
	};

	private final ReadOnlyBooleanWrapper copy = new ReadOnlyBooleanWrapper() {

		@Override
		protected void invalidated() {
			pseudoClassStateChanged(COPY_PSEUDOCLASS_STATE, get());
		}

		@Override
		public Object getBean() {
			return ToolGroupButton.this;
		}

		@Override
		public String getName() {
			return COPY_PSEUDOCLASS_STATE.getPseudoClassName();
		}
	};


	public ToolGroupButton() {
		super();

		initialize();
	}

	/** {@inheritDoc} */
	@Override
	public void fire() {
		boolean changeState = isSelected() && !isDisabled();

		super.fire();

		if (changeState) {
			if (nonNull(getToggleGroup()) && isSelected()) {
				fireEvent(new ActionEvent());
			}
		}
	}

	public void selectToolType(ToolType type) {
		if (type == ToolType.SELECT) {
			setSelectGroup(false);
			setCopy(false);
		}
		else if (type == ToolType.SELECT_GROUP) {
			setSelectGroup(true);
			setCopy(false);
		}
		else if (type == ToolType.CLONE) {
			setSelectGroup(false);
			setCopy(true);
		}
	}

	/**
	 * Indicates that the button has been set to the 'select group' state.
	 */
	public final ReadOnlyBooleanProperty selectGroupProperty() {
		return selectGroup.getReadOnlyProperty();
	}

	public void setSelectGroup(boolean value) {
		selectGroup.set(value);
	}

	public final boolean isSelectGroup() {
		return selectGroupProperty().get();
	}

	/**
	 * Indicates that the button has been set to the 'copy' state.
	 */
	public final ReadOnlyBooleanProperty copyProperty() {
		return copy.getReadOnlyProperty();
	}

	public void setCopy(boolean value) {
		copy.set(value);
	}

	public final boolean isCopy() {
		return copyProperty().get();
	}

	private void initialize() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}

}
