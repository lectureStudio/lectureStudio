/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.media.webrtc;

import dev.onvoid.webrtc.media.FourCC;
import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.video.VideoBufferConverter;
import dev.onvoid.webrtc.media.video.VideoCapture;
import dev.onvoid.webrtc.media.video.VideoCaptureCapability;
import dev.onvoid.webrtc.media.video.VideoDevice;
import dev.onvoid.webrtc.media.video.VideoFrameBuffer;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lecturestudio.core.camera.AbstractCamera;
import org.lecturestudio.core.camera.CameraException;
import org.lecturestudio.core.camera.CameraFormat;

/**
 * WebRTC camera implementation.
 *
 * @author Alex Andres
 */
public class WebRtcCamera extends AbstractCamera {

	/** The camera device. */
	private final VideoDevice device;

	/** The camera capture manager. */
	private VideoCapture videoCapture;

	/* The buffered image. */
	private BufferedImage image;

	/* The temporary image buffer. */
	private byte[] imageBuffer;


	/**
	 * Create a AVdevCamera with the specified capture device.
	 *
	 * @param device The camera device.
	 */
	public WebRtcCamera(VideoDevice device) {
		super();

		this.device = device;
	}

	@Override
	public String getName() {
		return device.getName();
	}

	@Override
	public String getDeviceDescriptor() {
		return device.getDescriptor();
	}

	@Override
	public BufferedImage getImage() {
		if (open.get() && device != null) {
			return image;
		}
		return null;
	}

	@Override
	public void open() throws CameraException {
		if (!open.get()) {
			int width = getFormat().getWidth();
			int height = getFormat().getHeight();
			int frameRate = (int) getFormat().getFrameRate();

			VideoCaptureCapability capability = new VideoCaptureCapability(width, height, frameRate);

			videoCapture = new VideoCapture();
			videoCapture.setVideoCaptureDevice(device);
			videoCapture.setVideoCaptureCapability(capability);
			videoCapture.setVideoSink(frame -> {
				VideoFrameBuffer buffer = frame.buffer;

				try {
					VideoBufferConverter.convertFromI420(buffer, imageBuffer, FourCC.ARGB);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			});

			try {
				createBufferedImage(width, height);

				videoCapture.start();
			}
			catch (Exception e) {
				throw new CameraException(e.getMessage(), e.getCause());
			}

			open.set(true);
		}
	}

	@Override
	public void close() throws CameraException {
		if (open.compareAndSet(true, false) && videoCapture != null) {
			try {
				videoCapture.stop();
				videoCapture.dispose();
			}
			catch (Exception e) {
				throw new CameraException(e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public CameraFormat[] getSupportedFormats() {
		if (formats == null) {
			formats = getCameraFormats();
		}

		return formats;
	}

	private CameraFormat[] getCameraFormats() {
		Set<CameraFormat> set = new HashSet<>();
		List<VideoCaptureCapability> formats = MediaDevices.getVideoCaptureCapabilities(device);

		if (formats != null && !formats.isEmpty()) {
			for (VideoCaptureCapability format : formats) {
				if (format.width > 1920) {
					continue;
				}

				set.add(new CameraFormat(format.width, format.height, 30));
			}
		}

		return set.toArray(new CameraFormat[0]);
	}

	private void createBufferedImage(int width, int height) {
		if (image != null) {
			image.flush();
			image = null;
			imageBuffer = null;
		}

		int bytesPerPixel = 4;
		int bufferSize = width * height * bytesPerPixel;

		DataBufferByte dataBuffer = new DataBufferByte(bufferSize);

		WritableRaster raster = Raster.createInterleavedRaster(dataBuffer,
				width,
                height,
                width * bytesPerPixel,
                bytesPerPixel,
                new int[] { 2, 1, 0, 3 },
                null);

		ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
				new int[] { 8, 8, 8, 8 },
				true,
				false,
				ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

		image = new BufferedImage(colorModel, raster, false, null);
		imageBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	}
}
