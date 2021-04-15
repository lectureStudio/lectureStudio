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

import java.text.MessageFormat;
import java.util.Locale;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.LocaleProvider;
import org.lecturestudio.core.app.ThemeProvider;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.GeneralSettingsView;

public class GeneralSettingsPresenter extends Presenter<GeneralSettingsView> {

	private Locale initialLocale;


	@Inject
	GeneralSettingsPresenter(ApplicationContext context, GeneralSettingsView view) {
		super(context, view);
	}

	@Override
	public void initialize() throws Exception {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		LocaleProvider localeProvider = new LocaleProvider();
		ThemeProvider themeProvider = new ThemeProvider();

		initialLocale = config.getLocale();

		view.setLocales(localeProvider.getLocales());
		view.setLocale(config.localeProperty());
		view.setThemes(themeProvider.getThemes());
		view.setTheme(config.themeProperty());
		view.setStartMaximized(config.startMaximizedProperty());
		view.setStartFullscreen(config.startFullscreenProperty());
		view.setTabletMode(config.tabletModeProperty());
		view.setSaveAnnotationsOnClose(config.saveDocOnCloseProperty());
		view.setExtendPageDimension(config.extendPageDimensionProperty());
		view.setExtendedFullscreen(config.extendedFullscreenProperty());
		view.setTextSize(config.uiControlSizeProperty());
		view.setOnClose(this::close);
		view.setOnReset(this::reset);
	}

	@Override
	public void close() {
		super.close();

		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();

		if (!initialLocale.equals(config.getLocale())) {
			showNotification(NotificationType.WARNING,
					dict.get("general.settings.language.notify.title"),
					MessageFormat.format(
							dict.get("general.settings.language.notify.message"),
							config.getApplicationName()));
		}
	}

	private void reset() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		config.setTheme(defaultConfig.getTheme());
		config.setLocale(defaultConfig.getLocale());
		config.setStartMaximized(defaultConfig.getStartMaximized());
		config.setStartFullscreen(defaultConfig.getStartFullscreen());
		config.setTabletMode(defaultConfig.getTabletMode());
		config.setSaveDocumentOnClose(defaultConfig.getSaveDocumentOnClose());
		config.setExtendedFullscreen(defaultConfig.getExtendedFullscreen());
		config.setExtendPageDimension(defaultConfig.getExtendPageDimension());
		config.setUIControlSize(defaultConfig.getUIControlSize());
	}
}