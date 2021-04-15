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

package org.lecturestudio.core.app.configuration;

import java.util.Locale;

import org.lecturestudio.core.app.Theme;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableHashMap;
import org.lecturestudio.core.util.ObservableList;

/**
 * The Configuration specifies application wide properties. Context specific
 * properties are encapsulated in the respective separate configuration classes.
 *
 * @author Alex Andres
 */
public class Configuration {

	/** The name of the application. */
	private final StringProperty applicationName = new StringProperty();

	/** The theme of the UI of the application. */
	private final ObjectProperty<Theme> theme = new ObjectProperty<>();

	/** The locale of the application. */
	private final ObjectProperty<Locale> locale = new ObjectProperty<>();

	/** The UI control size of the application. */
	private final DoubleProperty uiControlSize = new DoubleProperty();

	/** Indicates whether to open the application window maximized. */
	private final BooleanProperty startMaximized = new BooleanProperty();

	/** Indicates whether to open the application in fullscreen mode. */
	private final BooleanProperty startFullscreen = new BooleanProperty();

	/** Indicates whether to enable a virtual keyboard on tablet devices. */
	private final BooleanProperty tabletMode = new BooleanProperty();

	/** Enables/disables advanced settings visible in the settings UI view. */
	private final BooleanProperty advancedUIMode = new BooleanProperty();

	/** Hides/shows UI elements, like the menu, in fullscreen mode. */
	private final BooleanProperty extendedFullscreen = new BooleanProperty();

	/** Defines the extended drawing area of a page. */
	private final ObjectProperty<Dimension2D> extendPageDimension = new ObjectProperty<>();

	/** The list of recently opened slide documents. */
	private final ObservableList<RecentDocument> recentDocuments = new ObservableArrayList<>();

	/** The mapping of a filesystem path to a related context. */
	private final ObservableHashMap<String, String> contextPaths = new ObservableHashMap<>();

	/** The grid configuration containing all grid related properties. */
	private final GridConfiguration gridConfig = new GridConfiguration();

	/** The whiteboard configuration containing all whiteboard related properties. */
	private final WhiteboardConfiguration whiteboardConfig = new WhiteboardConfiguration();

	/** The grid display containing all display related properties. */
	private final DisplayConfiguration displayConfig = new DisplayConfiguration();

	/** The tool configuration containing all tool related properties. */
	private final ToolConfiguration toolConfig = new ToolConfiguration();

	/** The audio configuration containing all audio related properties. */
	private final AudioConfiguration audioConfig = new AudioConfiguration();


	/**
	 * Obtain the name of the application.
	 *
	 * @return the application name.
	 */
	public String getApplicationName() {
		return applicationName.get();
	}

	/**
	 * Set the name of the application.
	 *
	 * @param name The application name.
	 */
	public void setApplicationName(String name) {
		this.applicationName.set(name);
	}

	/**
	 * Obtain the application name property.
	 *
	 * @return the application name property.
	 */
	public ObjectProperty<String> applicationNameProperty() {
		return applicationName;
	}

	/**
	 * Obtain the current theme of the UI of the application.
	 *
	 * @return the UI theme.
	 */
	public Theme getTheme() {
		return theme.get();
	}

	/**
	 * Set the new UI theme.
	 *
	 * @param theme The UI theme to set.
	 */
	public void setTheme(Theme theme) {
		this.theme.set(theme);
	}

	/**
	 * Obtain the theme property.
	 *
	 * @return the theme property.
	 */
	public ObjectProperty<Theme> themeProperty() {
		return theme;
	}

	/**
	 * Obtain the current locale of the application.
	 *
	 * @return the current locale.
	 */
	public Locale getLocale() {
		return locale.get();
	}

	/**
	 * Set the new locale of the application.
	 *
	 * @param locale The new locale to set.
	 */
	public void setLocale(Locale locale) {
		this.locale.set(locale);
	}

	/**
	 * Obtain the locale property.
	 *
	 * @return the locale property.
	 */
	public ObjectProperty<Locale> localeProperty() {
		return locale;
	}

	/**
	 * Obtain the UI control size of the application.
	 *
	 * @return the UI control size.
	 */
	public double getUIControlSize() {
		return uiControlSize.get();
	}

	/**
	 * Set the new UI control size of the application.
	 *
	 * @param size The new UI control size.
	 */
	public void setUIControlSize(double size) {
		this.uiControlSize.set(size);
	}

	/**
	 * Obtain the UI control size property.
	 *
	 * @return the UI control size property.
	 */
	public DoubleProperty uiControlSizeProperty() {
		return uiControlSize;
	}

	/**
	 * Check whether to open the application window maximized.
	 *
	 * @return true if the application window should be opened maximized, false
	 * otherwise.
	 */
	public Boolean getStartMaximized() {
		return startMaximized.get();
	}

	/**
	 * Set whether to open the application window maximized.
	 *
	 * @param maximized True to open the application window maximized, false
	 *                  otherwise.
	 */
	public void setStartMaximized(boolean maximized) {
		this.startMaximized.set(maximized);
	}

	/**
	 * Obtain the start maximized property.
	 *
	 * @return the start maximized property.
	 */
	public BooleanProperty startMaximizedProperty() {
		return startMaximized;
	}

	/**
	 * Check whether to open the application window in fullscreen mode.
	 *
	 * @return true if the application window should be opened fullscreen, false
	 * otherwise.
	 */
	public Boolean getStartFullscreen() {
		return startFullscreen.get();
	}

