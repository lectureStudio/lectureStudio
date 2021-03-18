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

import org.lecturestudio.core.app.configuration.GridConfiguration;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.view.GridSettingsView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GridSettingsPresenterTest extends PresenterTest {

	private GridSettingsMockView view;


	@BeforeEach
	void setup() {
		GridConfiguration config = context.getConfiguration().getGridConfig();
		config.setColor(Color.BLACK);
		config.setHorizontalLinesInterval(5);
		config.setHorizontalLinesVisible(true);
		config.setVerticalLinesInterval(10);
		config.setVerticalLinesVisible(false);
		config.setShowGridOnDisplays(true);

		view = new GridSettingsMockView();

		GridSettingsPresenter presenter = new GridSettingsPresenter(context, view);
		presenter.initialize();
	}

	@Test
	void testReset() {
		view.resetAction.execute();

		GridConfiguration config = context.getConfiguration().getGridConfig();
		GridConfiguration defaultConfig = new DefaultConfiguration().getGridConfig();

		assertEquals(defaultConfig.getColor(), config.getColor());
		assertEquals(defaultConfig.getHorizontalLinesInterval(), config.getHorizontalLinesInterval());
		assertEquals(defaultConfig.getHorizontalLinesVisible(), config.getHorizontalLinesVisible());
		assertEquals(defaultConfig.getVerticalLinesInterval(), config.getVerticalLinesInterval());
		assertEquals(defaultConfig.getVerticalLinesVisible(), config.getVerticalLinesVisible());
		assertEquals(defaultConfig.getShowGridOnDisplays(), config.getShowGridOnDisplays());
	}



	private static class GridSettingsMockView implements GridSettingsView {

		Action resetAction;


		@Override
		public void setGridColor(ObjectProperty<Color> color) {
			assertEquals(Color.BLACK, color.get());
		}

		@Override
		public void setGridInterval(DoubleProperty interval) {
			assertEquals(10.0, interval.get());
		}

		@Override
		public void setShowGridOnDisplays(BooleanProperty show) {
			assertTrue(show.get());
		}

		@Override
		public void setShowVerticalGridLines(BooleanProperty show) {
			assertFalse(show.get());
		}

		@Override
		public void setShowHorizontalGridLines(BooleanProperty show) {
			assertTrue(show.get());
		}

		@Override
		public void setWhiteboardPage(Page page, PresentationParameter parameter) {

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
