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

package org.lecturestudio.core.view;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;

import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.configuration.WhiteboardConfiguration;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Page;

import static java.util.Objects.isNull;

/**
 * This class defines how a page should be rendered on an output device. It
 * holds information about the portion of the page that should be rendered,
 * whether to show the grid lines, ...
 * <p>
 * Positions and lengths are in page metrics.
 *
 * @author Alex Andres
 * @author Tobias
 */
public class PresentationParameter {

	/** Support for handling property change listeners */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/** Application configuration containing presentation settings */
	private final Configuration config;

	/** The page being presented */
	private final Page page;

	/** Rectangle defining the portion of the page to be rendered (in page metrics) */
	private Rectangle2D pageRect = new Rectangle2D(0, 0, 1, 1);

	/** Stores the last custom rectangle used for extended mode to restore it when exiting zoom mode */
	private Rectangle2D lastExtendedRect;

	/** Translation offset for the current view */
	private Point2D translation = new Point2D(0, 0);

	/** Flag indicating whether to show the grid */
	private boolean showGrid;

	/** Flag indicating whether the page is in extended mode */
	private boolean isExtended;

	/** Flag indicating whether zoom mode is active */
	private boolean zoomMode;

	/** Flag indicating whether translation is enabled */
	private boolean translate;

	/** Remembers whether extended mode was enabled before entering zoom mode */
	private boolean extendedBeforeZoom;


	/**
	 * Constructs a new PresentationParameter with the specified configuration and page.
	 *
	 * @param config The application configuration containing presentation settings.
	 * @param page   The page to be presented.
	 */
	public PresentationParameter(Configuration config, Page page) {
		this.config = config;
		this.page = page;
	}

	/**
	 * Gets the background color for the presentation from the whiteboard configuration.
	 *
	 * @return The background color to be used.
	 */
	public Color getBackgroundColor() {
		return config.getWhiteboardConfig().getBackgroundColor();
	}

	/**
	 * Gets the whiteboard configuration from the application configuration.
	 *
	 * @return The whiteboard configuration.
	 */
	public WhiteboardConfiguration getWhiteboardConfig() {
		return config.getWhiteboardConfig();
	}

	/**
	 * Activates zoom mode for the presentation with the specified rectangle area.
	 * Saves the current extended state and disables it while in zoom mode.
	 *
	 * @param rect The rectangle defining the area to zoom to (in page metrics).
	 */
	public void zoom(Rectangle2D rect) {
		// Only remember the extended state when entering zoom mode the first time.
		if (!zoomMode) {
			extendedBeforeZoom = isExtended;
		}
		isExtended = false;
		zoomMode = true;

		setPageRect(rect);
	}

	/**
	 * Sets the extended mode for the presentation. When extended mode is enabled, the page rectangle
	 * is adjusted to the extended dimensions specified in the configuration. When disabled, it resets
	 * to standard dimensions (1x1). This method also disables zoom mode.
	 *
	 * @param extended True to enable extended mode, false to disable it.
	 */
	public void setExtendedMode(boolean extended) {
		setExtendedMode(extended, null);
	}

	/**
	 * Sets the extended mode for the presentation with custom dimensions. When extended mode is enabled,
	 * the page rectangle is adjusted to the dimensions specified by the provided rectangle or the
	 * configuration if no rectangle is provided. When disabled, it resets to standard dimensions (1x1).
	 * This method also disables zoom mode.
	 *
	 * @param extended True to enable extended mode, false to disable it.
	 * @param rect     The rectangle defining the dimensions to use for extended mode. If null, dimensions
	 *                 from the configuration will be used.
	 */
	public void setExtendedMode(boolean extended, Rectangle2D rect) {
		isExtended = extended;
		zoomMode = false;

		if (isExtended) {
			Dimension2D factor = isNull(rect)
					? config.getExtendPageDimension()
					: new Dimension2D(rect.getWidth(), rect.getHeight());

			lastExtendedRect = isNull(rect) ? null : new Rectangle2D(rect);

			setPageRect(new Rectangle2D(0, 0, factor.getWidth(), factor.getHeight()));
		}
		else {
			setPageRect(new Rectangle2D(0, 0, 1, 1));
		}
	}

	/**
	 * Checks whether the presentation is in extended mode.
	 *
	 * @return True if the extended mode is active.
	 */
	public boolean isExtended() {
		return isExtended;
	}

