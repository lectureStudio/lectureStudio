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

package org.lecturestudio.presenter.swing.inject.guice;

import com.google.inject.AbstractModule;

import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.view.*;
import org.lecturestudio.presenter.api.view.*;
import org.lecturestudio.presenter.swing.view.*;
import org.lecturestudio.swing.view.*;

public class ViewModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ViewContextFactory.class).to(DIViewContextFactory.class);

		bind(AboutView.class).to(SwingAboutView.class);
		bind(AdjustAudioCaptureLevelView.class).to(SwingAdjustAudioCaptureLevelView.class);
		bind(CameraSettingsView.class).to(SwingCameraSettingsView.class);
		bind(CreateBookmarkView.class).to(SwingCreateBookmarkView.class);
		bind(CreateQuizView.class).to(SwingCreateQuizView.class);
		bind(CreateQuizDefaultOptionView.class).to(SwingQuizDefaultOptionView.class);
		bind(CreateQuizNumericOptionView.class).to(SwingQuizNumericOptionView.class);
		bind(ConfirmStopRecordingView.class).to(SwingConfirmStopRecordingView.class);
		bind(DisplaySettingsView.class).to(SwingDisplaySettingsView.class);
		bind(DirectoryChooserView.class).to(SwingFxDirectoryChooserView.class);
		bind(DocumentTemplateSettingsView.class).to(SwingDocumentTemplateSettingsView.class);
		bind(FileChooserView.class).to(SwingFxFileChooserView.class);
		bind(GeneralSettingsView.class).to(SwingGeneralSettingsView.class);
		bind(GotoBookmarkView.class).to(SwingGotoBookmarkView.class);
		bind(MainView.class).to(SwingMainView.class);
		bind(MenuView.class).to(SwingMenuView.class);
		bind(MessengerWindow.class).to(SwingMessengerWindow.class);
		bind(SoundSettingsView.class).to(SwingSoundSettingsView.class);
		bind(NewVersionView.class).to(SwingNewVersionView.class);
		bind(NotificationView.class).to(SwingNotificationView.class);
		bind(NotificationPopupView.class).to(SwingNotificationPopupView.class);
		bind(NotificationPopupManager.class).to(SwingNotificationPopupManager.class);
		bind(PreviewStreamView.class).to(SwingPreviewStreamView.class);
		bind(ProgressView.class).to(SwingProgressView.class);
		bind(QuitRecordingView.class).to(SwingQuitRecordingView.class);
		bind(ReconnectStreamView.class).to(SwingReconnectStreamView.class);
		bind(RecordSettingsView.class).to(SwingRecordSettingsView.class);
		bind(RemindDisplayActivationView.class).to(SwingRemindDisplayActivationView.class);
		bind(RemindRecordingView.class).to(SwingRemindRecordingView.class);
		bind(RestoreRecordingView.class).to(SwingRestoreRecordingView.class);
		bind(SaveDocumentsView.class).to(SwingSaveDocumentsView.class);
		bind(SaveDocumentOptionView.class).to(SwingSaveDocumentOptionView.class);
		bind(SaveRecordingView.class).to(SwingSaveRecordingView.class);
		bind(ShortcutsView.class).to(SwingShortcutsView.class);
		bind(SettingsView.class).to(SwingSettingsView.class);
		bind(SelectQuizView.class).to(SwingSelectQuizView.class);
		bind(SlidesView.class).to(SwingSlidesView.class);
		bind(SlideViewAddressOverlay.class).to(SwingSlideViewAddressOverlay.class);
		bind(StartCourseFeatureView.class).to(SwingStartCourseFeatureView.class);
		bind(StartView.class).to(SwingStartView.class);
		bind(StartRecordingView.class).to(SwingStartRecordingView.class);
		bind(StartScreenSharingView.class).to(SwingStartScreenSharingView.class);
		bind(StartStreamView.class).to(SwingStartStreamView.class);
		bind(StreamView.class).to(SwingStreamView.class);
		bind(StreamSettingsView.class).to(SwingStreamSettingsView.class);
		bind(StopwatchConfigView.class).to(SwingStopwatchConfigView.class);
		bind(TextBoxView.class).to(SwingTextBoxView.class);
		bind(TeXBoxView.class).to(SwingTeXBoxView.class);
		bind(ToolbarView.class).to(SwingToolbarView.class);
		bind(ToolSettingsView.class).to(SwingToolSettingsView.class);
		bind(WhiteboardSettingsView.class).to(SwingWhiteboardSettingsView.class);
	}
}
