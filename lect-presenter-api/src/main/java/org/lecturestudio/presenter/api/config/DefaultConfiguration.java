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

import org.lecturestudio.broadcast.config.BroadcastProfile;
import org.lecturestudio.core.app.Theme;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioProcessingSettings;
import org.lecturestudio.core.audio.AudioProcessingSettings.NoiseSuppressionLevel;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.tool.PresetColor;
import org.lecturestudio.core.util.OsInfo;
import org.lecturestudio.presenter.api.model.MessageBarPosition;
import org.lecturestudio.web.api.filter.IpFilter;
import org.lecturestudio.web.api.filter.IpRangeRule;
import org.lecturestudio.web.api.filter.RegexFilter;
import org.lecturestudio.web.api.filter.RegexRule;

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
		setClassroomName("Presenter Classroom");
		setClassroomShortName("");
		setAdvancedUIMode(false);
		setExtendedFullscreen(true);
		setNotifyToRecord(false);
		setConfirmStopRecording(true);
		setPageRecordingTimeout(2000);

		getGridConfig().setVerticalLinesVisible(true);
		getGridConfig().setVerticalLinesInterval(0.5);
		getGridConfig().setHorizontalLinesVisible(true);
		getGridConfig().setHorizontalLinesInterval(0.5);
		getGridConfig().setColor(new Color(230, 230, 230));
		getGridConfig().setShowGridOnDisplays(false);

		getWhiteboardConfig().setBackgroundColor(Color.WHITE);

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

		IpFilter filter = new IpFilter();
		filter.registerRule(new IpRangeRule("127.0.0.1", "127.0.0.1"));
		filter.registerRule(new IpRangeRule("192.168.0.1", "192.168.0.254"));
		filter.registerRule(new IpRangeRule("130.83.0.0", "130.83.255.254"));

		BroadcastProfile localProfile = new BroadcastProfile();
		localProfile.setName("Local");
		localProfile.setBroadcastAddress("0.0.0.0");
		localProfile.setBroadcastPort(OsInfo.isWindows() ? 80 : 8080);
		localProfile.setBroadcastTlsPort(OsInfo.isWindows() ? 443 : 8443);

		BroadcastProfile etitProfile = new BroadcastProfile();
		etitProfile.setName("ETiT Cloud");
		etitProfile.setBroadcastAddress("130.83.158.130");
		etitProfile.setBroadcastPort(8080);
		etitProfile.setBroadcastTlsPort(8443);

		getNetworkConfig().setIpFilter(filter);
		getNetworkConfig().setBroadcastProfile(localProfile);
		getNetworkConfig().getBroadcastProfiles().add(localProfile);
		getNetworkConfig().getBroadcastProfiles().add(etitProfile);

		getExternalMessagesConfig().setEnabled(false);
		getExternalSlidePreviewConfig().setEnabled(false);
		getExternalSpeechConfig().setEnabled(false);

		getMessageBarConfiguration().setMessageBarPosition(MessageBarPosition.AUTO);

		AudioProcessingSettings processingSettings = new AudioProcessingSettings();
		processingSettings.setHighpassFilterEnabled(true);
		processingSettings.setNoiseSuppressionEnabled(true);
		processingSettings.setNoiseSuppressionLevel(NoiseSuppressionLevel.MODERATE);

		getAudioConfig().setRecordingFormat(new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 1));
		getAudioConfig().setRecordingPath(System.getProperty("user.home"));
		getAudioConfig().setRecordingProcessingSettings(processingSettings);
		getAudioConfig().setDefaultRecordingVolume(1.0f);
		getAudioConfig().setMasterRecordingVolume(1.0f);

		getStreamConfig().setAudioCodec("OPUS");
		getStreamConfig().setAudioFormat(new AudioFormat(AudioFormat.Encoding.S16LE, 24000, 1));
		getStreamConfig().getCameraCodecConfig().setBitRate(200);
		getStreamConfig().getCameraCodecConfig().setPreset("ultrafast");
		getStreamConfig().getCameraCodecConfig().setFrameRate(30);

		RegexFilter inputFilter = new RegexFilter();
		inputFilter.registerRule(new RegexRule("^(1337)+"));
		inputFilter.registerRule(new RegexRule("^(42)+"));
		inputFilter.registerRule(new RegexRule("^(666)+"));

		getQuizConfig().setInputFilter(inputFilter);
	}

}
