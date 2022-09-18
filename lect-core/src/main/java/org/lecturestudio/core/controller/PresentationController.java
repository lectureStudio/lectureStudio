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

package org.lecturestudio.core.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.DisplayConfiguration;
import org.lecturestudio.core.app.configuration.ScreenConfiguration;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ImmutableBooleanProperty;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.presenter.PresentationPresenter;
import org.lecturestudio.core.service.DisplayService;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.PresentationViewContext;
import org.lecturestudio.core.view.PresentationViewFactory;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.core.view.SlideViewOverlay;

/**
 * The {@link PresentationController} maintains presentation windows displaying document
 * pages that are shown on connected displays. The windows can be switched on
 * and off for each individual display.
 *
 * @author Alex Andres
 */
public class PresentationController {

	/** The application context. */
	private final ApplicationContext context;

	/** The configuration for connected displays. */
	private final DisplayConfiguration displayConfig;

	/** The display service to obtain connected displays. */
	private final DisplayService displayService;

	/** Indicates whether any displays are connected or not. */
	private final ImmutableBooleanProperty screensAvailable;

	/** Indicates whether any presentation windows are visible or not. */
	private final ImmutableBooleanProperty presentationViewsVisible;

	/** The presentation window factory. */
	private final PresentationViewFactory factory;

	/** The mapping of Screen to {@link PresentationPresenter}. */
	private final Map<Screen, PresentationPresenter<?>> views;

	/** The bounds of the main window. */
	private Rectangle2D mainWindowBounds;

	/** The current presentation context. */
	private PresentationViewContext presentationContext;


	/**
	 * Create a {@link PresentationController} with the specified arguments.
	 *
	 * @param context        The application context.
	 * @param displayService The display service to obtain connected displays.
	 * @param factory        The presentation window factory.
	 */
	public PresentationController(ApplicationContext context, DisplayService displayService, PresentationViewFactory factory) {
		this.context = context;
		this.displayConfig = context.getConfiguration().getDisplayConfig();
		this.displayService = displayService;
		this.factory = factory;
		this.views = new ConcurrentHashMap<>();
		this.screensAvailable = new ImmutableBooleanProperty();
		this.presentationViewsVisible = new ImmutableBooleanProperty();

		initialize();
	}

	/**
	 * Add an overlay to the presentation windows displaying document pages.
	 *
	 * @param overlay The overlay to add.
	 */
	public void addSlideViewOverlay(SlideViewOverlay overlay) {
		for (PresentationPresenter<?> presenter : views.values()) {
			presenter.getPresentationView().addOverlay(overlay);
		}
	}

	/**
	 * Remove an overlay from the presentation windows displaying document pages.
	 *
	 * @param overlay The overlay to remove.
	 */
	public void removeSlideViewOverlay(SlideViewOverlay overlay) {
		for (PresentationPresenter<?> presenter : views.values()) {
			presenter.getPresentationView().removeOverlay(overlay);
		}
	}

	/**
	 * Returns a {@link ObservableList} containing all connected screens.
	 *
	 * @return The list of connected screens.
	 */
	public ObservableList<Screen> getScreens() {
		return displayService.getScreens();
	}

	/**
	 * Check whether any displays are connected or not.
	 *
	 * @return {@code true} if at least one display is connected, otherwise {@code false}.
	 */
	public boolean getScreensAvailable() {
		return screensAvailable.get();
	}

	/**
	 * Get the screens available property.
	 *
	 * @return The screens available property.
	 */
	public BooleanProperty screensAvailableProperty() {
		return screensAvailable.getImmutableProperty();
	}

	/**
	 * Check whether any presentation windows are visible or not.
	 *
	 * @return {@code true} if at least one presentation window is visible, otherwise {@code false}.
	 */
	public boolean getPresentationViewsVisible() {
		return presentationViewsVisible.get();
	}

	/**
	 * Get the presentation views visible property.
	 *
	 * @return the presentation views visible property.
	 */
	public BooleanProperty presentationViewsVisibleProperty() {
		return presentationViewsVisible.getImmutableProperty();
	}

	/**
	 * Set the bound of the main window.
	 *
	 * @param bounds The bounds of the main window.
	 */
	public void setMainWindowBounds(Rectangle2D bounds) {
		this.mainWindowBounds = bounds;

		updateScreensAvailable(getScreens());
	}

