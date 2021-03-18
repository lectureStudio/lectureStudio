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

package org.lecturestudio.player.javafx.inject.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.inject.Provider;

import javafx.util.BuilderFactory;

import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.util.AggregateBundle;
import org.lecturestudio.core.view.AboutView;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.NotificationPopupManager;
import org.lecturestudio.core.view.NotificationPopupView;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.javafx.guice.FxmlViewLoader;
import org.lecturestudio.javafx.guice.FxmlViewMatcher;
import org.lecturestudio.javafx.view.FxDirectoryChooserView;
import org.lecturestudio.javafx.view.FxFileChooserView;
import org.lecturestudio.javafx.view.FxNotificationPopupManager;
import org.lecturestudio.javafx.view.FxNotificationPopupView;
import org.lecturestudio.javafx.view.FxNotificationView;
import org.lecturestudio.javafx.view.builder.DIBuilderFactory;
import org.lecturestudio.player.api.view.MainView;
import org.lecturestudio.player.api.view.MediaControlsView;
import org.lecturestudio.player.api.view.MenuView;
import org.lecturestudio.player.api.view.SlidesView;
import org.lecturestudio.player.api.view.StartView;
import org.lecturestudio.player.javafx.view.FxAboutView;
import org.lecturestudio.player.javafx.view.FxMainView;
import org.lecturestudio.player.javafx.view.FxMediaControlsView;
import org.lecturestudio.player.javafx.view.FxMenuView;
import org.lecturestudio.player.javafx.view.FxSlidesView;
import org.lecturestudio.player.javafx.view.FxStartView;

public class ViewModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(BuilderFactory.class).to(DIBuilderFactory.class);
		bind(ViewContextFactory.class).to(DIViewContextFactory.class);

		bind(DirectoryChooserView.class).to(FxDirectoryChooserView.class);
		bind(FileChooserView.class).to(FxFileChooserView.class);
		bind(NotificationView.class).to(FxNotificationView.class);
		bind(NotificationPopupView.class).to(FxNotificationPopupView.class);
		bind(NotificationPopupManager.class).to(FxNotificationPopupManager.class);

		bind(AboutView.class).to(FxAboutView.class);
		bind(MainView.class).to(FxMainView.class);
		bind(MediaControlsView.class).to(FxMediaControlsView.class);
		bind(MenuView.class).to(FxMenuView.class);
		bind(SlidesView.class).to(FxSlidesView.class);
		bind(StartView.class).to(FxStartView.class);

		Provider<AggregateBundle> resourceProvider = getProvider(AggregateBundle.class);
		Provider<BuilderFactory> builderProvider = getProvider(BuilderFactory.class);

		bindListener(new FxmlViewMatcher(), new TypeListener() {

			@Override
			public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
				encounter.register(FxmlViewLoader.getInstance(resourceProvider, builderProvider));
			}

		});
	}
}
