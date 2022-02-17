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

import java.io.IOException;
import java.util.Properties;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.view.StreamSettingsView;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.service.StreamProviderService;

public class StreamSettingsPresenter extends Presenter<StreamSettingsView> {

	private final DefaultConfiguration defaultConfig;

	private StreamProviderService streamProviderService;


	@Inject
	public StreamSettingsPresenter(ApplicationContext context,
			StreamSettingsView view) {
		super(context, view);

		this.defaultConfig = new DefaultConfiguration();
	}

	public void reset() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();
		VideoCodecConfiguration cameraConfig = streamConfig.getCameraCodecConfig();

		streamConfig.setAudioCodec(defaultConfig.getStreamConfig().getAudioCodec());
		streamConfig.setAudioFormat(defaultConfig.getStreamConfig().getAudioFormat());
		streamConfig.setAccessToken(defaultConfig.getStreamConfig().getAccessToken());

		cameraConfig.setBitRate(defaultConfig.getStreamConfig().getCameraCodecConfig().getBitRate());
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		view.setAccessToken(streamConfig.accessTokenProperty());
		view.setOnCheckAccessToken(this::checkAccessToken);
		view.setOnReset(this::reset);

		// Retrieve properties here since named injection does not work.
		Properties streamProps = new Properties();

		try {
			streamProps.load(getClass().getClassLoader()
					.getResourceAsStream("resources/stream.properties"));
		}
		catch (IOException e) {
			logException(e, "Load stream properties failed");
		}

		String streamPublisherApiUrl = streamProps.getProperty(
				"stream.publisher.api.url");

		ServiceParameters parameters = new ServiceParameters();
		parameters.setUrl(streamPublisherApiUrl);

		streamProviderService = new StreamProviderService(parameters,
				streamConfig::getAccessToken);

		checkAccessToken();
	}

	public void checkAccessToken() {
		try {
			streamProviderService.getCourses();

			view.setAccessTokenValid(true);
		}
		catch (Exception e) {
			view.setAccessTokenValid(false);
		}
	}
}
