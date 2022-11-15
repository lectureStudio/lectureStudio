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

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private final Configuration config;
	private final Page page;

	private Rectangle2D pageRect = new Rectangle2D(0, 0, 1, 1);
	private Point2D translation = new Point2D(0, 0);

	private boolean showGrid;
	private boolean isExtended;
	private boolean zoomMode;
	private boolean translate;


	public PresentationParameter(Configuration config, Page page) {
		this.config = config;
		this.page = page;

		setShowGrid(getWhiteboardConfig().getShowGridAutomatically());
	}

	public Color getBackgroundColor() {
		return config.getWhiteboardConfig().getBackgroundColor();
	}

	public WhiteboardConfiguration getWhiteboardConfig() {
		return config.getWhiteboardConfig();
	}

	/**
	 * Sets the zoom mode and adjusts the page rectangle
	 * 
	 * @param rect
	 */
	public void zoom(Rectangle2D rect) {
		isExtended = false;
		zoomMode = true;

		setPageRect(rect);
	}

	/**
	 * Sets the extended mode
	 * 
	 * @param extended
	 */
	public void setExtendedMode(boolean extended) {
		isExtended = extended;
		zoomMode = false;

		if (isExtended) {
			Dimension2D factor = config.getExtendPageDimension();

			setPageRect(new Rectangle2D(0, 0, factor.getWidth(), factor.getHeight()));
		}
		else {
			setPageRect(new Rectangle2D(0, 0, 1, 1));
		}
	}

	public boolean isExtended() {
		return isExtended;
	}

	/**
	 * Resets the page rectangle according to the extended mode
	 */
	public void resetPageRect() {
		isExtended = false;
		zoomMode = false;

		setPageRect(new Rectangle2D(0, 0, 1, 1));
	}
	
	/**
	 * Returns the page rectangle, e.g. the portion of the page that should be
	 * rendered
	 * 
	 * @return
	 */
	public Rectangle2D getPageRect() {
		return pageRect.clone();
	}

	/**
	 * Sets the page rectangle
	 * 
	 * @param pageRect
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
	 * True if the grid should be drawn
	 * 
	 * @return
	 */
	public boolean showGrid() {
		return showGrid;
	}

	/**
	 * Sets whether the grid should be drawn
	 * 
	 * @param showGrid
	 */
	public void setShowGrid(boolean showGrid) {
		if (this.showGrid != showGrid) {
			boolean oldValue = this.showGrid;
			this.showGrid = showGrid;
			pcs.firePropertyChange("ShowGrid", oldValue, showGrid);
		}
	}

	/**
	 * Sets the translation
	 * 
	 * @param translation
	 */
	public void setTranslation(Point2D translation) {
		if (this.translation != translation) {
			Point2D oldValue = this.translation;
			this.translation = translation;
			pcs.firePropertyChange("Translation", oldValue, translation);
		}
	}

	/**
	 * returns the translation
	 * 
	 * @return
	 */
	public Point2D getTranslation() {
		return translation;
	}
	
	public boolean isTranslation() {
		return translate;
	}
	
	public void setTranslation(boolean translate) {
		this.translate = translate;
	}

	/**
	 * Listener is notified whenever a property is changed
	 * 
	 * @param listener
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

	public void removeParameterChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	/**
	 * Returns the page
	 * 
	 * @return
	 */
	public Page getPage() {
		return page;
	}

	/**
	 * True if in zoom mode
	 * 
	 * @return
	 */
	public boolean isZoomMode() {
		return zoomMode;
	}
	
	public Rectangle2D getViewRect() {
		Rectangle2D viewRect = getPageRect();
		viewRect.setLocation(viewRect.getX() + getTranslation().getX(),
							 viewRect.getY() + getTranslation().getY());
		
		return viewRect;
	}

	public void copy(PresentationParameter other) {
		// PageRect reflects zoom and extended state.
		setPageRect(other.getPageRect());
		setShowGrid(other.showGrid());
		setTranslation(other.getTranslation());

		isExtended = other.isExtended;
		zoomMode = other.zoomMode;
	}
}
