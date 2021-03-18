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

import static java.util.Objects.nonNull;

import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTabbedPane;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.SettingsBaseView;
import org.lecturestudio.presenter.api.view.SettingsView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "main-settings")
public class SwingSettingsView extends JTabbedPane implements SettingsView {

	private final BooleanProperty extendedSettings;

	private final Set<Component> extendedComponents;


	SwingSettingsView() {
		super();

		extendedSettings = new BooleanProperty();
		extendedComponents = new HashSet<>();

		initialize();
	}

	@Override
	public void setAdvancedSettings(boolean selected) {
		SwingUtils.invoke(() -> {
			extendedSettings.set(selected);
			showAdvancedSettings(selected);
		});
	}

	@Override
	public void setSettingsPath(String path) {
		for (int i = 0; i < getTabCount(); i++) {
			Component tab = getTabComponentAt(i);

			if (tab.getName().equals(path)) {
				setSelectedIndex(i);
				break;
			}
		}
	}

	@Override
	public void setOnClose(Action action) {
		for (int i = 0; i < getTabCount(); i++) {
			Component content = getComponentAt(i);

			if (nonNull(content) && SettingsBaseView.class.isAssignableFrom(content.getClass())) {
				SettingsBaseView settingsView = (SettingsBaseView) content;
				settingsView.setOnClose(action);
			}
		}
	}

	private void showAdvancedSettings(boolean show) {
		for (Component component : extendedComponents) {
			component.setVisible(show);
		}

		revalidate();
		repaint();
	}

	@ViewPostConstruct
	private void initialize() {
		registerExtended(this);

		showAdvancedSettings(extendedSettings.get());
	}

	private void registerExtended(Component component) {
		String name = component.getName();

		if (nonNull(name) && name.equals("extendedUI")) {
			extendedComponents.add(component);
		}
		if (component instanceof Container) {
			Container container = (Container) component;

			for (Component c : container.getComponents()) {
				registerExtended(c);
			}
		}
	}
}
