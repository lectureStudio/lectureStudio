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
import org.lecturestudio.core.view.AboutView;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.NotificationPopupManager;
import org.lecturestudio.core.view.NotificationPopupView;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.core.view.ProgressView;
import org.lecturestudio.core.view.SlideViewAddressOverlay;
import org.lecturestudio.core.view.TeXBoxView;
import org.lecturestudio.core.view.TextBoxView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.view.AdjustAudioCaptureLevelView;
import org.lecturestudio.presenter.api.view.CameraSettingsView;
import org.lecturestudio.presenter.api.view.ConfirmStopRecordingView;
import org.lecturestudio.presenter.api.view.CreateBookmarkView;
import org.lecturestudio.presenter.api.view.CreateQuizDefaultOptionView;
import org.lecturestudio.presenter.api.view.CreateQuizNumericOptionView;
import org.lecturestudio.presenter.api.view.CreateQuizView;
import org.lecturestudio.presenter.api.view.DisplaySettingsView;
import org.lecturestudio.presenter.api.view.GeneralSettingsView;
import org.lecturestudio.presenter.api.view.GotoBookmarkView;
import org.lecturestudio.presenter.api.view.GridSettingsView;
import org.lecturestudio.presenter.api.view.MainView;
import org.lecturestudio.presenter.api.view.MenuView;
import org.lecturestudio.presenter.api.view.MessengerWindow;
import org.lecturestudio.presenter.api.view.ReconnectStreamView;
import org.lecturestudio.presenter.api.view.SoundSettingsView;
import org.lecturestudio.core.view.NewVersionView;
import org.lecturestudio.presenter.api.view.QuitRecordingView;
import org.lecturestudio.presenter.api.view.RecordSettingsView;
import org.lecturestudio.presenter.api.view.RestoreRecordingView;
import org.lecturestudio.presenter.api.view.SaveDocumentOptionView;
import org.lecturestudio.presenter.api.view.SaveDocumentsView;
import org.lecturestudio.presenter.api.view.SaveQuizResultsView;
import org.lecturestudio.presenter.api.view.SaveRecordingView;
import org.lecturestudio.presenter.api.view.SelectQuizView;
import org.lecturestudio.presenter.api.view.SettingsView;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.presenter.api.view.StartCourseFeatureView;
import org.lecturestudio.presenter.api.view.StartStreamView;
import org.lecturestudio.presenter.api.view.StartView;
import org.lecturestudio.presenter.api.view.StreamSettingsView;
import org.lecturestudio.presenter.api.view.ToolSettingsView;
import org.lecturestudio.presenter.api.view.ToolbarView;
import org.lecturestudio.presenter.api.view.WhiteboardSettingsView;
import org.lecturestudio.presenter.swing.view.SwingAboutView;
import org.lecturestudio.presenter.swing.view.SwingAdjustAudioCaptureLevelView;
import org.lecturestudio.presenter.swing.view.SwingCameraSettingsView;
import org.lecturestudio.presenter.swing.view.SwingConfirmStopRecordingView;
import org.lecturestudio.presenter.swing.view.SwingCreateBookmarkView;
import org.lecturestudio.presenter.swing.view.SwingCreateQuizView;
import org.lecturestudio.presenter.swing.view.SwingDisplaySettingsView;
import org.lecturestudio.presenter.swing.view.SwingGeneralSettingsView;
import org.lecturestudio.presenter.swing.view.SwingGotoBookmarkView;
import org.lecturestudio.presenter.swing.view.SwingGridSettingsView;
import org.lecturestudio.presenter.swing.view.SwingMainView;
import org.lecturestudio.presenter.swing.view.SwingMenuView;
import org.lecturestudio.presenter.swing.view.SwingMessengerWindow;
import org.lecturestudio.presenter.swing.view.SwingReconnectStreamView;
import org.lecturestudio.presenter.swing.view.SwingSoundSettingsView;
import org.lecturestudio.presenter.swing.view.SwingNewVersionView;
import org.lecturestudio.presenter.swing.view.SwingQuitRecordingView;
import org.lecturestudio.presenter.swing.view.SwingQuizDefaultOptionView;
import org.lecturestudio.presenter.swing.view.SwingQuizNumericOptionView;
import org.lecturestudio.presenter.swing.view.SwingRecordSettingsView;
import org.lecturestudio.presenter.swing.view.SwingRestoreRecordingView;
import org.lecturestudio.presenter.swing.view.SwingSaveDocumentOptionView;
import org.lecturestudio.presenter.swing.view.SwingSaveDocumentsView;
import org.lecturestudio.presenter.swing.view.SwingSaveQuizResultsView;
import org.lecturestudio.presenter.swing.view.SwingSaveRecordingView;
import org.lecturestudio.presenter.swing.view.SwingSelectQuizView;
import org.lecturestudio.presenter.swing.view.SwingSettingsView;
import org.lecturestudio.presenter.swing.view.SwingSlideViewAddressOverlay;
import org.lecturestudio.presenter.swing.view.SwingSlidesView;
import org.lecturestudio.presenter.swing.view.SwingStartCourseFeatureView;
import org.lecturestudio.presenter.swing.view.SwingStartStreamView;
import org.lecturestudio.presenter.swing.view.SwingStartView;
import org.lecturestudio.presenter.swing.view.SwingStreamSettingsView;
import org.lecturestudio.presenter.swing.view.SwingToolSettingsView;
import org.lecturestudio.presenter.swing.view.SwingToolbarView;
import org.lecturestudio.presenter.swing.view.SwingWhiteboardSettingsView;
import org.lecturestudio.swing.view.SwingDirectoryChooserView;
import org.lecturestudio.swing.view.SwingFileChooserView;
import org.lecturestudio.swing.view.SwingNotificationPopupManager;
import org.lecturestudio.swing.view.SwingNotificationPopupView;
import org.lecturestudio.swing.view.SwingNotificationView;
import org.lecturestudio.swing.view.SwingProgressView;
import org.lecturestudio.swing.view.SwingTeXBoxView;
import org.lecturestudio.swing.view.SwingTextBoxView;

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
		bind(DirectoryChooserView.class).to(SwingDirectoryChooserView.class);
		bind(FileChooserView.class).to(SwingFileChooserView.class);
		bind(GeneralSettingsView.class).to(SwingGeneralSettingsView.class);
		bind(GridSettingsView.class).to(SwingGridSettingsView.class);
		bind(GotoBookmarkView.class).to(SwingGotoBookmarkView.class);
		bind(MainView.class).to(SwingMainView.class);
		bind(MenuView.class).to(SwingMenuView.class);
		bind(MessengerWindow.class).to(SwingMessengerWindow.class);
		bind(SoundSettingsView.class).to(SwingSoundSettingsView.class);
		bind(NewVersionView.class).to(SwingNewVersionView.class);
		bind(NotificationView.class).to(SwingNotificationView.class);
		bind(NotificationPopupView.class).to(SwingNotificationPopupView.class);
		bind(NotificationPopupManager.class).to(SwingNotificationPopupManager.class);
		bind(ProgressView.class).to(SwingProgressView.class);
		bind(QuitRecordingView.class).to(SwingQuitRecordingView.class);
		bind(ReconnectStreamView.class).to(SwingReconnectStreamView.class);
		bind(RecordSettingsView.class).to(SwingRecordSettingsView.class);
		bind(RestoreRecordingView.class).to(SwingRestoreRecordingView.class);
		bind(SaveDocumentsView.class).to(SwingSaveDocumentsView.class);
		bind(SaveDocumentOptionView.class).to(SwingSaveDocumentOptionView.class);
		bind(SaveQuizResultsView.class).to(SwingSaveQuizResultsView.class);
		bind(SaveRecordingView.class).to(SwingSaveRecordingView.class);
		bind(SettingsView.class).to(SwingSettingsView.class);
		bind(SelectQuizView.class).to(SwingSelectQuizView.class);
		bind(SlidesView.class).to(SwingSlidesView.class);
		bind(SlideViewAddressOverlay.class).to(SwingSlideViewAddressOverlay.class);
		bind(StartCourseFeatureView.class).to(SwingStartCourseFeatureView.class);
		bind(StartView.class).to(SwingStartView.class);
		bind(StartStreamView.class).to(SwingStartStreamView.class);
		bind(StreamSettingsView.class).to(SwingStreamSettingsView.class);
		bind(TextBoxView.class).to(SwingTextBoxView.class);
		bind(TeXBoxView.class).to(SwingTeXBoxView.class);
		bind(ToolbarView.class).to(SwingToolbarView.class);
		bind(ToolSettingsView.class).to(SwingToolSettingsView.class);
		bind(WhiteboardSettingsView.class).to(SwingWhiteboardSettingsView.class);
	}
}
