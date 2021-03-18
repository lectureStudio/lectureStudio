package com.github.javaffmpeg;

import java.awt.image.*;
import java.nio.*;

public final class Image {

	public static PixelFormat getPixelFormat(BufferedImage image) {
	   	switch (image.getType()) {
		    case BufferedImage.TYPE_3BYTE_BGR:
			    return PixelFormat.BGR24;

		    case BufferedImage.TYPE_4BYTE_ABGR:
		    case BufferedImage.TYPE_4BYTE_ABGR_PRE:
			    return PixelFormat.ABGR;

		    case BufferedImage.TYPE_BYTE_BINARY:
			    return PixelFormat.RGB444;

		    case BufferedImage.TYPE_BYTE_INDEXED:
			    return PixelFormat.RGB555;

		    case BufferedImage.TYPE_BYTE_GRAY:
			    return PixelFormat.GRAY8;

		    case BufferedImage.TYPE_INT_ARGB:
		    case BufferedImage.TYPE_INT_ARGB_PRE:
			    return PixelFormat.ARGB;

		    case BufferedImage.TYPE_INT_RGB:
			    return PixelFormat.RGB24;

		    case BufferedImage.TYPE_USHORT_GRAY:
			    return PixelFormat.GRAY16;

		    case BufferedImage.TYPE_USHORT_555_RGB:
			    return PixelFormat.RGB555;

		    case BufferedImage.TYPE_USHORT_565_RGB:
			    return PixelFormat.RGB565;

		    case BufferedImage.TYPE_CUSTOM:
			    return null;

		    default:
			    return null;
	    }
	}

	public static ByteBuffer createImageBuffer(BufferedImage image) {
		SampleModel model = image.getSampleModel();
		Raster raster = image.getRaster();
		DataBuffer inBuffer = raster.getDataBuffer();
		ByteBuffer outBuffer = null;
		
		int x = -raster.getSampleModelTranslateX();
		int y = -raster.getSampleModelTranslateY();
		int step = model.getWidth() * model.getNumBands();
		int channels = model.getNumBands();

		if (model instanceof ComponentSampleModel) {
			ComponentSampleModel compModel = (ComponentSampleModel) model;
			step = compModel.getScanlineStride();
			channels = ((ComponentSampleModel) model).getPixelStride();
		}
		else if (model instanceof SinglePixelPackedSampleModel) {
			SinglePixelPackedSampleModel singleModel = (SinglePixelPackedSampleModel) model;
			step = singleModel.getScanlineStride();
			channels = 1;
		}
		else if (model instanceof MultiPixelPackedSampleModel) {
			MultiPixelPackedSampleModel multiModel = (MultiPixelPackedSampleModel) model;
			step = multiModel.getScanlineStride();
			channels = ((MultiPixelPackedSampleModel) model).getPixelBitStride() / 8;
		}
		
		int start = y * step + x * channels;
		
		if (inBuffer instanceof DataBufferByte) {
			byte[] a = ((DataBufferByte) inBuffer).getData();
			
			outBuffer = ByteBuffer.allocate(a.length);
			outBuffer.put(a);
		}
		else if (inBuffer instanceof DataBufferShort) {
			short[] a = ((DataBufferShort) inBuffer).getData();
			
			outBuffer = ByteBuffer.allocate(a.length * 2);
			outBuffer.asShortBuffer().put(a);
		}
		else if (inBuffer instanceof DataBufferUShort) {
			short[] a = ((DataBufferUShort) inBuffer).getData();
			
			outBuffer = ByteBuffer.allocate(a.length * 2);
			// this one is slow
			copy(ShortBuffer.wrap(a, start, a.length - start), step, outBuffer.asShortBuffer(), step / 2, false);
		}
		else if (inBuffer instanceof DataBufferInt) {
			int[] a = ((DataBufferInt) inBuffer).getData();
			
			outBuffer = ByteBuffer.allocate(a.length * 4);
			outBuffer.asIntBuffer().put(a);
		}
		else if (inBuffer instanceof DataBufferFloat) {
			float[] a = ((DataBufferFloat) inBuffer).getData();
			
			outBuffer = ByteBuffer.allocate(a.length * 4);
			outBuffer.asFloatBuffer().put(a);
		}
		else if (inBuffer instanceof DataBufferDouble) {
			double[] a = ((DataBufferDouble) inBuffer).getData();
			
			outBuffer = ByteBuffer.allocate(a.length * 8);
			outBuffer.asDoubleBuffer().put(a);
		}

		outBuffer.position(0);
		
		return outBuffer;
	}
	
