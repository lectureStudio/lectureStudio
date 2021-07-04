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

package org.lecturestudio.presenter.api.service;

import java.security.MessageDigest;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.broadcast.config.BroadcastProfile;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.NetUtils;
import org.lecturestudio.media.config.AudioStreamConfig;
import org.lecturestudio.media.config.CameraStreamConfig;
import org.lecturestudio.presenter.api.config.NetworkConfiguration;
import org.lecturestudio.media.net.server.MediaStreamProvider;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.event.CameraStateEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.presenter.api.net.LocalBroadcaster;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomDocument;

/**
 * The {@code StreamService} is the interface between user interface and the
 * servers.
 *
 * @author Alex Andres
 */
@Singleton
public class ChannelStreamService extends ExecutableBase {

	@Inject
	private ApplicationContext context;

	@Inject
	private LocalBroadcaster localBroadcaster;

	@Inject
	private DocumentService documentService;

	private MediaStreamProvider streamProvider;

	private ExecutableState streamState;

	private ExecutableState cameraState;


	public void startCameraStream() throws ExecutableException {
		if (cameraState == ExecutableState.Started) {
			return;
		}

		setCameraState(ExecutableState.Starting);

		streamProvider.setCameraStreamConfig(createCameraStreamConfig());
		streamProvider.startCameraStream();

		setCameraState(ExecutableState.Started);
	}

	public void stopCameraStream() throws ExecutableException {
		if (cameraState != ExecutableState.Started) {
			return;
		}

		setCameraState(ExecutableState.Stopping);

		streamProvider.stopCameraStream();

		setCameraState(ExecutableState.Stopped);
	}

	@Override
	protected void initInternal() {
		streamState = ExecutableState.Stopped;
		cameraState = ExecutableState.Stopped;
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (streamState == ExecutableState.Started) {
			return;
		}

		setStreamState(ExecutableState.Starting);

		checkStartLocalBroadcaster();

		try {
			streamProvider = createStreamProvider();
			streamProvider.setClassroom(createClassroom());
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

//		org.lecturestudio.web.api.model.StreamService streamService = new org.lecturestudio.web.api.model.StreamService();
//		streamService.setAudioCodec(streamConfig.getAudioCodec());
//		streamService.setAudioFormat(streamConfig.getAudioFormat());

//		streamProvider.setStreamConfig(streamService);
		streamProvider.setAudioStreamConfig(createAudioStreamConfig());
		streamProvider.addStateListener((oldState, newState) -> {
			if (started() && newState == ExecutableState.Error) {
				setStreamState(ExecutableState.Error);
				setCameraState(ExecutableState.Error);
			}
		});
		streamProvider.start();

		setStreamState(ExecutableState.Started);
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (streamState != ExecutableState.Started) {
			return;
		}

		setStreamState(ExecutableState.Stopping);

		streamProvider.stop();
		streamProvider.destroy();

		checkStopLocalBroadcaster();

		setStreamState(ExecutableState.Stopped);
	}

	@Override
	protected void destroyInternal() {

	}

	private MediaStreamProvider createStreamProvider() throws Exception {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration netConfig = config.getNetworkConfig();

//		ConnectionParameters parameters = new ConnectionParameters(netConfig.getBroadcastAddress(), netConfig.getBroadcastTlsPort(), true);

		return new MediaStreamProvider(context);
	}

	private Classroom createClassroom() throws Exception {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration networkConfig = config.getNetworkConfig();

		// Set current document.
		MessageDigest digest = MessageDigest.getInstance("MD5");
		Document doc = documentService.getDocuments().getSelectedDocument();
		String fileName = doc.getName() + ".pdf";

		ClassroomDocument classDoc = new ClassroomDocument();
		classDoc.setFileName(fileName);
		classDoc.setChecksum(doc.getChecksum(digest));

		Classroom classroom = new Classroom(config.getClassroomName(), config.getClassroomShortName());
		classroom.getDocuments().add(classDoc);
		classroom.setIpFilterRules(networkConfig.getIpFilter().getRules());

		return classroom;
	}

	private AudioStreamConfig createAudioStreamConfig() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		AudioStreamConfig audioStreamConfig = new AudioStreamConfig();
		audioStreamConfig.codec = config.getStreamConfig().getAudioCodec();
		audioStreamConfig.format = config.getStreamConfig().getAudioFormat();
		audioStreamConfig.captureDeviceName = config.getAudioConfig().getInputDeviceName();
		audioStreamConfig.system = config.getAudioConfig().getSoundSystem();

		return audioStreamConfig;
	}

	private CameraStreamConfig createCameraStreamConfig() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();
		VideoCodecConfiguration codecConfig = streamConfig.getCameraCodecConfig();

		CameraStreamConfig cameraStreamConfig = new CameraStreamConfig();
		cameraStreamConfig.cameraName = streamConfig.getCameraName();
		cameraStreamConfig.cameraFormat = streamConfig.getCameraFormat();
		cameraStreamConfig.codecConfig = codecConfig;

		return cameraStreamConfig;
	}

	private void checkStartLocalBroadcaster() throws ExecutableException {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration netConfig = config.getNetworkConfig();
		BroadcastProfile bastProfile = netConfig.getBroadcastProfile();
		String broadcastAddress = bastProfile.getBroadcastAddress();
		Integer broadcastPort = bastProfile.getBroadcastPort();

		if (NetUtils.isLocalAddress(broadcastAddress, broadcastPort)) {
			localBroadcaster.start();
		}
		else {
			checkStopLocalBroadcaster();
		}
	}

	private void checkStopLocalBroadcaster() throws ExecutableException {
		localBroadcaster.stop();
		localBroadcaster.destroy();
	}

	/**
	 * Sets the new stream state of this controller.
	 *
	 * @param state The new state.
	 */
	private void setStreamState(ExecutableState state) {
		this.streamState = state;

		context.getEventBus().post(new StreamingStateEvent(streamState));
	}

	/**
	 * Sets the new camera state of this controller.
	 *
	 * @param state The new state.
	 */
	private void setCameraState(ExecutableState state) {
		this.cameraState = state;

		context.getEventBus().post(new CameraStateEvent(cameraState));
	}
}
