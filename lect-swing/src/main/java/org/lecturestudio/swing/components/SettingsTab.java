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

package org.lecturestudio.swing.components;

import org.lecturestudio.swing.model.AdaptiveTabType;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;

public class SettingsTab {

	private String text;

	private String name;

	private Icon icon;

	private Component content;

	private Dimension size;

	private AdaptiveTabType defaultTabType = AdaptiveTabType.NORMAL;


	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Component getContent() {
		return content;
	}

	public void setContent(Component content) {
		this.content = content;
	}

	public Dimension getSize() {
		return size;
	}

	public void setSize(Dimension size) {
		this.size = size;
	}

	public void setDefaultTabType(AdaptiveTabType defaultTabType) {
		this.defaultTabType = defaultTabType;
	}

	public AdaptiveTabType getDefaultTabType() {
		return defaultTabType;
	}
}
