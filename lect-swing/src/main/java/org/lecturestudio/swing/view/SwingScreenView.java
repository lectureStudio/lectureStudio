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

package org.lecturestudio.swing.view;

import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.media.video.VideoFrame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.lecturestudio.swing.util.VideoFrameConverter;
import org.lecturestudio.web.api.stream.ScreenViewComponent;

public class SwingScreenView extends JComponent implements ScreenViewComponent {

	private BufferedImage tempImage;

	private BufferedImage image;


	public SwingScreenView() {
		super();
	}

	@Override
	public void setVideoFrame(VideoFrame frame) {
		if (!isVisible() || !isDisplayable()) {
			return;
		}

		try {
			tempImage = VideoFrameConverter.convertVideoFrame(frame, tempImage);
			image = VideoFrameConverter.convertVideoFrameToComponentSize(image, tempImage, this);
		}
		catch (Exception e) {
			return;
		}

		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());

		if (nonNull(image)) {
			paintImage(g2);
		}
	}

	private void paintImage(Graphics2D g2) {
		AffineTransform transform = g2.getTransform();
		AffineTransform imageTransform = new AffineTransform();
		imageTransform.translate(transform.getTranslateX(), transform.getTranslateY());

		double x = (getWidth() * transform.getScaleX() - image.getWidth(null)) / 2;
		double y = (getHeight() * transform.getScaleX() - image.getHeight(null)) / 2;

		g2.setTransform(imageTransform);
		g2.drawImage(image, (int) x, (int) y, null);
		g2.setTransform(transform);
	}
}
