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

import static java.util.Objects.nonNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.ParameterChangeListener;

/**
 * Manages a set of {@code PresentationParameter}s and notifies listeners
 * whenever a parameter has changed. Usually a {@code PresentationParameterProvider}
 * exists for each {@link ViewType}.
 *
 * @author Alex Andres
 * @author Tobias
 */
public class PresentationParameterProvider implements PropertyChangeListener {

	/**
	 * Maps Page objects to their corresponding PresentationParameter.
	 * Stores the presentation parameters for each page.
	 */
	private final Map<Page, PresentationParameter> parameter = new HashMap<>();

	/**
	 * List of listeners that get notified when presentation parameters change.
	 * These listeners respond to changes in parameters for their associated pages.
	 */
	private final List<ParameterChangeListener> listener = new ArrayList<>();

	/**
	 * The application configuration containing settings for the presentation,
	 * including whiteboard settings like grid colors and intervals.
	 */
	private final Configuration config;


	/**
	 * Constructs a new PresentationParameterProvider with the specified configuration.
	 * This constructor initializes change listeners for the whiteboard configuration properties
	 * that affect all presentation parameters, such as grid colors, line intervals, and visibility settings.
	 * When these configuration properties change, all parameter change listeners are notified.
	 *
	 * @param config The application configuration containing presentation settings.
	 */
	public PresentationParameterProvider(Configuration config) {
		this.config = config;

		// Listener for color changes in the whiteboard configuration.
		ChangeListener<Color> colorListener = (observable, oldValue, newValue) -> {
			notifyAllParameterChangeListeners();
		};
		// Listener for grid interval changes in the whiteboard configuration.
		ChangeListener<Double> gridIntervalListener = (observable, oldValue, newValue) -> {
			notifyAllParameterChangeListeners();
		};
		// Listener for grid line visibility changes in the whiteboard configuration.
		ChangeListener<Boolean> gridLineListener = (observable, oldValue, newValue) -> {
			notifyAllParameterChangeListeners();
		};

		// Register listeners for all relevant whiteboard configuration properties
		config.getWhiteboardConfig().gridColorProperty().addListener(colorListener);
		config.getWhiteboardConfig().horizontalLinesIntervalProperty().addListener(gridIntervalListener);
		config.getWhiteboardConfig().horizontalLinesVisibleProperty().addListener(gridLineListener);
		config.getWhiteboardConfig().verticalLinesIntervalProperty().addListener(gridIntervalListener);
		config.getWhiteboardConfig().verticalLinesVisibleProperty().addListener(gridLineListener);
		config.getWhiteboardConfig().backgroundColorProperty().addListener(colorListener);
	}

	/**
	 * Gets the presentation parameter for the specified page.
	 * If no parameter exists for the page, a new one is created.
	 *
	 * @param page The page for which to get the presentation parameter.
	 *
	 * @return The presentation parameter for the specified page, or null if the page is null.
	 */
	public PresentationParameter getParameter(Page page) {
		if (page == null) {
			return null;
		}

		PresentationParameter param = parameter.get(page);

		// If there isn't any, create a new one.
		if (param == null) {
			param = new PresentationParameter(config, page);
			setParameter(param);
		}
		return param;
	}

	/**
	 * Removes the presentation parameter for the specified page.
	 *
	 * @param p The page for which to clear the parameter.
	 */
	public void clearParameter(Page p) {
		parameter.remove(p);
	}

	/**
	 * Returns all presentation parameters currently managed by this provider.
	 *
	 * @return A collection of all presentation parameters.
	 */
	public Collection<PresentationParameter> getAllPresentationParameters() {
		return parameter.values();
	}

	/**
	 * Notifies all registered parameter change listeners about parameter changes.
	 * For each listener, retrieves the parameter for the listener's page and
	 * triggers the parameterChanged callback.
	 */
	public void notifyAllParameterChangeListeners() {
		for (ParameterChangeListener l : listener) {
			PresentationParameter param = getParameter(l.forPage());

			if (nonNull(param)) {
				l.parameterChanged(param.getPage(), param);
			}
		}
	}

	/**
	 * Sets the presentation parameter for a page and registers this provider
	 * as a listener for changes to that parameter.
	 *
	 * @param para The presentation parameter to set.
	 */
	public void setParameter(PresentationParameter para) {
		parameter.put(para.getPage(), para);

		para.removeParameterChangeListener(this);
		para.addParameterChangeListener(this);
	}

	/**
	 * Adds a parameter change listener to be notified when parameters change.
	 *
	 * @param l The parameter change listener to add.
	 */
	public void addParameterChangeListener(ParameterChangeListener l) {
		listener.add(l);
	}

	/**
	 * Removes a parameter change listener.
	 *
	 * @param l The parameter change listener to remove.
	 */
	public void removeParameterChangeListener(ParameterChangeListener l) {
		listener.remove(l);
	}

	/**
	 * Called when a property changes in a presentation parameter.
	 * Notifies only those listeners that are associated with the page
	 * of the changed parameter.
	 *
	 * @param evt The property change event containing the source parameter that changed.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Forward notifications from Parameter to interested listeners
		PresentationParameter para = (PresentationParameter) evt.getSource();
		for (ParameterChangeListener l : listener) {
			if (l.forPage() == para.getPage()) {
				l.parameterChanged(para.getPage(), para);
			}
		}
	}

	/**
	 * Removes all presentation parameters from this provider.
	 */
	public void clearParameters() {
		parameter.clear();
	}

}
