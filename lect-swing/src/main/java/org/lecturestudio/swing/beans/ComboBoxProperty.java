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

package org.lecturestudio.swing.beans;

import static java.util.Objects.isNull;

import java.awt.event.ItemEvent;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.lecturestudio.core.beans.ObjectProperty;

public class ComboBoxProperty<T> extends ObjectProperty<T> {

	private final JComboBox<T> comboBox;

	private boolean valid = true;


	public ComboBoxProperty(JComboBox<T> comboBox) {
		this.comboBox = comboBox;
		this.comboBox.addItemListener(e -> {
			int stateChange = e.getStateChange();

			if (stateChange == ItemEvent.SELECTED) {
				super.set(comboBox.getModel().getElementAt(comboBox.getSelectedIndex()));
			}
		});
		this.comboBox.addPropertyChangeListener("model", evt -> {
			ComboBoxModel<T> model = (ComboBoxModel<T>) evt.getNewValue();
			model.setSelectedItem(super.get());
		});
	}

	@Override
	public T get() {
		return comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
	}

	@Override
	public void set(T value) {
		final T current = get();

		if (value == current) {
			return;
		}
		if (isNull(current) || !current.equals(value)) {
			if (valid) {
				valid = false;
				comboBox.setSelectedItem(value);
				super.set(value);
				valid = true;
			}
		}
	}
}