	/**
	 * Set whether to open the application window in fullscreen mode.
	 *
	 * @param fullscreen True to open the application window fullscreen, false
	 *                   otherwise.
	 */
	public void setStartFullscreen(boolean fullscreen) {
		this.startFullscreen.set(fullscreen);
	}

	/**
	 * Obtain the start fullscreen property.
	 *
	 * @return the start fullscreen property.
	 */
	public BooleanProperty startFullscreenProperty() {
		return startFullscreen;
	}

	/**
	 * Check whether to enable a virtual keyboard on tablet devices.
	 *
	 * @return true to enable a virtual keyboard, false otherwise.
	 */
	public Boolean getTabletMode() {
		return tabletMode.get();
	}

	/**
	 * Set whether to enable a virtual keyboard on tablet devices.
	 *
	 * @param enable True to enable a virtual keyboard, false otherwise.
	 */
	public void setTabletMode(boolean enable) {
		this.tabletMode.set(enable);
	}

	/**
	 * Obtain the tablet mode property.
	 *
	 * @return the tablet mode property.
	 */
	public BooleanProperty tabletModeProperty() {
		return tabletMode;
	}

	/**
	 * Check whether to hide/show UI elements, like the menu, in fullscreen
	 * mode.
	 *
	 * @return true if the extended fullscreen mode is enabled, false otherwise.
	 *
	 * @see #setExtendedFullscreen(boolean)
	 */
	public Boolean getExtendedFullscreen() {
		return extendedFullscreen.get();
	}

	/**
	 * Set whether to hide/show UI elements, like the menu, in fullscreen mode.
	 * <p>
	 * If the value is set to {@code true}, then the related UI elements must be
	 * hidden when fullscreen is activated, and shown again when leaving the
	 * fullscreen mode.
	 *
	 * @param enabled True to enable the extended fullscreen mode, false
	 *                otherwise.
	 */
	public void setExtendedFullscreen(boolean enabled) {
		this.extendedFullscreen.set(enabled);
	}

	/**
	 * Obtain the extended fullscreen mode property.
	 *
	 * @return the extended fullscreen mode property.
	 */
	public BooleanProperty extendedFullscreenProperty() {
		return extendedFullscreen;
	}

	/**
	 * Check whether to enable/disable advanced settings visible in the settings
	 * UI view.
	 *
	 * @return true if the advanced settings mode is enabled, false otherwise.
	 *
	 * @see #setAdvancedUIMode(Boolean)
	 */
	public Boolean getAdvancedUIMode() {
		return advancedUIMode.get();
	}

	/**
	 * Set whether to enable/disable advanced settings visible in the settings
	 * UI view.
	 * <p>
	 * If the value is set to {@code true}, then the advanced settings UI
	 * elements must be visible in the settings UI view, and hidden again when
	 * the advanced settings mode is disabled.
	 *
	 * @param enabled True to enable the advanced settings mode, false
	 *                otherwise.
	 */
	public void setAdvancedUIMode(Boolean enabled) {
		this.advancedUIMode.set(enabled);
	}

	/**
	 * Obtain the advanced settings mode property.
	 *
	 * @return the advanced settings mode property.
	 */
	public BooleanProperty advancedUIModeProperty() {
		return advancedUIMode;
	}

	/**
	 * Obtain the extended drawing area of a page.
	 *
	 * @return the new extended drawing area.
	 *
	 * @see #setExtendPageDimension(Dimension2D)
	 */
	public Dimension2D getExtendPageDimension() {
		return extendPageDimension.get();
	}

	/**
	 * Set the new extended drawing area of a page. The specified dimension
	 * defines how the page is scaled down in order to provide additional blank
	 * drawing area. The width and height of the specified dimension must be in
	 * the range of [0,1].
	 *
	 * @param dimension The new extended page dimension to set.
	 */
	public void setExtendPageDimension(Dimension2D dimension) {
		this.extendPageDimension.set(dimension);
	}

	/**
	 * Obtain the extended page dimension property.
	 *
	 * @return the extended page dimension property.
	 */
	public ObjectProperty<Dimension2D> extendPageDimensionProperty() {
		return extendPageDimension;
	}

	/**
	 * Obtain the list of recently opened slide documents.
	 *
	 * @return the list of recently opened slide documents.
	 *
	 * @see RecentDocument
	 */
	public ObservableList<RecentDocument> getRecentDocuments() {
		return recentDocuments;
	}

	/**
	 * Returns the mapping of a filesystem path to a related context.
	 *
	 * @return The context to path mapping.
	 */
	public ObservableHashMap<String, String> getContextPaths() {
		return contextPaths;
	}

	/**
	 * Obtain the grid configuration containing all grid related properties.
	 *
	 * @return the grid configuration.
	 */
	public GridConfiguration getGridConfig() {
		return gridConfig;
	}

	/**
	 * Obtain the whiteboard configuration containing all whiteboard related
	 * properties.
	 *
	 * @return the whiteboard configuration.
	 */
	public WhiteboardConfiguration getWhiteboardConfig() {
		return whiteboardConfig;
	}

	/**
	 * Obtain the grid display containing all display related properties.
	 *
	 * @return the display configuration.
	 */
	public DisplayConfiguration getDisplayConfig() {
		return displayConfig;
	}

	/**
	 * Obtain the tool configuration containing all tool related properties.
	 *
	 * @return the tool configuration.
	 */
	public ToolConfiguration getToolConfig() {
		return toolConfig;
	}

	/**
	 * Obtain the audio configuration containing all audio related properties.
	 *
	 * @return the audio configuration.
	 */
	public AudioConfiguration getAudioConfig() {
		return audioConfig;
	}

}
