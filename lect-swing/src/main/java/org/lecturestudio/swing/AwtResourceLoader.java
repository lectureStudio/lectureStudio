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

package org.lecturestudio.swing;

import static java.util.Objects.nonNull;

import com.formdev.flatlaf.util.ScaledImageIcon;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.lecturestudio.core.io.ResourceLoader;

/**
 * A convenience class for loading resources such as images.
 * 
 * @author Alex Andres
 */
public abstract class AwtResourceLoader extends ResourceLoader {

	/**
	 * Default resource path where images can be found.
	 */
	private static final String RESOURCE_PATH = "resources/";


	/**
	 * Returns an {@code Icon} that is loaded from the specified path.
	 * 
	 * @param path the relative path where the image is located.
	 * 
	 * @return the loaded image as icon.
	 */
	public static Icon getIcon(String path) {
		return getIcon(path, null);
	}

	/**
	 * Returns an {@code Icon} that is loaded from the specified path.
	 *
	 * @param path the relative path where the image is located.
	 * @param iconSize the size of the icon.
	 *
	 * @return the loaded image as icon.
	 */
	public static Icon getIcon(String path, Integer iconSize) {
		String filePath = RESOURCE_PATH + "gfx/icons/" + path;
		Icon icon = null;
		Image image;

		if (filePath.endsWith(".svg")) {
			try {
				URL url = ClassLoader.getSystemResource(filePath);

				SVGIcon svgicon = new SVGIcon();
				svgicon.setAntiAlias(true);
				svgicon.setAutosize(SVGIcon.AUTOSIZE_STRETCH);
				svgicon.setSvgURI(url.toURI());

				if (nonNull(iconSize)) {
					svgicon.setPreferredSize(new Dimension(iconSize, iconSize));
				}

				return svgicon;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		else {
			try {
				image = readImage(filePath);

				if (nonNull(iconSize) && nonNull(image)) {
					int imageWidth = image.getWidth(null);
					int imageHeight = image.getHeight(null);
					int imageSize = Math.min(imageWidth, imageHeight);
					double scale = iconSize.doubleValue() / imageSize;
					int scaleWidth = (int) (imageWidth * scale);
					int scaleHeight = (int) (imageHeight * scale);

					image = image.getScaledInstance(scaleWidth, scaleHeight,
							Image.SCALE_AREA_AVERAGING);
				}
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		if (nonNull(image)) {
			icon = new ScaledImageIcon(new ImageIcon(image));
		}

		return icon;
	}

	/**
	 * Returns an {@code BufferedImage} that is loaded from the specified path.
	 * 
	 * @param path the relative path where the image is located.
	 * 
	 * @return the loaded image.
	 */
	public static BufferedImage getImage(String path) {
		return readImage(RESOURCE_PATH + path);
	}

	/**
	 * Reads an image which can be located on the file system or in the
	 * jar-file.
	 * 
	 * @param path the path where the image is located.
	 * 
	 * @return the loaded image.
	 */
	public static BufferedImage readImage(String path) {
		BufferedImage image;
		URL url = getResourceURL(path);

		try {
			if (url != null) {
				image = ImageIO.read(url);
			}
			else {
				// Give a second chance to load the image.
				InputStream inputStream = new FileInputStream(path);
				image = ImageIO.read(inputStream);
				inputStream.close();
			}
		}
		catch (Exception e) {
			return null;
		}
		
		return image;
	}

}