	/**
	 * Set the presentation context that controls what and how is being shown on
	 * the presentation view.
	 *
	 * @param context The new presentation context.
	 */
	public void setPresentationViewContext(PresentationViewContext context) {
		presentationContext = context;

		for (PresentationPresenter<?> presenter : views.values()) {
			presenter.getPresentationView().setPresentationViewContext(context);
		}
	}

	/**
	 * Show or hide all presentation windows.
	 *
	 * @param show True to show all presentation windows, false to hide all windows.
	 */
	public void showPresentationViews(boolean show) {
		for (Screen screen : views.keySet()) {
			showPresentationView(screen, show);
		}
	}

	/**
	 * Show a presentation window that lies within the bounds of the specified screen.
	 *
	 * @param screen The screen containing a presentation window.
	 * @param show   True to show a presentation window on that screen, false to hide a window.
	 */
	public void showPresentationView(Screen screen, boolean show) {
		PresentationPresenter<?> presenter = views.get(screen);

		if (nonNull(presenter)) {
			if (containsMainWindow(screen)) {
				return;
			}

			for (ScreenConfiguration screenConfig : displayConfig.getScreens()) {
				if (screenConfig.getScreen().equals(screen)) {
					// Override screen view visibility.
					show &= screenConfig.getEnabled();
					break;
				}
			}

			presenter.getPresentationView().setVisible(show);

			updatePresentationViewsVisible();
		}
	}

	private void initialize() {
		ObservableList<Screen> screens = getScreens();

		initializePresentationViews();
		updateScreensAvailable(screens);

		setPresentationBackground(displayConfig.getBackgroundColor());

		displayConfig.backgroundColorProperty().addListener((observable, oldColor, newColor) -> {
			setPresentationBackground(newColor);
		});

		screens.addListener(new ListChangeListener<>() {

			@Override
			public void listChanged(ObservableList<Screen> list) {
				updatePresentationViews(list);
				updateScreensAvailable(list);
			}
		});
	}

	private void initializePresentationViews() {
		List<Screen> screenList = getScreens();

		for (Screen screen : screenList) {
			PresentationPresenter<?> presenter = views.get(screen);

			if (isNull(presenter)) {
				presenter = factory.createPresentationView(context, screen);

				if (nonNull(presenter)) {
					views.put(screen, presenter);
				}
			}
		}
	}

	private void setPresentationBackground(Color color) {
		for (PresentationPresenter<?> presenter : views.values()) {
			presenter.getPresentationView().setBackgroundColor(color);
		}
	}

	private void updateScreensAvailable(List<Screen> connectedScreens) {
		int available = 0;

		for (Screen screen : connectedScreens) {
			if (!containsMainWindow(screen)) {
				available++;
			}
		}

		screensAvailable.set(available > 0);
	}

	private void updatePresentationViewsVisible() {
		int visible = 0;

		for (PresentationPresenter<?> presenter : views.values()) {
			if (presenter.getPresentationView().isVisible()) {
				visible++;
			}
		}

		presentationViewsVisible.set(visible > 0);
	}

	private void updatePresentationViews(List<Screen> connectedScreens) {
		// Remove disconnected screens.
		for (Screen screen : views.keySet()) {
			if (!connectedScreens.contains(screen)) {
				// Close presentation view.
				PresentationPresenter<?> presenter = views.remove(screen);

				if (nonNull(presenter)) {
					presenter.close();
				}
			}
		}

		// Add connected screens.
		Set<Screen> set = views.keySet();

		for (Screen screen : connectedScreens) {
			if (!set.contains(screen)) {
				PresentationPresenter<?> presenter = factory.createPresentationView(context, screen);

				if (nonNull(presenter)) {
					presenter.getPresentationView().setPresentationViewContext(presentationContext);

					views.put(screen, presenter);

					showPresentationView(screen, displayConfig.getAutostart());
				}
			}
		}
	}

	private boolean containsMainWindow(Screen screen) {
		if (isNull(mainWindowBounds)) {
			return false;
		}

		Rectangle2D screenBounds = screen.getBounds();

		if (screenBounds.contains(mainWindowBounds)) {
			// Don't show any presentation views on the screen that contains the main window.
			return true;
		}

		// The main window may cover more area than the screen itself or cover a huge area on the screen.
		Rectangle2D intersection = mainWindowBounds.intersection(screenBounds);

		if (nonNull(intersection)) {
			double screenArea = screenBounds.getWidth() * screenBounds.getHeight();
			double intersectionArea = intersection.getWidth() * intersection.getHeight();

			// The common screen/window area covers more than 10 percent of the screen bounds.
			return intersectionArea / screenArea > 0.1;
		}

		return false;
	}
}
