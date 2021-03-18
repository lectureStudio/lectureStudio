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

import static java.util.Objects.nonNull;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.view.SettingsView;

public class SettingsPresenter extends Presenter<SettingsView> {

	@Inject
	SettingsPresenter(ApplicationContext context, SettingsView view) {
		super(context, view);
	}

	public void setSettingsPath(String path) {
		if (nonNull(path)) {
			view.setSettingsPath(path);
		}
	}

	@Override
	public void initialize() {
		setOnClose(() -> {
			try {
				context.saveConfiguration();
			}
			catch (Exception e) {
				logException(e, "Save configuration failed");
			}
		});

		context.getConfiguration().advancedUIModeProperty().addListener((observable, oldValue, newValue) -> {
			view.setAdvancedSettings(newValue);
		});

		view.setAdvancedSettings(context.getConfiguration().getAdvancedUIMode());
		view.setOnClose(this::close);
	}

	@Override
	public boolean cache() {
		return true;
	}
}
