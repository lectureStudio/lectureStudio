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

package org.lecturestudio.media.avdev;

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

import org.lecturestudio.avdev.PictureFormat;
import org.lecturestudio.avdev.VideoCaptureDevice;
import org.lecturestudio.avdev.VideoOutputStream;
import org.lecturestudio.avdev.VideoSink;
import org.lecturestudio.core.camera.AbstractCamera;
import org.lecturestudio.core.camera.CameraException;
import org.lecturestudio.core.camera.CameraFormat;

/**
 * AVdev camera implementation.
 *
 * @author Alex Andres
 */
public class AVdevCamera extends AbstractCamera {

	/** The camera device. */
	private final VideoCaptureDevice device;

	/** The camera image output stream. */
	private VideoOutputStream stream;

	/* The buffered image. */
	private BufferedImage image;

	/* The temporary image buffer. */
	private byte[] imageBuffer;


	/**
	 * Create a AVdevCamera with the specified capture device.
	 *
	 * @param device The camera device.
	 */
	public AVdevCamera(VideoCaptureDevice device) {
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

			// Keep RGB format, since pixel format conversion is done by AVdev.
			PictureFormat format = new PictureFormat(PictureFormat.PixelFormat.RGB24, width, height);

			try {
				createBufferedImage(format.getWidth(), format.getHeight());

				device.setPictureFormat(format);
				device.setFrameRate((float) getFormat().getFrameRate());

				stream = device.createOutputStream(new PictureSink());
				stream.open();
				stream.start();
			}
			catch (Exception e) {
				throw new CameraException(e.getMessage(), e.getCause());
			}

			open.set(true);
		}
	}

	@Override
	public void close() throws CameraException {
		if (open.compareAndSet(true, false) && stream != null) {
			try {
				stream.stop();
				stream.close();
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
		List<PictureFormat> formats = device.getPictureFormats();

		if (formats != null && !formats.isEmpty()) {
			for (PictureFormat format : formats) {
				if (format.getWidth() > 1920) {
					continue;
				}

				set.add(new CameraFormat(format.getWidth(), format.getHeight(), 30));
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

		int bytesPerPixel = 3;
		int bufferSize = width * height * bytesPerPixel;

		DataBufferByte dataBuffer = new DataBufferByte(bufferSize);

		WritableRaster raster = Raster.createInterleavedRaster(dataBuffer,
				width,
                height,
                width * bytesPerPixel,
                bytesPerPixel,
                new int[] { 2, 1, 0 },
                null);

		ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
				new int[] { 8, 8, 8 },
				false,
				false,
				ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

		image = new BufferedImage(colorModel, raster, false, null);
		imageBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	}



	private class PictureSink implements VideoSink {

		@Override
		public void write(byte[] data, int length) {
			// Copy pixels.
			System.arraycopy(data, 0, imageBuffer, 0, imageBuffer.length);
		}

	}

}
