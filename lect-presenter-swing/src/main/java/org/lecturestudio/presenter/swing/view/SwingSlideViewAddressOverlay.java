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

package org.lecturestudio.presenter.swing.view;

import javax.swing.JLabel;

import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.view.SlideViewAddressOverlay;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.converter.FontConverter;

public class SwingSlideViewAddressOverlay extends JLabel implements SlideViewAddressOverlay {

	private Position position;


	@Override
	public void setAddress(String address) {
		setText(address);
	}

	@Override
	public void setTextColor(Color color) {
		setForeground(ColorConverter.INSTANCE.to(color));
	}

	@Override
	public void setBackgroundColor(Color color) {
		setBackground(ColorConverter.INSTANCE.to(color));
	}

	@Override
	public void setFont(Font font) {
		setFont(FontConverter.INSTANCE.to(font));
	}

	@Override
	public void setFontSize(double size) {
		setFont(getFont().deriveFont((float) size));
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void setPosition(Position position) {
		this.position = position;
	}
}
