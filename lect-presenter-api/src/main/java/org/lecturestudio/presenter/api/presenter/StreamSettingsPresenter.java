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

import static java.util.Objects.isNull;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.net.ScreenShareProfiles;
import org.lecturestudio.presenter.api.service.WebServiceInfo;
import org.lecturestudio.presenter.api.view.StreamSettingsView;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.service.StreamProviderService;

public class StreamSettingsPresenter extends Presenter<StreamSettingsView> {

	private final DefaultConfiguration defaultConfig;

	private final WebServiceInfo webServiceInfo;


	@Inject
	public StreamSettingsPresenter(ApplicationContext context,
			StreamSettingsView view, WebServiceInfo webServiceInfo) {
		super(context, view);

		this.webServiceInfo = webServiceInfo;
		this.defaultConfig = new DefaultConfiguration();
	}

	public void reset() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();
		VideoCodecConfiguration cameraConfig = streamConfig.getCameraCodecConfig();

		streamConfig.setAudioCodec(defaultConfig.getStreamConfig().getAudioCodec());
		streamConfig.setAudioFormat(defaultConfig.getStreamConfig().getAudioFormat());
		streamConfig.setAccessToken(defaultConfig.getStreamConfig().getAccessToken());
		streamConfig.setServerName(defaultConfig.getStreamConfig().getServerName());
		streamConfig.setScreenShareProfile(defaultConfig.getStreamConfig().getScreenShareProfile());

		cameraConfig.setBitRate(defaultConfig.getStreamConfig().getCameraCodecConfig().getBitRate());
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		if (isNull(streamConfig.getServerName())) {
			streamConfig.setServerName(defaultConfig.getStreamConfig().getServerName());
		}
		if (isNull(streamConfig.getScreenShareProfile())) {
			streamConfig.setScreenShareProfile(defaultConfig.getStreamConfig().getScreenShareProfile());
		}

		streamConfig.screenProfileProperty().addListener((o, oldValue, newValue) -> {
			streamConfig.getScreenCodecConfig().setFrameRate(newValue.getFramerate());
			streamConfig.getScreenCodecConfig().setBitRate(newValue.getBitrate());
		});

		view.setServerName(streamConfig.serverNameProperty());
		view.setAccessToken(streamConfig.accessTokenProperty());
		view.setScreenShareProfile(streamConfig.screenProfileProperty());
		view.setScreenShareProfiles(ScreenShareProfiles.DEFAULT);
		view.setOnCheckAccessToken(this::checkAccessToken);
		view.setOnReset(this::reset);

		checkAccessToken();
	}

	public void checkAccessToken() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		ServiceParameters parameters = new ServiceParameters();
		parameters.setUrl(webServiceInfo.getStreamPublisherApiUrl());

		StreamProviderService streamProviderService = new StreamProviderService(
				parameters, streamConfig::getAccessToken);

		CompletableFuture.supplyAsync(streamProviderService::getUserInfo)
				.thenAccept((userInfo) -> {
					view.setAccessTokenValid(true);
				})
				.exceptionally(throwable -> {
					view.setAccessTokenValid(false);
					return null;
				});
	}
}
