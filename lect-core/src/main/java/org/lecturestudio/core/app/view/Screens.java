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

package org.lecturestudio.core.app.view;

import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.view.Screen;

/**
 * Screen helper class to retrieve information about connected graphic output
 * devices.
 *
 * @author Alex Andres
 */
public final class Screens {

	/** Local Graphics Environment. */
	private static final GraphicsEnvironment GE = GraphicsEnvironment.getLocalGraphicsEnvironment();


	/**
	 * Get all connected screens including the primary screen.
	 *
	 * @return an array of all connected screens.
	 */
	public static Screen[] getAllScreens() {
		GraphicsDevice[] devices = getScreenDevices();
		Screen[] screens = new Screen[devices.length];

		for (int i = 0; i < screens.length; i++) {
			screens[i] = createScreen(devices[i]);
		}

		return screens;
	}

	/**
	 * Get all connected screens without the primary screen.
	 *
	 * @return an array of all connected screens.
	 */
	public static Screen[] getConnectedScreens() {
		Rectangle defaultBounds = GE.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		GraphicsDevice[] devices = getScreenDevices();
		Screen[] screens = new Screen[devices.length - 1];

		for (int i = 0, c = 0; i < devices.length; i++) {
			GraphicsDevice device = devices[i];
			Rectangle bounds = device.getDefaultConfiguration().getBounds();

			if (!bounds.equals(defaultBounds)) {
				screens[c++] = createScreen(device);
			}
		}

		return screens;
	}

	/**
	 * Get all connected screen devices.
	 *
	 * @return an array of screen devices.
	 */
	public static GraphicsDevice[] getScreenDevices() {
		return GE.getScreenDevices();
	}

	/**
	 * Get the default (primary) screen device.
	 *
	 * @return default screen device.
	 */
	public static GraphicsDevice getDefaultScreenDevice() {
		return GE.getDefaultScreenDevice();
	}

	/**
	 * Get a screen device which contains the specified container. If the
	 * container has not been placed yet on any screen the default screen device
	 * is returned. In case the container cannot be assigned to any screen, then
	 * {@code null} is returned. The decision to choose the screen is based on
	 * the area that the container covers on the corresponding screen.
	 *
	 * @param container A component container.
	 *
	 * @return a screen device or {@code null}.
	 */
	public static GraphicsDevice getScreenDevice(Container container) {
		Rectangle bounds = container.getBounds();

		if (bounds.isEmpty()) {
			return getDefaultScreenDevice();
		}

		// Covered area of the container on the screen.
		int area = 0;

		GraphicsDevice result = null;
		GraphicsDevice[] devices = getScreenDevices();

		for (GraphicsDevice device : devices) {
			Rectangle screenSize = device.getDefaultConfiguration().getBounds();
			Rectangle coveredSize = screenSize.intersection(bounds);
			int coveredArea = coveredSize.width * coveredSize.height;

			if (coveredArea > area) {
				area = coveredArea;
				result = device;
			}
		}

		return result;
	}

	/**
	 * Get {@link GraphicsConfiguration} associated with a graphics device which
	 * has the bounds defined by the provided screen.
	 *
	 * @param screen The screen for which to obtain the {@link GraphicsConfiguration}.
	 *
	 * @return a {@link GraphicsConfiguration} or {@code null}.
	 */
	public static GraphicsConfiguration getGraphicsConfiguration(Screen screen) {
		GraphicsDevice[] devices = GE.getScreenDevices();

		for (GraphicsDevice device : devices) {
			Rectangle bounds = device.getDefaultConfiguration().getBounds();
			Rectangle screenBounds = toAwtRectangle(screen.getBounds());

			if (bounds.equals(screenBounds)) {
				return device.getDefaultConfiguration();
			}
		}

		return null;
	}

	/**
	 * Converts a {@link Rectangle2D} into a {@link Rectangle}
	 *
	 * @param r {@link Rectangle2D} to be converted
	 * @return the converted {@link Rectangle}
	 */
	private static Rectangle toAwtRectangle(Rectangle2D r) {
		return new Rectangle((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
	}

	private static Screen createScreen(GraphicsDevice device) {
		GraphicsConfiguration deviceConfig = device.getDefaultConfiguration();
		Rectangle bounds = deviceConfig.getBounds();

		int screenWidth = (int) (bounds.width * deviceConfig.getDefaultTransform().getScaleX());
		int screenHeight = (int) (bounds.height * deviceConfig.getDefaultTransform().getScaleY());

		return new Screen(bounds.x, bounds.y, screenWidth, screenHeight);
	}
}