	/**
	 * Resets the page rectangle to its default state. Disables zoom mode and restores the extended
	 * mode if it was enabled before zooming. Otherwise, resets to standard dimensions (1x1).
	 */
	public void resetPageRect() {
		zoomMode = false;

		if (extendedBeforeZoom) {
			// Restore the extended mode if it was active before zoom.
			setExtendedMode(true, lastExtendedRect);
		}
		else {
			isExtended = false;
			setPageRect(new Rectangle2D(0, 0, 1, 1));

			// Clear the remembered state after handling reset.
			extendedBeforeZoom = false;
			lastExtendedRect = null;
		}
	}

	/**
	 * Gets a copy of the current page rectangle.
	 *
	 * @return A clone of the current page rectangle.
	 */
	public Rectangle2D getPageRect() {
		return pageRect.clone();
	}

	/**
	 * Sets the page rectangle to a new value and resets translation.
	 * Fires a property change event if the rectangle changes.
	 *
	 * @param pageRect The new page rectangle to set.
	 */
	public void setPageRect(Rectangle2D pageRect) {
		if (!this.pageRect.equals(pageRect)) {
			// Translation is only temporary.
			translation = new Point2D();

			Rectangle2D oldValue = this.pageRect;
			this.pageRect = new Rectangle2D(pageRect);

			pcs.firePropertyChange("PageRect", oldValue, pageRect);
		}
	}

	/**
	 * Checks whether the grid display is enabled.
	 *
	 * @return True if the grid should be shown.
	 */
	public boolean showGrid() {
		return showGrid;
	}

	/**
	 * Sets whether to show the grid on the presentation.
	 * Fires a property change event if the value changes.
	 *
	 * @param showGrid True to show the grid, false to hide it.
	 */
	public void setShowGrid(boolean showGrid) {
		if (this.showGrid != showGrid) {
			boolean oldValue = this.showGrid;
			this.showGrid = showGrid;
			pcs.firePropertyChange("ShowGrid", oldValue, showGrid);
		}
	}

	/**
	 * Sets the translation offset for the current view.
	 * Fires a property change event if the value changes.
	 *
	 * @param translation The new translation point to set.
	 */
	public void setTranslation(Point2D translation) {
		if (this.translation != translation) {
			Point2D oldValue = this.translation;
			this.translation = translation;
			pcs.firePropertyChange("Translation", oldValue, translation);
		}
	}

	/**
	 * Gets the current translation offset.
	 *
	 * @return The current translation point.
	 */
	public Point2D getTranslation() {
		return translation;
	}
	
	/**
	 * Checks whether translation is enabled.
	 *
	 * @return True if translation is enabled, false otherwise.
	 */
	public boolean isTranslation() {
		return translate;
	}
	
	/**
	 * Enables or disables translation mode.
	 *
	 * @param translate True to enable translation, false to disable it.
	 */
	public void setTranslation(boolean translate) {
		this.translate = translate;
	}

	/**
	 * Adds a property change listener to monitor parameter changes.
	 * Prevents duplicating listeners for the same property and listener.
	 *
	 * @param listener The property change listener to add.
	 */
	public void addParameterChangeListener(PropertyChangeListener listener) {
		if (listener instanceof PropertyChangeListenerProxy) {
			PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;

			for (PropertyChangeListener l : pcs.getPropertyChangeListeners(proxy.getPropertyName())) {
				if (l == proxy.getListener()) {
					return;
				}
			}
		}

		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Removes a property change listener.
	 *
	 * @param l The property change listener to remove.
	 */
	public void removeParameterChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	/**
	 * Gets the page being presented.
	 *
	 * @return The current page.
	 */
	public Page getPage() {
		return page;
	}

	/**
	 * Checks whether zoom mode is active.
	 *
	 * @return True if zoom mode is active, false otherwise.
	 */
	public boolean isZoomMode() {
		return zoomMode;
	}
	
	/**
	 * Gets the view rectangle adjusted by the current translation.
	 * This represents the actual visible area in the presentation.
	 *
	 * @return The view rectangle with translation applied.
	 */
	public Rectangle2D getViewRect() {
		Rectangle2D viewRect = getPageRect();
		viewRect.setLocation(viewRect.getX() + getTranslation().getX(),
							 viewRect.getY() + getTranslation().getY());
		
		return viewRect;
	}

	/**
	 * Copies parameter values from another PresentationParameter object.
	 * This includes page rectangle, grid visibility, translation, extended mode and zoom mode.
	 *
	 * @param other The PresentationParameter to copy values from.
	 */
	public void copy(PresentationParameter other) {
		// PageRect reflects zoom and extended state.
		setPageRect(other.getPageRect());
		setShowGrid(other.showGrid());
		setTranslation(other.getTranslation());

		isExtended = other.isExtended;
		zoomMode = other.zoomMode;
	}
}
