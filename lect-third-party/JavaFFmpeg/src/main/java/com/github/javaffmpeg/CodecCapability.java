package com.github.javaffmpeg;

import org.bytedeco.javacpp.avcodec;

public enum CodecCapability {

	/**
	 * Decoder can use draw_horiz_band callback.
	 */
	DRAW_HORIZ_BAND		(avcodec.CODEC_CAP_DRAW_HORIZ_BAND),
	
	/**
	 * Codec uses get_buffer() for allocating buffers and supports custom
	 * allocators. If not set, it might not use get_buffer() at all or use
	 * operations that assume the buffer was allocated by
	 * avcodec_default_get_buffer.
	 */
	DR1					(avcodec.CODEC_CAP_DR1),
	TRUNCATED			(avcodec.CODEC_CAP_TRUNCATED),
	
	/**
	 * Codec can export data for HW decoding (XvMC).
	 */
	HWACCEL				(avcodec.CODEC_CAP_HWACCEL),
	
	/**
	 * Encoder or decoder requires flushing with NULL input at the end in order
	 * to give the complete and correct output.
	 * 
	 * NOTE: If this flag is not set, the codec is guaranteed to never be fed
	 * with with NULL data. The user can still send NULL data to the public
	 * encode or decode function, but libavcodec will not pass it along to the
	 * codec unless this flag is set.
	 * 
	 * Decoders: The decoder has a non-zero delay and needs to be fed with
	 * avpkt->data=NULL, avpkt->size=0 at the end to get the delayed data until
	 * the decoder no longer returns frames.
	 * 
	 * Encoders: The encoder needs to be fed with NULL data at the end of
	 * encoding until the encoder no longer returns data.
	 * 
	 * NOTE: Setting this flag also means that the encoder must set the pts and
	 * duration for each output packet. If this flag is not set, the pts and
	 * duration will be determined by libavcodec from the input frame.
	 */
	DELAY				(avcodec.CODEC_CAP_DELAY),
	
	/**
	 * Codec can be fed a final frame with a smaller size. This can be used to
	 * prevent truncation of the last audio samples.
	 */
	SMALL_LAST_FRAME	(avcodec.CODEC_CAP_SMALL_LAST_FRAME),
	
	/**
	 * Codec can export data for HW decoding (VDPAU).
	 */
	HWACCEL_VDPAU		(avcodec.CODEC_CAP_HWACCEL_VDPAU),
	
	/**
	 * Codec can output multiple frames per AVPacket Normally demuxers return
	 * one frame at a time, demuxers which do not do are connected to a parser
	 * to split what they return into proper frames. This flag is reserved to
	 * the very rare category of codecs which have a bitstream that cannot be
	 * split into frames without timeconsuming operations like full decoding.
	 * Demuxers carring such bitstreams thus may return multiple frames in a
	 * packet. This has many disadvantages like prohibiting stream copy in many
	 * cases thus it should only be considered as a last resort.
	 */
	SUBFRAMES			(avcodec.CODEC_CAP_SUBFRAMES),
	
	/**
	 * Codec is experimental and is thus avoided in favor of non experimental
	 * encoders
	 */
	EXPERIMENTAL		(avcodec.CODEC_CAP_EXPERIMENTAL),
	
	/**
	 * Codec should fill in channel configuration and samplerate instead of
	 * container
	 */
	CHANNEL_CONF		(avcodec.CODEC_CAP_CHANNEL_CONF),
	
	/**
	 * Codec supports frame-level multithreading.
	 */
	FRAME_THREADS		(avcodec.CODEC_CAP_FRAME_THREADS),
	
	/**
	 * Codec supports slice-based (or partition-based) multithreading.
	 */
	SLICE_THREADS		(avcodec.CODEC_CAP_SLICE_THREADS),
	
	/**
	 * Codec supports changed parameters at any point.
	 */
	PARAM_CHANGE		(avcodec.CODEC_CAP_PARAM_CHANGE),
	
	/**
	 * Codec supports avctx->thread_count == 0 (auto).
	 */
	AUTO_THREADS		(avcodec.CODEC_CAP_AUTO_THREADS),
	
	/**
	 * Audio encoder supports receiving a different number of samples in each
	 * call.
	 */
	VARIABLE_FRAME_SIZE	(avcodec.CODEC_CAP_VARIABLE_FRAME_SIZE),
	
	/**
	 * Codec is intra only.
	 */
	INTRA_ONLY			(avcodec.CODEC_CAP_INTRA_ONLY),
	
	/**
	 * Codec is lossless.
	 */
	LOSSLESS			(avcodec.CODEC_CAP_LOSSLESS);
	
	
	private int id;
	
	
	private CodecCapability(int id) {
		this.id = id;
	}
	
	public final int value() {
		return id;
	}
	
	public static CodecCapability byId(int id) {
		for (CodecCapability value : values()) {
			if (value.id == id)
				return value;
		}
		
		return null;
	}
	
}
