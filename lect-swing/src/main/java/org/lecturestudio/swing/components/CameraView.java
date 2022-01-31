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
import static java.util.Objects.nonNull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

/**
 * The CameraView displays captured images by a camera device.
 *
 * @author Alex Andres
 */
public class CameraView extends JComponent {

	private static final long serialVersionUID = 1160992899312965308L;

	private Image image;

	private String statusMessage;


	/**
	 * Create a new CameraView.
	 */
	public CameraView() {
		setBorder(new EmptyBorder(0, 0, 0, 0));
	}

	/**
	 * Display the provided image.
	 *
	 * @param image The image to display.
	 */
	public void showImage(Image image) {
		if (isNull(image)) {
			return;
		}

		if (nonNull(this.image)) {
			this.image.flush();
			this.image = null;
		}

		this.image = image;

		repaint();
	}

	/**
	 * Clear the image.
	 */
	public void clearImage() {
		this.image = null;

		repaint();
	}

	/**
	 * Set the camera status message.
	 *
	 * @param message The camera status message.
	 */
	public void setStatusMessage(String message) {
		this.statusMessage = message;

		paintImmediately(new Rectangle(getWidth(), getHeight()));
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		if (nonNull(image)) {
			AffineTransform transform = g2.getTransform();
			AffineTransform imageTransform = new AffineTransform();
			imageTransform.translate(transform.getTranslateX(), transform.getTranslateY());

			g2.setTransform(imageTransform);
			g2.drawImage(image, 0, 0, null);
			g2.setTransform(transform);
		}
		else {
			paintNoImage(g2);
		}
	}

	private void paintNoImage(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setBackground(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());

		int cx = (getWidth() - 70) / 2;
		int cy = (getHeight() - 40) / 2;

		g2.setStroke(new BasicStroke(2));
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRoundRect(cx, cy, 70, 40, 10, 10);
		g2.setColor(Color.WHITE);
		g2.fillOval(cx + 5, cy + 5, 30, 30);
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillOval(cx + 10, cy + 10, 20, 20);
		g2.setColor(Color.WHITE);
		g2.fillOval(cx + 12, cy + 12, 16, 16);
		g2.fillRoundRect(cx + 50, cy + 5, 15, 10, 5, 5);
		g2.fillRect(cx + 63, cy + 25, 7, 2);
		g2.fillRect(cx + 63, cy + 28, 7, 2);
		g2.fillRect(cx + 63, cy + 31, 7, 2);

		g2.setColor(Color.GRAY);
		g2.setStroke(new BasicStroke(2));
		g2.drawLine(cx + 10, cy + 10, cx + 30, cy + 30);
		g2.drawLine(cx + 10, cy + 30, cx + 30, cy + 10);

		if (isNull(statusMessage)) {
			return;
		}

		FontMetrics metrics = g2.getFontMetrics(getFont());
		int w = metrics.stringWidth(statusMessage);
		int h = metrics.getHeight();

		g2.setColor(Color.WHITE);
		g2.drawString(statusMessage, (getWidth() - w) / 2, cy - h);
	}

}
