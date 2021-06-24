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

import org.lecturestudio.broadcast.config.BroadcastProfile;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.codec.AudioCodecLoader;
import org.lecturestudio.core.audio.codec.AudioCodecProvider;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.NetworkConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.view.StreamSettingsView;

import javax.inject.Inject;
import java.util.Arrays;

import static java.util.Objects.nonNull;

public class StreamSettingsPresenter extends Presenter<StreamSettingsView> {

	private final DefaultConfiguration defaultConfig;


	@Inject
	public StreamSettingsPresenter(ApplicationContext context, StreamSettingsView view) {
		super(context, view);

		this.defaultConfig = new DefaultConfiguration();
	}

	public void setStreamAudioFormats(String codecName) {
		AudioCodecProvider codecProvider = AudioCodecLoader.getInstance().getProvider(codecName);
		if (nonNull(codecProvider)) {
			AudioFormat[] audioFormats = codecProvider.getAudioEncoder().getSupportedFormats();
			if (nonNull(audioFormats)) {
				view.setStreamAudioFormats(Arrays.asList(audioFormats));
			}
		}
	}

	public void reset() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration netConfig = config.getNetworkConfig();
		StreamConfiguration streamConfig = config.getStreamConfig();
		VideoCodecConfiguration cameraConfig = streamConfig.getCameraCodecConfig();

		streamConfig.setAudioCodec(defaultConfig.getStreamConfig().getAudioCodec());
		streamConfig.setAudioFormat(defaultConfig.getStreamConfig().getAudioFormat());

		cameraConfig.setBitRate(defaultConfig.getStreamConfig().getCameraCodecConfig().getBitRate());

		netConfig.getBroadcastProfiles().clear();
		netConfig.getBroadcastProfiles().addAll(defaultConfig.getNetworkConfig().getBroadcastProfiles());
		netConfig.setBroadcastProfile(defaultConfig.getNetworkConfig().getBroadcastProfile());
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		AudioConfiguration audioConfig = config.getAudioConfig();
		NetworkConfiguration netConfig = config.getNetworkConfig();
		StreamConfiguration streamConfig = config.getStreamConfig();
		VideoCodecConfiguration cameraConfig = streamConfig.getCameraCodecConfig();

		String soundSystemName = audioConfig.getSoundSystem();
		String[] codecNames = AudioUtils.getSupportedAudioCodecs(soundSystemName);

		setStreamAudioFormats(streamConfig.getAudioCodec());

		view.setStreamAudioFormat(streamConfig.audioFormatProperty());
		view.setStreamAudioCodecNames(codecNames);
		view.setStreamAudioCodecName(streamConfig.audioCodecProperty());
		view.setStreamCameraBitrate(cameraConfig.bitRateProperty());

		view.setBroadcastProfiles(netConfig.getBroadcastProfiles());
		view.setBroadcastProfile(netConfig.broadcastProfileProperty());
		view.setOnNewBroadcastProfile(profile -> netConfig.getBroadcastProfiles().add(profile));

		view.setOnReset(this::reset);

		netConfig.getBroadcastProfiles().addListener(new ListChangeListener<>() {

			@Override
			public void listChanged(ObservableList<BroadcastProfile> list) {
				view.setBroadcastProfiles(netConfig.getBroadcastProfiles());
			}
		});

		streamConfig.audioCodecProperty().addListener((observable, oldCodec, newCodec) -> {
			setStreamAudioFormats(newCodec);
		});
	}
}
