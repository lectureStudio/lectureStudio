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

package org.lecturestudio.swing.app;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class LectSwingPreloader extends SwingPreloader {

	@Override
	protected JComponent createView() throws Exception {
		final BufferedImage image = readImage("resources/gfx/splash.png");
		final int width = image.getWidth();
		final int height = image.getHeight();

		JPanel panel = new JPanel() {

			final Font versionFont = new Font("Dialog", Font.BOLD, 12);


			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;

				StringBuilder builder = new StringBuilder();
				builder.append(getVersion());

				FontMetrics fontMetrics = g2.getFontMetrics(versionFont);
				int width = fontMetrics.stringWidth(builder.toString());

				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.drawImage(image, 0, 0, null);
				g2.setFont(versionFont);
				g2.setColor(new Color(250, 250, 250));
				g2.drawString(builder.toString(), image.getWidth() - 40 - width, 195);
			}
		};

		panel.setSize(width, height);
		panel.setPreferredSize(new Dimension(width, height));

		return panel;
	}

	private static String getVersion() {
		String version = null;

		try {
			Manifest manifest = new Manifest(SwingPreloader.class.getClassLoader().getResourceAsStream(JarFile.MANIFEST_NAME));
			Attributes attr = manifest.getMainAttributes();
			version = attr.getValue("Package-Version");
		}
		catch (Exception e) {
			// Ignore
		}

		if (isNull(version)) {
			// Set default version.
			version = "1.0";
		}

		return version;
	}

	/**
	 * Reads an image which can be located on the file system or in the
	 * jar-file.
	 *
	 * @param path the path where the image is located.
	 *
	 * @return the loaded image.
	 */
	private static BufferedImage readImage(String path) throws Exception {
		BufferedImage image;
		URL url = ClassLoader.getSystemResource(path);

		if (nonNull(url)) {
			image = ImageIO.read(url);
		}
		else {
			// Give a second chance to load the image.
			InputStream inputStream = new FileInputStream(path);
			image = ImageIO.read(inputStream);
			inputStream.close();
		}

		return image;
	}
}
