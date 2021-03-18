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

package org.lecturestudio.editor.api.presenter;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.LocaleProvider;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.editor.api.config.DefaultConfiguration;
import org.lecturestudio.editor.api.view.GeneralSettingsView;

public class GeneralSettingsPresenter extends Presenter<GeneralSettingsView> {

	@Inject
	GeneralSettingsPresenter(ApplicationContext context, GeneralSettingsView view) {
		super(context, view);
	}

	@Override
	public void initialize() throws Exception {
		Configuration config = context.getConfiguration();
		LocaleProvider localeProvider = new LocaleProvider();

		view.setLocales(localeProvider.getLocales());
		view.setLocale(config.localeProperty());
		view.setStartMaximized(config.startMaximizedProperty());
		view.setExtendedFullscreen(config.extendedFullscreenProperty());
		view.setTextSize(config.uiControlSizeProperty());
		view.setOnReset(this::reset);
	}

	private void reset() {
		Configuration config = context.getConfiguration();
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		config.setTheme(defaultConfig.getTheme());
		config.setLocale(defaultConfig.getLocale());
		config.setStartMaximized(defaultConfig.getStartMaximized());
		config.setTabletMode(defaultConfig.getTabletMode());
		config.setExtendPageDimension(defaultConfig.getExtendPageDimension());
		config.setUIControlSize(defaultConfig.getUIControlSize());
	}
}