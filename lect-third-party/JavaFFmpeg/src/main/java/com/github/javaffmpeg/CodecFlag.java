package com.github.javaffmpeg;

import org.bytedeco.javacpp.avcodec;

public enum CodecFlag {

    PASS1(avcodec.CODEC_FLAG_PASS1),   ///< Use internal 2pass ratecontrol in first pass mode.
    PASS2(avcodec.CODEC_FLAG_PASS2),   ///< Use internal 2pass ratecontrol in second pass mode.
    GRAY(avcodec.CODEC_FLAG_GRAY),   ///< Only decode/encode grayscale.
    PSNR(avcodec.CODEC_FLAG_PSNR),   ///< error[?] variables will be set during encoding.
    TRUNCATED(avcodec.CODEC_FLAG_TRUNCATED), /** Input bitstream might be truncated at a random
                                                 location instead of only at frame boundaries. */
    INTERLACED_DCT(avcodec.CODEC_FLAG_INTERLACED_DCT), ///< Use interlaced DCT.
    LOW_DELAY(avcodec.CODEC_FLAG_LOW_DELAY), ///< Force low delay.
    GLOBAL_HEADER(avcodec.CODEC_FLAG_GLOBAL_HEADER), ///< Place global headers in extradata instead of every keyframe.
    BITEXACT(avcodec.CODEC_FLAG_BITEXACT), ///< Use only bitexact stuff (except (I)DCT).
    /* Fx : Flag for h263+ extra options */
    AC_PRED(avcodec.CODEC_FLAG_AC_PRED), ///< H.263 advanced intra coding / MPEG-4 AC prediction
    LOOP_FILTER(avcodec.CODEC_FLAG_LOOP_FILTER), ///< loop filter
    INTERLACED_ME(avcodec.CODEC_FLAG_INTERLACED_ME), ///< interlaced motion estimation
    CLOSED_GOP(avcodec.CODEC_FLAG_CLOSED_GOP),
    FAST(avcodec.CODEC_FLAG2_FAST), ///< Allow non spec compliant speedup tricks.
    NO_OUTPUT(avcodec.CODEC_FLAG2_NO_OUTPUT), ///< Skip bitstream encoding.
    LOCAL_HEADER(avcodec.CODEC_FLAG2_LOCAL_HEADER), ///< Place global headers at every keyframe instead of in extradata.
    DROP_FRAME_TIMECODE(avcodec.CODEC_FLAG2_DROP_FRAME_TIMECODE), ///< timecode is in drop frame format. DEPRECATED!!!!
    IGNORE_CROP(avcodec.CODEC_FLAG2_IGNORE_CROP), ///< Discard cropping information from SPS.
    CHUNKS(avcodec.CODEC_FLAG2_CHUNKS), ///< Input bitstream might be truncated at a packet boundaries instead of only at frame boundaries.
    SHOW_ALL(avcodec.CODEC_FLAG2_SHOW_ALL); ///< Show all frames before the first keyframe
	
	
	
	private final long id;
	
	
	private CodecFlag(long id) {
		this.id = id;
	}

	public final long value() {
		return id;
	}
	
	public static CodecFlag byId(int id) {
		for (CodecFlag value : values()) {
			if (value.id == id)
				return value;
		}
		
		return null;
	}
	
}
