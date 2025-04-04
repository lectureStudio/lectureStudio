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

package org.lecturestudio.editor.javafx.inject.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javafx.scene.text.Font;
import javafx.util.BuilderFactory;

import javax.inject.Provider;

import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.util.AggregateBundle;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.AboutView;
import org.lecturestudio.core.view.ConfirmationNotificationView;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.NewVersionView;
import org.lecturestudio.core.view.NotificationPopupManager;
import org.lecturestudio.core.view.NotificationPopupView;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.core.view.ProgressDialogView;
import org.lecturestudio.core.view.ProgressView;
import org.lecturestudio.core.view.TextBoxView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.view.*;
import org.lecturestudio.editor.javafx.view.*;
import org.lecturestudio.javafx.control.TextBox;
import org.lecturestudio.javafx.guice.FxmlViewLoader;
import org.lecturestudio.javafx.guice.FxmlViewMatcher;
import org.lecturestudio.javafx.view.FxConfirmationNotificationView;
import org.lecturestudio.javafx.view.FxDirectoryChooserView;
import org.lecturestudio.javafx.view.FxFileChooserView;
import org.lecturestudio.javafx.view.FxNewVersionView;
import org.lecturestudio.javafx.view.FxNotificationPopupManager;
import org.lecturestudio.javafx.view.FxNotificationPopupView;
import org.lecturestudio.javafx.view.FxNotificationView;
import org.lecturestudio.javafx.view.FxProgressDialogView;
import org.lecturestudio.javafx.view.FxProgressView;
import org.lecturestudio.javafx.view.builder.DIBuilderFactory;

public class ViewModule extends AbstractModule {

	@Override
	protected void configure() {
		try {
			loadFonts();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		bind(BuilderFactory.class).to(DIBuilderFactory.class);
		bind(ViewContextFactory.class).to(DIViewContextFactory.class);

		bind(DirectoryChooserView.class).to(FxDirectoryChooserView.class);
		bind(FileChooserView.class).to(FxFileChooserView.class);
		bind(NotificationView.class).to(FxNotificationView.class);
		bind(ConfirmationNotificationView.class).to(FxConfirmationNotificationView.class);
		bind(NotificationPopupView.class).to(FxNotificationPopupView.class);
		bind(NotificationPopupManager.class).to(FxNotificationPopupManager.class);

		bind(AboutView.class).to(FxAboutView.class);
		bind(ActionsSettingsView.class).to(FxActionsSettingsView.class);
		bind(AudioEffectsView.class).to(FxAudioEffectsView.class);
		bind(GeneralSettingsView.class).to(FxGeneralSettingsView.class);
		bind(ImportRecordingView.class).to(FxImportRecordingView.class);
		bind(InsertPageView.class).to(FxInsertPageView.class);
		bind(MainView.class).to(FxMainView.class);
		bind(MediaControlsView.class).to(FxMediaControlsView.class);
		bind(MediaTrackControlsView.class).to(FxMediaTrackControlsView.class);
		bind(MediaTracksView.class).to(FxMediaTracksView.class);
		bind(MenuView.class).to(FxMenuView.class);
		bind(NewVersionView.class).to(FxNewVersionView.class);
		bind(NoiseReductionProgressView.class).to(FxNoiseReductionProgressView.class);
		bind(NoiseReductionSettingsView.class).to(FxNoiseReductionSettingsView.class);
		bind(LoudnessNormalizeView.class).to(FxLoudnessNormalizeView.class);
		bind(PageEventsView.class).to(FxPageEventsView.class);
		bind(ProgressView.class).to(FxProgressView.class);
		bind(ProgressDialogView.class).to(FxProgressDialogView.class);
		bind(QuitSaveRecordingView.class).to(FxQuitSaveRecordingView.class);
		bind(ReplacePageView.class).to(FxReplacePageView.class);
		bind(SplitRecordingView.class).to(FxSplitRecordingView.class);
		bind(SettingsView.class).to(FxSettingsView.class);
		bind(SlidesView.class).to(FxSlidesView.class);
		bind(SoundSettingsView.class).to(FxSoundSettingsView.class);
		bind(StartView.class).to(FxStartView.class);
		bind(ToolbarView.class).to(FxToolbarView.class);
		bind(TextBoxView.class).to(TextBox.class);
		bind(VideoExportView.class).to(FxVideoExportView.class);
		bind(VideoExportProgressView.class).to(FxVideoExportProgressView.class);
		bind(VideoExportSettingsView.class).to(FxVideoExportSettingsView.class);
		bind(VideoSettingsView.class).to(FxVideoSettingsView.class);

		Provider<AggregateBundle> resourceProvider = getProvider(AggregateBundle.class);
		Provider<BuilderFactory> builderProvider = getProvider(BuilderFactory.class);

		bindListener(new FxmlViewMatcher(), new TypeListener() {

			@Override
			public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
				encounter.register(FxmlViewLoader.getInstance(resourceProvider, builderProvider));
			}

		});
	}

	private void loadFonts() throws Exception {
		String[] fontFiles = FileUtils.getResourceListing("/resources/fonts", (name) -> name.endsWith(".ttf"));

		for (String file : fontFiles) {
			Font.loadFont(getClass().getResourceAsStream(file), Font.getDefault().getSize());
		}
	}
}
