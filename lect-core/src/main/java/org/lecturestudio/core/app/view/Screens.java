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
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

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
		return convertDisplayDevices(getScreenDevices()).toArray(new Screen[0]);
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
	 * container has not been placed yet on any screen, the default screen device
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

	public static Screen createScreen(GraphicsDevice device, GraphicsDevice primaryDevice) {
		if (primaryDevice == null) {
			primaryDevice = getDefaultScreenDevice();
		}

		GraphicsConfiguration primaryDeviceConfig = primaryDevice.getDefaultConfiguration();
		Rectangle primaryBounds = primaryDeviceConfig.getBounds();

		GraphicsConfiguration deviceConfig = device.getDefaultConfiguration();
		Rectangle bounds = deviceConfig.getBounds();
		AffineTransform transform = deviceConfig.getDefaultTransform();

		int x, y, w, h;

		if (device.equals(primaryDevice)) {
			x = (int) (bounds.x * transform.getScaleX());
			y = (int) (bounds.y * transform.getScaleY());
			w = (int) (bounds.width * transform.getScaleX());
			h = (int) (bounds.height * transform.getScaleY());
		}
		else {
			x = (int) ((bounds.x - primaryBounds.x) * transform.getScaleX());
			y = (int) ((bounds.y - primaryBounds.y) * transform.getScaleY());
			w = (int) (bounds.width * transform.getScaleX());
			h = (int) (bounds.height * transform.getScaleY());
		}

		return new Screen(x, y, w, h, device);
	}

	/**
	 * Converts multiple displays from logical to pixel coordinates.
	 *
	 * @param devices The graphics devices that represent connected displays.
	 *
	 * @return List of Screen objects.
	 */
	public static List<Screen> convertDisplayDevices(GraphicsDevice[] devices) {
		List<Screen> screens = new ArrayList<>();

		for (GraphicsDevice device : devices) {
			GraphicsConfiguration deviceConfig = device.getDefaultConfiguration();
			Rectangle logicalBounds = deviceConfig.getBounds();
			AffineTransform transform = deviceConfig.getDefaultTransform();

			screens.add(new Screen(logicalBounds.x, logicalBounds.y,
					(int) (logicalBounds.width * transform.getScaleX()),
					(int) (logicalBounds.height * transform.getScaleX()),
					device));
		}

		return screens;
	}
}
