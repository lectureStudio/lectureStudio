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

package org.lecturestudio.media.net.server;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.media.camera.CameraService;
import org.lecturestudio.core.camera.CameraSource;
import org.lecturestudio.core.camera.bus.event.CameraServerEvent;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.codec.h264.RtpH264Packetizer;
import org.lecturestudio.core.io.VideoSource;
import org.lecturestudio.core.net.Sync;
import org.lecturestudio.core.net.rtp.RtpPacket;
import org.lecturestudio.media.config.CameraStreamConfig;
import org.lecturestudio.web.api.connector.client.ClientConnector;

public class CameraServer extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(CameraServer.class);

	private final ClientConnector connector;

	private final CameraStreamConfig cameraStreamConfig;

	/** The RTP packetizer for the video codec. */
	private RtpH264Packetizer rtpPacketizer;

	private VideoSource videoSource;


	public CameraServer(CameraStreamConfig cameraStreamConfig, ClientConnector connector) {
		this.cameraStreamConfig = cameraStreamConfig;
		this.connector = connector;
	}
	
	@Override
	protected void initInternal() throws ExecutableException {
		connector.init();

		Camera[] cameraList = CameraService.get().getCameraDriver().getCameras();

		if (isNull(cameraList) || cameraList.length < 1) {
			throw new ExecutableException("No cameras available.");
		}

		Camera camera = CameraService.get().getCamera(cameraStreamConfig.cameraName);
		CameraFormat cameraFormat = cameraStreamConfig.cameraFormat;
		VideoCodecConfiguration codecConfig = cameraStreamConfig.codecConfig;

		rtpPacketizer = new RtpH264Packetizer(RtpH264Packetizer.Mode.SINGLE_NAL);

		videoSource = new CameraSource(camera, cameraFormat, codecConfig);
		videoSource.setSink(this::onVideoFrame);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		connector.addStateListener((oldState, newState) -> {
			if (started() && newState == ExecutableState.Error) {
				try {
					setState(ExecutableState.Error);

					stop();
					destroy();
				}
				catch (ExecutableException e) {
					LOG.error("Handle connector error failed.", e);
				}
			}
		});
		connector.start();

		try {
			videoSource.start();
		}
		catch (IOException e) {
			throw new ExecutableException("Start video source failed.", e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		connector.stop();

		try {
			videoSource.stop();
		}
		catch (IOException e) {
			throw new ExecutableException("Stop video source failed.", e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		connector.destroy();
	}
	
	@Override
	protected void fireStateChanged() {
		ApplicationBus.post(new CameraServerEvent(getState()));
	}

	private void onVideoFrame(ByteBuffer data) {
		try {
			if (data != null) {
				long timestamp = Sync.getTimestamp();
				List<RtpPacket> packets = rtpPacketizer.processPacket(data, timestamp);

				for (RtpPacket packet : packets) {
					connector.send(packet.toByteArray());
				}
			}
		}
		catch (Exception e) {
			LOG.error("Send video frame failed.", e);
		}
	}
	
}
