/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.media.video.VideoFrame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.*;

import org.lecturestudio.swing.util.VideoFrameConverter;

public class VideoFrameView extends JPanel {

	private BufferedImage image;


	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		if (nonNull(image)) {
			AffineTransform transform = g2.getTransform();
			AffineTransform imageTransform = new AffineTransform();
			imageTransform.translate(transform.getTranslateX(), transform.getTranslateY());

			int x = (int) ((getWidth() * transform.getScaleX() - image.getWidth(null)) / 2);
			int y = (int) ((getHeight() * transform.getScaleX() - image.getHeight(null)) / 2);

			g2.setTransform(imageTransform);
			g2.drawImage(image, x, y, null);
			g2.setTransform(transform);
		}
	}

	public void paintVideoFrame(VideoFrame videoFrame) throws Exception {
		try {
			image = VideoFrameConverter.convertVideoFrameToComponentSize(videoFrame, image, this);
		}
		catch (Exception e) {
			return;
		}

		repaint();
	}
}
