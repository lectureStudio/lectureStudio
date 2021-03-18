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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCombination;

import org.lecturestudio.javafx.util.AcceleratorHandler;
import org.lecturestudio.javafx.util.AcceleratorSupport;

public class ExtSplitMenuButton extends SplitMenuButton implements AcceleratorSupport, Toggle {

	private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

	/**
	 * The accelerator property enables accessing the associated action in one keystroke.
	 * It is a convenience offered to perform quickly a given action.
	 */
	private ObjectProperty<KeyCombination> accelerator;

	/**
	 * The {@link ToggleGroup} to which this {@code ToggleButton} belongs. A
	 * {@code ToggleButton} can only be in one group at any one time. If the
	 * group is changed, then the button is removed from the old group prior to
	 * being added to the new group.
	 */
	private ObjectProperty<ToggleGroup> toggleGroup;

	/**
	 * Indicates whether this toggle button is selected. This can be manipulated
	 * programmatically.
	 */
	private BooleanProperty selected;


	@Override
	public final void setAccelerator(KeyCombination value) {
		acceleratorProperty().set(value);
	}

	@Override
	public final KeyCombination getAccelerator() {
		return accelerator == null ? null : accelerator.get();
	}

	@Override
	public final ObjectProperty<KeyCombination> acceleratorProperty() {
		if (accelerator == null) {
			accelerator = new SimpleObjectProperty<>(this, "accelerator");

			AcceleratorHandler.set(this);
		}
		return accelerator;
	}

	/** {@inheritDoc} */
	@Override
	public void fire() {
		if (!isDisabled()) {
			// Don't toggle from selected to not selected if part of a group.
			if (getToggleGroup() == null || !isSelected()) {
				setSelected(!isSelected());
				super.fire();
			}
		}
	}

	@Override
	public ToggleGroup getToggleGroup() {
		return toggleGroup == null ? null : toggleGroup.get();
	}

	@Override
	public void setToggleGroup(ToggleGroup toggleGroup) {
		toggleGroupProperty().set(toggleGroup);
	}

	@Override
	public ObjectProperty<ToggleGroup> toggleGroupProperty() {
		if (toggleGroup == null) {
			toggleGroup = new ObjectPropertyBase<>() {

				private ToggleGroup old;

				@Override
				protected void invalidated() {
					final ToggleGroup tg = get();
					if (tg != null && !tg.getToggles().contains(ExtSplitMenuButton.this)) {
						if (old != null) {
							old.getToggles().remove(ExtSplitMenuButton.this);
						}
						tg.getToggles().add(ExtSplitMenuButton.this);
					}
					else if (tg == null) {
						old.getToggles().remove(ExtSplitMenuButton.this);
					}
					old = tg;
				}

				@Override
				public Object getBean() {
					return ExtSplitMenuButton.this;
				}

				@Override
				public String getName() {
					return "toggleGroup";
				}
			};
		}
		return toggleGroup;
	}

	@Override
	public boolean isSelected() {
		return selected != null && selected.get();
	}

	@Override
	public void setSelected(boolean selected) {
		selectedProperty().set(selected);
	}

	@Override
	public BooleanProperty selectedProperty() {
		if (selected == null) {
			selected = new BooleanPropertyBase() {

				@Override
				protected void invalidated() {
					final boolean selected = get();
					final ToggleGroup group = getToggleGroup();

					pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, selected);
					notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);

					if (group != null) {
						if (selected) {
							group.selectToggle(ExtSplitMenuButton.this);
						}
						else if (group.getSelectedToggle() == ExtSplitMenuButton.this) {
							if (!group.getSelectedToggle().isSelected()) {
								for (Toggle toggle : group.getToggles()) {
									if (toggle.isSelected()) {
										return;
									}
								}
							}
							group.selectToggle(null);
						}
					}
				}

				@Override
				public Object getBean() {
					return ExtSplitMenuButton.this;
				}

				@Override
				public String getName() {
					return "selected";
				}
			};
		}
		return selected;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/split-toggle-button.css").toExternalForm();
	}
}
