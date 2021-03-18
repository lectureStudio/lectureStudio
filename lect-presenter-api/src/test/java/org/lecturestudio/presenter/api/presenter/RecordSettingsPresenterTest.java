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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.RecordSettingsView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecordSettingsPresenterTest extends PresenterTest {

	@BeforeEach
	void setup() {
		AudioConfiguration config = context.getConfiguration().getAudioConfig();
		config.setInputDeviceName("dummy");
		config.setSoundSystem("dummy");
		config.setRecordingPath(config.getRecordingPath());
		config.setRecordingFormat(new AudioFormat(AudioFormat.Encoding.S32LE, 32000, 4));
	}

	@Test
	void testInit() {
		AtomicReference<AudioFormat> formatRef = new AtomicReference<>();
		AtomicReference<String> pathRef = new AtomicReference<>();

		RecordSettingsMockView view = new RecordSettingsMockView() {
			@Override
			public void setRecordingAudioFormat(ObjectProperty<AudioFormat> audioFormat) {
				formatRef.set(audioFormat.get());
			}

			@Override
			public void setRecordingPath(StringProperty path) {
				pathRef.set(path.get());
			}
		};

		RecordSettingsPresenter presenter = new RecordSettingsPresenter(context, view, viewFactory);
		presenter.initialize();

		AudioConfiguration config = context.getConfiguration().getAudioConfig();

		assertEquals(config.getRecordingFormat(), formatRef.get());
		assertEquals(config.getRecordingPath(), pathRef.get());
	}

	@Test
	void testReset() {
		RecordSettingsMockView view = new RecordSettingsMockView();

		RecordSettingsPresenter presenter = new RecordSettingsPresenter(context, view, viewFactory);
		presenter.initialize();

		view.resetAction.execute();

		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		assertEquals(defaultConfig.getNotifyToRecord(), config.getNotifyToRecord());
		assertEquals(defaultConfig.getConfirmStopRecording(), config.getConfirmStopRecording());
		assertEquals(defaultConfig.getAudioConfig().getRecordingPath(), config.getAudioConfig().getRecordingPath());
		assertEquals(defaultConfig.getAudioConfig().getRecordingFormat(), config.getAudioConfig().getRecordingFormat());
	}

	@Test
	void testSelectPath() {
		AudioConfiguration config = context.getConfiguration().getAudioConfig();
		AtomicReference<DirectoryChooserMockView> chooserRef = new AtomicReference<>();

		viewFactory = new ViewContextMockFactory() {
			@Override
			public DirectoryChooserView createDirectoryChooserView() {
				DirectoryChooserMockView view = (DirectoryChooserMockView) super.createDirectoryChooserView();
				chooserRef.set(view);
				return view;
			}
		};

		RecordSettingsMockView view = new RecordSettingsMockView();

		RecordSettingsPresenter presenter = new RecordSettingsPresenter(context, view, viewFactory);
		presenter.initialize();

		view.selectPathAction.execute();

		assertNotNull(chooserRef.get());
		assertEquals(new File(config.getRecordingPath()), chooserRef.get().directory);
		assertEquals(view, chooserRef.get().parent);
	}



	private static class RecordSettingsMockView implements RecordSettingsView {

		Action resetAction;

		Action selectPathAction;


		@Override
		public void setNotifyToRecord(BooleanProperty notify) {
			assertFalse(notify.get());
		}

		@Override
		public void setConfirmStopRecording(BooleanProperty confirm) {
			assertTrue(confirm.get());
		}

		@Override
		public void setPageRecordingTimeout(IntegerProperty timeout) {

		}

		@Override
		public void setRecordingAudioFormat(ObjectProperty<AudioFormat> audioFormat) {

		}

		@Override
		public void setRecordingAudioFormats(List<AudioFormat> formats) {

		}

		@Override
		public void setRecordingPath(StringProperty path) {

		}

		@Override
		public void setOnSelectRecordingPath(Action action) {
			assertNotNull(action);

			selectPathAction = action;
		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnReset(Action action) {
			assertNotNull(action);

			resetAction = action;
		}
	}
}