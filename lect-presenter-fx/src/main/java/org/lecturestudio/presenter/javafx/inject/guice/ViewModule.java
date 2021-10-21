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

package org.lecturestudio.presenter.javafx.inject.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.inject.Provider;

import javafx.util.BuilderFactory;

import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.util.AggregateBundle;
import org.lecturestudio.core.view.*;
import org.lecturestudio.javafx.control.*;
import org.lecturestudio.javafx.control.TextBox;
import org.lecturestudio.javafx.guice.FxmlViewLoader;
import org.lecturestudio.javafx.guice.FxmlViewMatcher;
import org.lecturestudio.javafx.view.*;
import org.lecturestudio.javafx.view.builder.DIBuilderFactory;
import org.lecturestudio.presenter.api.view.*;
import org.lecturestudio.presenter.javafx.view.*;

public class ViewModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(BuilderFactory.class).to(DIBuilderFactory.class);
		bind(ViewContextFactory.class).to(DIViewContextFactory.class);

		bind(AboutView.class).to(FxAboutView.class);
		bind(AdjustAudioCaptureLevelView.class).to(FxAdjustAudioCaptureLevelView.class);
		bind(SoundSettingsView.class).to(FxMicrophoneSettingsView.class);
		bind(CameraSettingsView.class).to(FxCameraSettingsView.class);
		bind(CreateBookmarkView.class).to(FxCreateBookmarkView.class);
		bind(CreateQuizView.class).to(FxCreateQuizView.class);
		bind(CreateQuizDefaultOptionView.class).to(FxCreateQuizDefaultOptionView.class);
		bind(CreateQuizNumericOptionView.class).to(FxCreateQuizNumericOptionView.class);
		bind(ConfirmStopRecordingView.class).to(FxConfirmStopRecordingView.class);
		bind(DisplaySettingsView.class).to(FxDisplaySettingsView.class);
		bind(DirectoryChooserView.class).to(FxDirectoryChooserView.class);
		bind(FileChooserView.class).to(FxFileChooserView.class);
		bind(GeneralSettingsView.class).to(FxGeneralSettingsView.class);
		bind(GridSettingsView.class).to(FxGridSettingsView.class);
		bind(GotoBookmarkView.class).to(FxGotoBookmarkView.class);
		bind(MainView.class).to(FxMainView.class);
		bind(MenuView.class).to(FxMenuView.class);
		bind(MessengerWindow.class).to(FxMessengerWindow.class);
		bind(NetworkSettingsView.class).to(FxNetworkSettingsView.class);
		bind(NotificationView.class).to(FxNotificationView.class);
		bind(NotificationPopupView.class).to(FxNotificationPopupView.class);
		bind(NotificationPopupManager.class).to(FxNotificationPopupManager.class);
		bind(ProgressView.class).to(FxProgressView.class);
		bind(QuitRecordingView.class).to(FxQuitRecordingView.class);
		bind(RecordSettingsView.class).to(FxRecordSettingsView.class);
		bind(RestoreRecordingView.class).to(FxRestoreRecordingView.class);
		bind(SaveDocumentsView.class).to(FxSaveDocumentsView.class);
		bind(SaveDocumentOptionView.class).to(FxSaveDocumentOptionView.class);
		bind(SaveQuizResultsView.class).to(FxSaveQuizResultsView.class);
		bind(SaveRecordingView.class).to(FxSaveRecordingView.class);
		bind(SettingsView.class).to(FxSettingsView.class);
		bind(SelectQuizView.class).to(FxSelectQuizView.class);
		bind(SlidesView.class).to(FxSlidesView.class);
		bind(SlideViewAddressOverlay.class).to(FxSlideViewAddressOverlay.class);
		bind(StartView.class).to(FxStartView.class);
		bind(StreamSettingsView.class).to(FxStreamSettingsView.class);
		bind(TextBoxView.class).to(TextBox.class);
		bind(TeXBoxView.class).to(TeXBox.class);
		bind(ToolbarView.class).to(FxToolbarView.class);
		bind(ToolSettingsView.class).to(FxToolSettingsView.class);
		bind(WebServiceSettingsView.class).to(FxWebServiceSettingsView.class);
		bind(WhiteboardSettingsView.class).to(FxWhiteboardSettingsView.class);

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
