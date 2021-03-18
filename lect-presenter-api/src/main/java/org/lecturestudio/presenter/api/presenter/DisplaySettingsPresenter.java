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

package org.lecturestudio.presenter.api.presenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.DisplayConfiguration;
import org.lecturestudio.core.app.configuration.ScreenConfiguration;
import org.lecturestudio.core.controller.PresentationController;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.view.DisplaySettingsView;

public class DisplaySettingsPresenter extends Presenter<DisplaySettingsView> {

	private final DisplayConfiguration displayConfig;

	private final PresentationController presentationController;


	@Inject
	DisplaySettingsPresenter(ApplicationContext context, DisplaySettingsView view, PresentationController presentationController) {
		super(context, view);

		this.presentationController = presentationController;
		this.displayConfig = context.getConfiguration().getDisplayConfig();
	}

	@Override
	public void initialize() {
		ObservableList<Screen> screens = presentationController.getScreens();
		screens.addListener(new ListChangeListener<>() {

			@Override
			public void listItemsInserted(ObservableList<Screen> list, int startIndex, int itemCount) {
				listChanged(list);
				screensConnected(list, startIndex, itemCount);
			}

			@Override
			public void listChanged(ObservableList<Screen> list) {
				view.setScreens(loadScreenList(list));
			}
		});

		screensConnected(screens, 0, screens.size());

		view.setDisplayBackgroundColor(displayConfig.backgroundColorProperty());
		view.setEnableDisplaysOnStart(displayConfig.autostartProperty());
		view.setScreens(loadScreenList(screens));
		view.setOnReset(this::reset);
	}

	private void reset() {
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		displayConfig.setAutostart(defaultConfig.getDisplayConfig().getAutostart());
		displayConfig.setBackgroundColor(defaultConfig.getDisplayConfig().getBackgroundColor());
		displayConfig.getScreens().clear();
	}

	private void screensConnected(ObservableList<Screen> list, int startIndex, int itemCount) {
		for (int i = startIndex; i < startIndex + itemCount; i++) {
			Screen screen = list.get(i);
			addScreenToConfig(screen);
		}
	}

	private void addScreenToConfig(Screen screen) {
		List<ScreenConfiguration> screens = displayConfig.getScreens();

		for (ScreenConfiguration screenConfig : screens) {
			if (screenConfig.getScreen().equals(screen)) {
				// No duplicates.
				return;
			}
		}

		ScreenConfiguration screenConfig = new ScreenConfiguration();
		screenConfig.setScreen(screen);
		screenConfig.setEnabled(true);

		screens.add(screenConfig);
	}

	private void updateConfigValue(Screen screen, boolean enable) {
		List<ScreenConfiguration> screens = displayConfig.getScreens();

		for (ScreenConfiguration screenConfig : screens) {
			if (screenConfig.getScreen().equals(screen)) {
				screenConfig.setEnabled(enable);
				return;
			}
		}
	}

	private List<ScreenConfiguration> loadScreenList(List<Screen> screens) {
		List<ScreenConfiguration> configScreens = displayConfig.getScreens();
		List<ScreenConfiguration> screenConfigs = new ArrayList<>();

		for (Screen screen : screens) {
			ScreenConfiguration screenConfig = new ScreenConfiguration();
			screenConfig.setScreen(screen);
			screenConfig.setEnabled(getScreenEnabled(configScreens, screen));
			screenConfig.enabledProperty().addListener((observable, oldValue, newValue) -> {
				// Update config first.
				updateConfigValue(screen, newValue);

				presentationController.showPresentationView(screen, newValue);
			});

			screenConfigs.add(screenConfig);
		}

		return screenConfigs;
	}

	private boolean getScreenEnabled(List<ScreenConfiguration> screenConfigs, Screen screen) {
		for (ScreenConfiguration screenConfig : screenConfigs) {
			if (screenConfig.getScreen().equals(screen)) {
				return screenConfig.getEnabled();
			}
		}

		// Enable by default.
		return true;
	}
}