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

package org.lecturestudio.presenter.swing.combobox;

import static java.util.Objects.nonNull;

import java.awt.*;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.*;

import org.lecturestudio.presenter.api.model.ParticipantVideoLayout;

public class ParticipantVideoLayoutRenderer extends DefaultListCellRenderer {

	private final ResourceBundle resources;

	private final String prefix;


	@Inject
	public ParticipantVideoLayoutRenderer(ResourceBundle resources, String prefix) {
		this.resources = resources;
		this.prefix = prefix;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		ParticipantVideoLayout layout = (ParticipantVideoLayout) value;

		if (nonNull(layout)) {
			setText(resources.getString(prefix + layout.name().toLowerCase()));
		}
		else {
			setText("< layout translation not found >");
		}

		return this;
	}
}
