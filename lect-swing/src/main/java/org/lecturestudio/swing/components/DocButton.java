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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Objects;

import javax.swing.JButton;

import com.formdev.flatlaf.util.UIScale;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.util.FileUtils;

public class DocButton extends JButton {

	private final RecentDocument recentDocument;


	public DocButton(RecentDocument doc) {
		super();

		recentDocument = Objects.requireNonNull(doc);
	}

	public RecentDocument getRecentDocument() {
		return recentDocument;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		float uiScale = UIScale.getUserScaleFactor();

		Font font = getFont();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g2d.setPaint(Color.BLACK);
		g2d.setFont(font.deriveFont(Font.BOLD));
		g2d.drawString(recentDocument.getDocumentName(), 15 * uiScale, 20 * uiScale);
		g2d.setFont(font.deriveFont(Font.PLAIN));
		g2d.drawString(FileUtils.shortenPath(recentDocument.getDocumentPath(), 35), 15 * uiScale, 50 * uiScale);
	}
}
