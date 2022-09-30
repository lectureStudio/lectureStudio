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

import java.awt.Component;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.lecturestudio.presenter.api.net.ScreenShareProfile;

public class ScreenShareProfileRenderer extends DefaultListCellRenderer {

	private final ResourceBundle resources;

	private final String prefix;


	@Inject
	public ScreenShareProfileRenderer(ResourceBundle resources, String prefix) {
		this.resources = resources;
		this.prefix = prefix;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

		ScreenShareProfile profile = (ScreenShareProfile) value;

		if (nonNull(profile)) {
			setText(resources.getString(prefix + profile.getType().name().toLowerCase()));
		}
		else {
			setText("");
		}

		return this;
	}
}
