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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.lecturestudio.core.app.configuration.DisplayConfiguration;
import org.lecturestudio.core.app.configuration.ScreenConfiguration;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.controller.PresentationController;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.service.DisplayService;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationViewFactory;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.view.DisplaySettingsView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DisplaySettingsPresenterTest extends PresenterTest {

	private static final Screen[] SCREENS = {
		new Screen(0, 0, 1280, 720),
		new Screen(1280, 720, 1920, 1200),
		new Screen(-640, -480, 640, 480)
	};

	private ObservableList<Screen> screens;

	private DisplaySettingsMockView view;


	@BeforeEach
	void setup() {
		screens = new ObservableArrayList<>();
		screens.addAll(List.of(SCREENS));

		DisplayConfiguration config = context.getConfiguration().getDisplayConfig();
		config.setAutostart(true);
		config.setNotifyToActivate(true);
		config.setBackgroundColor(Color.BLACK);

		DisplayService displayService = () -> screens;

		PresentationViewFactory factory = (context, screen) -> null;

		PresentationController presentationController = new PresentationController(context, displayService, factory);

		view = new DisplaySettingsMockView();

		DisplaySettingsPresenter presenter = new DisplaySettingsPresenter(context, view, presentationController);
		presenter.initialize();
	}

	@Test
	void testInit() {
		DisplayConfiguration config = context.getConfiguration().getDisplayConfig();
		int index = 0;

		for (ScreenConfiguration screenConfig : config.getScreens()) {
			assertEquals(SCREENS[index++], screenConfig.getScreen());
		}
	}

	@Test
	void testReset() {
		view.resetAction.execute();

		DisplayConfiguration config = context.getConfiguration().getDisplayConfig();
		DisplayConfiguration defaultConfig = new DefaultConfiguration().getDisplayConfig();

		assertEquals(defaultConfig.getAutostart(), config.getAutostart());
		assertEquals(defaultConfig.getNotifyToActivate(), config.getNotifyToActivate());
		assertEquals(defaultConfig.getBackgroundColor(), config.getBackgroundColor());
		assertEquals(defaultConfig.getScreens(), config.getScreens());
	}



	private static class DisplaySettingsMockView implements DisplaySettingsView {

		private boolean reset = false;

		Action resetAction;


		@Override
		public void setEnableDisplaysOnStart(BooleanProperty enable) {
			assertTrue(enable.get());

			enable.addListener((observable, oldValue, newValue) -> {
				if (reset) {
					assertFalse(enable.get());
				}
			});
		}

		@Override
		public void setNotifyToActivate(BooleanProperty activate) {
			assertTrue(activate.get());

			activate.addListener((observable, oldValue, newValue) -> {
				if (reset) {
					assertTrue(activate.get());
				}
			});
		}

		@Override
		public void setDisplayBackgroundColor(ObjectProperty<Color> color) {
			assertEquals(Color.BLACK, color.get());

			color.addListener((observable, oldValue, newValue) -> {
				if (reset) {
					assertEquals(Color.WHITE, color.get());
				}
			});
		}

		@Override
		public void setScreens(List<ScreenConfiguration> screens) {
			int index = 0;

			for (ScreenConfiguration config : screens) {
				assertEquals(SCREENS[index++], config.getScreen());
			}
		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnReset(Action action) {
			assertNotNull(action);

			resetAction = () -> {
				reset = true;
			};
			resetAction = resetAction.andThen(action);
		}
	}

}
