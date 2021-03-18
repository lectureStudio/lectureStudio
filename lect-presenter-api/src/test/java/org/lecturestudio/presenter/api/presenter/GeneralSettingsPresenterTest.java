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
import java.util.Locale;

import org.lecturestudio.core.app.Theme;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.GeneralSettingsView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeneralSettingsPresenterTest extends PresenterTest {

	private GeneralSettingsMockView view;


	@BeforeEach
	void setup() throws Exception {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		config.setTheme(new Theme("test", null));
		config.setLocale(Locale.ROOT);
		config.setStartMaximized(false);
		config.setTabletMode(false);
		config.setSaveDocumentOnClose(true);
		config.setExtendPageDimension(new Dimension2D(0.4, 0.4));
		config.setExtendedFullscreen(true);
		config.setNotifyToRecord(true);
		config.setConfirmStopRecording(true);
		config.setUIControlSize(13);

		view = new GeneralSettingsMockView();

		GeneralSettingsPresenter presenter = new GeneralSettingsPresenter(context, view);
		presenter.initialize();
	}

	@Test
	void testReset() {
		view.resetAction.execute();

		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		assertEquals(defaultConfig.getTheme(), config.getTheme());
		assertEquals(defaultConfig.getLocale(), config.getLocale());
		assertEquals(defaultConfig.getStartMaximized(), config.getStartMaximized());
		assertEquals(defaultConfig.getTabletMode(), config.getTabletMode());
		assertEquals(defaultConfig.getSaveDocumentOnClose(), config.getSaveDocumentOnClose());
		assertEquals(defaultConfig.getExtendedFullscreen(), config.getExtendedFullscreen());
		assertEquals(defaultConfig.getExtendPageDimension(), config.getExtendPageDimension());
		assertEquals(defaultConfig.getUIControlSize(), config.getUIControlSize());
	}



	private static class GeneralSettingsMockView implements GeneralSettingsView {

		Action resetAction;


		@Override
		public void setTheme(ObjectProperty<Theme> theme) {
			assertEquals(new Theme("test", null), theme.get());
		}

		@Override
		public void setThemes(List<Theme> themes) {
			assertNotNull(themes);
		}

		@Override
		public void setLocale(ObjectProperty<Locale> locale) {
			assertEquals(Locale.ROOT, locale.get());
		}

		@Override
		public void setLocales(List<Locale> locales) {
			assertNotNull(locales);
		}

		@Override
		public void setStartMaximized(BooleanProperty maximized) {
			assertFalse(maximized.get());
		}

		@Override
		public void setTabletMode(BooleanProperty tabletMode) {
			assertFalse(tabletMode.get());
		}

		@Override
		public void setSaveAnnotationsOnClose(BooleanProperty saveAnnotations) {
			assertTrue(saveAnnotations.get());
		}

		@Override
		public void setExtendedFullscreen(BooleanProperty extended) {
			assertTrue(extended.get());
		}

		@Override
		public void setExtendPageDimension(ObjectProperty<Dimension2D> dimension) {
			assertEquals(new Dimension2D(0.4, 0.4), dimension.get());
		}

		@Override
		public void setTextSize(DoubleProperty size) {
			assertEquals(13, size.get());
		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnReset(Action action) {
			assertNotNull(action);

			resetAction = action;
		}
	}
}
