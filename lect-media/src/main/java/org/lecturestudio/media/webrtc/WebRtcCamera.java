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

import static java.util.Objects.nonNull;

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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lecturestudio.core.camera.AbstractCamera;
import org.lecturestudio.core.camera.CameraException;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.camera.CameraFormatComparator;

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
	public void open() throws CameraException {
		if (!open.get()) {
			int width = getFormat().getWidth();
			int height = getFormat().getHeight();
			int frameRate = (int) getFormat().getFrameRate();

			final int imageWidth = nonNull(imageSize) ? (int) imageSize.getWidth() : width;
			final int imageHeight = nonNull(imageSize) ? (int) imageSize.getHeight() : height;

			CameraFormat nearestFormat = getNearestSupportedCameraFormat(getFormat());

			if (nonNull(nearestFormat)) {
				width = nearestFormat.getWidth();
				height = nearestFormat.getHeight();
			}

			VideoCaptureCapability capability = new VideoCaptureCapability(width, height, frameRate);

			videoCapture = new VideoCapture();
			videoCapture.setVideoCaptureDevice(device);
			videoCapture.setVideoCaptureCapability(capability);
			videoCapture.setVideoSink(frame -> {
				VideoFrameBuffer buffer = frame.buffer;

				try {
					VideoFrameBuffer scaled = buffer.cropAndScale(0, 0,
							buffer.getWidth(), buffer.getHeight(),
							imageWidth, imageHeight);
					VideoBufferConverter.convertFromI420(scaled, imageBuffer, FourCC.ARGB);
					scaled.release();

					if (nonNull(imageConsumer)) {
						imageConsumer.accept(image);
					}
				}
				catch (Exception e) {
					throw new RuntimeException("Scale camera frame failed", e);
				}
			});

			try {
				createBufferedImage(imageWidth, imageHeight);

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
				set.add(new CameraFormat(format.width, format.height, format.frameRate));
			}
		}

		return set.toArray(new CameraFormat[0]);
	}

	private List<CameraFormat> getCameraFormatsForRatio(BigDecimal ratio) {
		CameraFormat[] formats = getSupportedFormats();
		List<CameraFormat> ratioFormats = new ArrayList<>();

		for (CameraFormat format : formats) {
			float r = format.getWidth() / (float) format.getHeight();
			BigDecimal rt = BigDecimal.valueOf(r).setScale(3, RoundingMode.HALF_UP);

			if (rt.equals(ratio)) {
				ratioFormats.add(format);
			}
		}

		ratioFormats.sort(new CameraFormatComparator());

		return ratioFormats;
	}

	private CameraFormat getNearestSupportedCameraFormat(CameraFormat format) {
		int width = format.getWidth();
		int height = format.getHeight();
		float ratio = width / (float) height;

		BigDecimal ratioT = BigDecimal.valueOf(ratio).setScale(3, RoundingMode.HALF_UP);
		List<CameraFormat> ratioFormats = getCameraFormatsForRatio(ratioT);
		CameraFormat bestFormat = null;

		for (CameraFormat cf : ratioFormats) {
			if (cf.equals(format)) {
				return format;
			}
			if (cf.getWidth() == width && cf.getHeight() < height) {
				bestFormat = cf;
			}
			else if (cf.getWidth() < width) {
				bestFormat = cf;
			}
		}

		return bestFormat;
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
