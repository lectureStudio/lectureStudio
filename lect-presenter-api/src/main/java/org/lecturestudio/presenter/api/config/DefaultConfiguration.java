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

package org.lecturestudio.presenter.api.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import org.lecturestudio.core.app.Theme;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioProcessingSettings;
import org.lecturestudio.core.audio.AudioProcessingSettings.NoiseSuppressionLevel;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.tool.PresetColor;
import org.lecturestudio.presenter.api.model.MessageBarPosition;

public class DefaultConfiguration extends PresenterConfiguration {

	public DefaultConfiguration() {
		setApplicationName("lecturePresenter");
		setTheme(new Theme("default", null));
		setLocale(Locale.GERMANY);
		setCheckNewVersion(true);
		setUIControlSize(10);
		setExtendPageDimension(new Dimension2D(1.3, 1.3));
		setStartMaximized(true);
		setTabletMode(false);
		setSaveDocumentOnClose(true);
		setAdvancedUIMode(true);
		setExtendedFullscreen(true);
		setNotifyToRecord(false);
		setConfirmStopRecording(true);
		setPageRecordingTimeout(2000);

		getWhiteboardConfig().setBackgroundColor(Color.WHITE);
		getWhiteboardConfig().setVerticalLinesVisible(true);
		getWhiteboardConfig().setVerticalLinesInterval(0.5);
		getWhiteboardConfig().setHorizontalLinesVisible(true);
		getWhiteboardConfig().setHorizontalLinesInterval(0.5);
		getWhiteboardConfig().setGridColor(new Color(230, 230, 230));
		getWhiteboardConfig().setShowGridOnDisplays(false);

		getDisplayConfig().setAutostart(false);
		getDisplayConfig().setBackgroundColor(Color.WHITE);
		getDisplayConfig().setIpPosition(Position.BOTTOM_CENTER);

		getToolConfig().getPenSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getPenSettings().setWidth(0.003);
		getToolConfig().getHighlighterSettings().setColor(PresetColor.ORANGE.getColor());
		getToolConfig().getHighlighterSettings().setAlpha(140);
		getToolConfig().getHighlighterSettings().setWidth(0.011);
		getToolConfig().getHighlighterSettings().setScale(false);
		getToolConfig().getPointerSettings().setColor(PresetColor.RED.getColor());
		getToolConfig().getPointerSettings().setAlpha(140);
		getToolConfig().getPointerSettings().setWidth(0.011);
		getToolConfig().getArrowSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getArrowSettings().setWidth(0.003);
		getToolConfig().getLineSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getLineSettings().setWidth(0.003);
		getToolConfig().getRectangleSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getRectangleSettings().setWidth(0.003);
		getToolConfig().getEllipseSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getEllipseSettings().setWidth(0.003);
		getToolConfig().getTextSelectionSettings().setColor(PresetColor.ORANGE.getColor());
		getToolConfig().getTextSelectionSettings().setAlpha(140);
		getToolConfig().getTextSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getTextSettings().setFont(new Font("Arial", 24));
		getToolConfig().getTextSettings().setTextAttributes(new TextAttributes());
		getToolConfig().getLatexSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getLatexSettings().setFont(new TeXFont(TeXFont.Type.SERIF, 20));

		getToolConfig().getPresetColors().addAll(new ArrayList<>(6));

		Collections.fill(getToolConfig().getPresetColors(), Color.WHITE);

		getExternalMessagesConfig().setEnabled(false);
		getExternalSlidePreviewConfig().setEnabled(false);
		getExternalSpeechConfig().setEnabled(false);

		getSlideViewConfiguration().setBottomSliderPosition(0.7);
		getSlideViewConfiguration().setLeftSliderPosition(0.375);
		getSlideViewConfiguration().setRightSliderPosition(0.8);
		getSlideViewConfiguration().setMessageBarPosition(MessageBarPosition.BOTTOM);

		AudioProcessingSettings processingSettings = new AudioProcessingSettings();
		processingSettings.setHighpassFilterEnabled(true);
		processingSettings.setNoiseSuppressionEnabled(true);
		processingSettings.setNoiseSuppressionLevel(NoiseSuppressionLevel.MODERATE);

		getAudioConfig().setRecordingFormat(new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 1));
		getAudioConfig().setRecordingPath(System.getProperty("user.home"));
		getAudioConfig().setRecordingProcessingSettings(processingSettings);
		getAudioConfig().setPlaybackVolume(1.0);
		getAudioConfig().setDefaultRecordingVolume(1.0f);
		getAudioConfig().setMasterRecordingVolume(1.0f);
		getAudioConfig().setMixAudioStreams(false);

		getStreamConfig().setServerName("lect.stream");
		getStreamConfig().setAudioCodec("OPUS");
		getStreamConfig().setAudioFormat(new AudioFormat(AudioFormat.Encoding.S16LE, 24000, 1));
		getStreamConfig().getCameraCodecConfig().setBitRate(200);
		getStreamConfig().getCameraCodecConfig().setPreset("ultrafast");
		getStreamConfig().getCameraCodecConfig().setFrameRate(30);

		getTemplateConfig().getQuizTemplateConfig().setBounds(new Rectangle2D(0.05, 0.05, 0.9, 0.9));
		getTemplateConfig().getChatMessageTemplateConfig().setBounds(new Rectangle2D(0.05, 0.05, 0.9, 0.9));
	}

}
