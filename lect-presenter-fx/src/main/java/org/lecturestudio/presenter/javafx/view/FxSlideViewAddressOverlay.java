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

package org.lecturestudio.presenter.javafx.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.view.SlideViewAddressOverlay;
import org.lecturestudio.javafx.beans.converter.ColorConverter;
import org.lecturestudio.javafx.beans.converter.FontConverter;

public class FxSlideViewAddressOverlay extends Label implements SlideViewAddressOverlay {

	private Position position;


	@Override
	public void setAddress(String address) {
		setText(address);
	}

	@Override
	public void setTextColor(Color color) {
		setTextFill(ColorConverter.INSTANCE.to(color));
	}

	@Override
	public void setBackgroundColor(Color color) {
		BackgroundFill backgroundFill = new BackgroundFill(ColorConverter.INSTANCE.to(color), CornerRadii.EMPTY, Insets.EMPTY);

		setBackground(new Background(backgroundFill));
	}

	@Override
	public void setFont(Font font) {
		setFont(FontConverter.INSTANCE.to(font));
	}

	@Override
	public void setFontSize(double size) {
		setFont(javafx.scene.text.Font.font(getFont().getFamily(), size));
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
