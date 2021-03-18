package com.github.javaffmpeg;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.avcodec.AVPicture;
import org.bytedeco.javacpp.swscale.*;

import static org.bytedeco.javacpp.swscale.*;

public class PictureResampler {

	/** The re-sample context */
	private SwsContext convertContext;
	
	/** The input picture format */
	private PictureFormat srcFormat;
	
	/** The output picture format */
	private PictureFormat dstFormat;
	
	
	public void open(PictureFormat srcFormat, PictureFormat dstFormat) throws JavaFFmpegException {
		if (srcFormat == null || dstFormat == null)
			throw new JavaFFmpegException("Invalid video format provided: from " + srcFormat + " to " + dstFormat);
			
		convertContext = sws_getCachedContext(convertContext,
				srcFormat.getWidth(), srcFormat.getHeight(), srcFormat.getFormat().value(),
				dstFormat.getWidth(), dstFormat.getHeight(), dstFormat.getFormat().value(),
				SWS_BILINEAR, null, null, (double[]) null);
        
		if (convertContext == null)
            throw new JavaFFmpegException("Could not initialize the image conversion context.");
		
		this.srcFormat = srcFormat;
		this.dstFormat = dstFormat;
	}
	
	void resample(AVPicture srcPicture, AVPicture dstPicture) throws JavaFFmpegException {
		sws_scale(convertContext, new PointerPointer(srcPicture), srcPicture.linesize(), 0,
				srcFormat.getHeight(), new PointerPointer(dstPicture), dstPicture.linesize());
	}
	
	public void close() {
		if (convertContext != null) {
			sws_freeContext(convertContext);
			convertContext = null;
        }
	}
	
}
