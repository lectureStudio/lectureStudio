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

import static java.util.Objects.isNull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.SwingConstants;

public class ColorChooserButton extends JButton {

	private Color current;


	public ColorChooserButton() {
		this(Color.black);
	}

	public ColorChooserButton(Color color) {
		setIconTextGap(0);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setHorizontalAlignment(SwingConstants.CENTER);
		setSelectedColor(color);

		addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(null, null, current);
			setSelectedColor(newColor);
		});
	}

	public Color getSelectedColor() {
		return current;
	}

	public void setSelectedColor(Color newColor) {
		if (isNull(newColor) || newColor.equals(current)) {
			return;
		}

		current = newColor;

		setIcon(createIcon(current, 16, 16));
		repaint();

		ItemEvent event = new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
				newColor, ItemEvent.SELECTED);

		for (ItemListener listener : getItemListeners()) {
			listener.itemStateChanged(event);
		}
	}

	public static ImageIcon createIcon(Color main, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(main);
		graphics.fillRect(0, 0, width, height);
		graphics.setXORMode(Color.DARK_GRAY);
		graphics.drawRect(0, 0, width - 1, height - 1);
		image.flush();

		return new ImageIcon(image);
	}
}
