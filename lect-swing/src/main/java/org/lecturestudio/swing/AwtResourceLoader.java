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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.lecturestudio.core.io.ResourceLoader;
import org.lecturestudio.core.util.FileUtils;

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
	 * Returns an {@code URI} of an {@code Icon}.
	 *
	 * @param path the relative path where the image is located.
	 *
	 * @return the URI of the icon.
	 */
	public static URI getIconURI(String path) throws URISyntaxException {
		String filePath = RESOURCE_PATH + "gfx/icons/" + path;
		return ClassLoader.getSystemResource(filePath).toURI();
	}

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
				image = loadMultiresolutionImage(filePath);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		if (nonNull(image)) {
			icon = new ImageIcon(image);
		}

		return icon;
	}

	/**
	 * Returns an {@code BufferedImage} that is loaded from the specified path
	 * that points to a SVG.
	 *
	 * @param path     the relative path where the SVG is located.
	 * @param iconSize the size of the rendered image.
	 *
	 * @return the loaded image.
	 */
	public static BufferedImage getSVGImage(String path, Integer iconSize) {
		if (!path.endsWith(".svg")) {
			return null;
		}

		String filePath = RESOURCE_PATH + path;

		try {
			URL url = ClassLoader.getSystemResource(filePath);

			SVGIcon svgicon = new SVGIcon();
			svgicon.setAntiAlias(true);
			svgicon.setAutosize(SVGIcon.AUTOSIZE_STRETCH);
			svgicon.setSvgURI(url.toURI());

			if (nonNull(iconSize)) {
				svgicon.setPreferredSize(new Dimension(iconSize, iconSize));
			}

			return (BufferedImage) svgicon.getImage();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	public static Image loadMultiresolutionImage(String fileName) throws Exception {
		Path parent = Paths.get(fileName).getParent();
		String parentPath = parent.toString().replace("\\", "/");

		String ext = FileUtils.getExtension(fileName);
		String name = FileUtils.stripExtension(FileUtils.toPlatformPath(fileName));

		String[] listing = FileUtils.getResourceListing(parentPath, (filePath) -> {
			filePath = FileUtils.toPlatformPath(filePath);

			// Common prefix index.
			int index = filePath.indexOf(name);
			if (index == -1) {
				return false;
			}

			filePath = filePath.substring(filePath.indexOf(name));

			return filePath.matches(name.replace("\\", "\\\\") + "(-(\\d+)x(\\d+))?." + ext);
		});

		if (listing.length == 0) {
			return null;
		}

		List<String> files = Arrays.asList(listing);

		// TODO: use correct comparator
		files.sort(Comparator.reverseOrder());

		List<Image> images = new ArrayList<>();

		for (String imagePath : files) {
			Image image = readImage(imagePath);

			if (nonNull(image)) {
				images.add(image);
			}
		}

		return new BaseMultiResolutionImage(images.toArray(new Image[0]));
	}

}
