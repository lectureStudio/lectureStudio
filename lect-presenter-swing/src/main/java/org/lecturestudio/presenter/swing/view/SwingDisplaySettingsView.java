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

package org.lecturestudio.presenter.swing.view;

import java.awt.Container;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.lecturestudio.core.app.configuration.ScreenConfiguration;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.DisplaySettingsView;
import org.lecturestudio.swing.beans.ConvertibleObjectProperty;
import org.lecturestudio.swing.components.ColorChooserButton;
import org.lecturestudio.swing.components.DisplayPanel;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;

@SwingView(name = "display-settings", presenter = org.lecturestudio.presenter.api.presenter.DisplaySettingsPresenter.class)
public class SwingDisplaySettingsView extends JPanel implements DisplaySettingsView {

	private JCheckBox screenPowerPlanCheckBox;

	private JCheckBox autoPresentationCheckBox;

	private JCheckBox notifyToActivateCheckBox;

	private ColorChooserButton colorChooserButton;

	private Container displayContainer;

	private DisplayPanel displayPanel;

	private JButton closeButton;

	private JButton resetButton;


	SwingDisplaySettingsView() {
		super();
	}

	@Override
	public void setScreenPowerPlan(BooleanProperty enablePlan) {
		SwingUtils.bindBidirectional(screenPowerPlanCheckBox, enablePlan);
	}

	@Override
	public void setEnableDisplaysOnStart(BooleanProperty enable) {
		SwingUtils.bindBidirectional(autoPresentationCheckBox, enable);
	}

	@Override
	public void setNotifyToActivate(BooleanProperty activate) {
		SwingUtils.bindBidirectional(notifyToActivateCheckBox, activate);
	}

	@Override
	public void setDisplayBackgroundColor(ObjectProperty<Color> color) {
		SwingUtils.bindBidirectional(colorChooserButton, new ConvertibleObjectProperty<>(color,
				ColorConverter.INSTANCE));
	}

	@Override
	public void setScreens(List<ScreenConfiguration> screens) {
		SwingUtils.invoke(() -> {
			displayContainer.removeAll();

			for (int i = 0; i < screens.size(); i++) {
				ScreenConfiguration screenConfig = screens.get(i);
				Rectangle2D bounds = screenConfig.getScreen().getBounds();

				String id = String.valueOf(i);
				JCheckBox checkBox = new JCheckBox(String.format("%s: %s",
						id, ScreenBoundsConverter.INSTANCE.to(bounds)));
				checkBox.setSelected(screenConfig.getEnabled());

				SwingUtils.bindBidirectional(checkBox, screenConfig.enabledProperty());

				displayContainer.add(checkBox);
			}

			displayPanel.setScreens(screens);

			displayContainer.revalidate();
			displayContainer.repaint();
		});
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}



	/**
	 * Screen bounds of Rectangle2D to String converter.
	 */
	private static class ScreenBoundsConverter implements Converter<Rectangle2D, String> {

		static final ScreenBoundsConverter INSTANCE = new ScreenBoundsConverter();


		@Override
		public String to(Rectangle2D bounds) {
			return String.format("[%d, %d, %d, %d]", (int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
		}

		@Override
		public Rectangle2D from(String value) {
			return new Rectangle2D();
		}

	}
}