	public static BufferedImage createImage(ByteBuffer data, int width, int height, int type) {
		BufferedImage image = new BufferedImage(width, height, type);
		
		SampleModel model = image.getSampleModel();
		Raster raster = image.getRaster();
		DataBuffer outBuffer = raster.getDataBuffer();
		
		int x = -raster.getSampleModelTranslateX();
		int y = -raster.getSampleModelTranslateY();
		int step = model.getWidth() * model.getNumBands();
		int channels = model.getNumBands();
		
		data.position(0).limit(height * width * channels);
		
		if (model instanceof ComponentSampleModel) {
			ComponentSampleModel compModel = (ComponentSampleModel) model;
			step = compModel.getScanlineStride();
			channels = compModel.getPixelStride();
		}
		else if (model instanceof SinglePixelPackedSampleModel) {
			SinglePixelPackedSampleModel singleModel = (SinglePixelPackedSampleModel) model;
			step = singleModel.getScanlineStride();
			channels = 1;
		}
		else if (model instanceof MultiPixelPackedSampleModel) {
			MultiPixelPackedSampleModel multiModel = (MultiPixelPackedSampleModel) model; 
			step = multiModel.getScanlineStride();
			channels = ((MultiPixelPackedSampleModel) model).getPixelBitStride() / 8;
		}
		
		int start = y * step + x * channels;

		if (outBuffer instanceof DataBufferByte) {
			byte[] a = ((DataBufferByte) outBuffer).getData();
			data.get(a);
		}
		else if (outBuffer instanceof DataBufferShort) {
			short[] a = ((DataBufferShort) outBuffer).getData();
			data.asShortBuffer().get(a);
		}
		else if (outBuffer instanceof DataBufferUShort) {
			short[] a = ((DataBufferUShort) outBuffer).getData();
			// this one is slow
			copy(data.asShortBuffer(), step / 2, ShortBuffer.wrap(a, start, a.length - start), step, false);
		}
		else if (outBuffer instanceof DataBufferInt) {
			int[] a = ((DataBufferInt) outBuffer).getData();
			data.asIntBuffer().get(a);
		}
		else if (outBuffer instanceof DataBufferFloat) {
			float[] a = ((DataBufferFloat) outBuffer).getData();
			data.asFloatBuffer().get(a);
		}
		else if (outBuffer instanceof DataBufferDouble) {
			double[] a = ((DataBufferDouble) outBuffer).getData();
			data.asDoubleBuffer().get(a);
		}
		
		return image;
	}
	
	public static void copy(ShortBuffer srcBuf, int srcStep, ShortBuffer dstBuf, int dstStep, boolean signed) {
		int w = Math.min(srcStep, dstStep);
		int srcLine = srcBuf.position();
		int dstLine = dstBuf.position();

		while (srcLine < srcBuf.capacity() && dstLine < dstBuf.capacity()) {
			srcBuf.position(srcLine);
			dstBuf.position(dstLine);

			w = Math.min(Math.min(w, srcBuf.remaining()), dstBuf.remaining());

			for (int x = 0; x < w; x++) {
				int in = signed ? srcBuf.get() : srcBuf.get() & 0xFFFF;
				short out = (short) in;
				dstBuf.put(out);
			}

			srcLine += srcStep;
			dstLine += dstStep;
		}
	}
	
}
