package com.github.javaffmpeg;

import org.bytedeco.javacpp.BytePointer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avutil.*;

public class Encoder extends Coder {

	private final static Logger LOG = LogManager.getLogger(Encoder.class.getName());

	/** Encoded audio packet buffer */
	private BytePointer audioBuffer;
	
	/** Encoded video packet buffer */
    private BytePointer videoBuffer;
    
    /** Encoded audio packet buffer size */
    private int audioBufferSize;
    
    /** Encoded video packet buffer size */
    private int videoBufferSize;
    
	/** The encoder picture format */
	private PictureFormat dstVideoFormat;
	
	/** The encoder audio format */
	private AudioFormat audioFormat;
	
	/** Source image structure */
    private AVPicture picture;
    
    /** Encoding video frame buffer */
    private BytePointer pictureBuffer;
    
    /** Audio re-sampler that is used to convert provided audio frames into encoder audio format */
    private AudioResampler audioResampler;
    
    /** Picture re-sampler that is used to convert provided pictures into encoder picture format */
	private PictureResampler videoResampler;
    
	private Rational timeBase;
	
    /** Synchronization counter */
    private long sync_opts_audio;
    private long sync_opts_video;
    private long sync_opts_video_out;
    
    
    public Encoder(Codec codec) throws JavaFFmpegException {
    	this(codec, null);
	}
    
    Encoder(Codec codec, AVCodecContext avContext) throws JavaFFmpegException {
    	super(codec, avContext);
    	
		if (this.avContext != null)
			this.avContext.codec_id(codec.getCodec().id());
	}
    
    @Override
    public void open(Map<String, String> options) throws JavaFFmpegException {
	    super.open(options);

		if (getMediaType() == MediaType.VIDEO) {
			avFrame.pts(0); // required by libx264
			
			createVideoBuffer();
			
	        dstVideoFormat = new PictureFormat(avContext.width(), avContext.height(), PixelFormat.byId(avContext.pix_fmt()));
		}
		else if (getMediaType() == MediaType.AUDIO) {
			audioFormat = new AudioFormat();
	        audioFormat.setSampleFormat(SampleFormat.byId(avContext.sample_fmt()));
	        audioFormat.setChannelLayout(ChannelLayout.byId(avContext.channel_layout()));
	        audioFormat.setChannels(avContext.channels());
	        audioFormat.setSampleRate(avContext.sample_rate());
	        
	        audioBufferSize = 256 * 4096;
            audioBuffer = new BytePointer(av_malloc(audioBufferSize));
		}
		
		timeBase = new Rational(avContext.time_base().num(), avContext.time_base().den());
        
        state = State.Opened;
	}
    
    @Override
    public void close() {
//    	if (picture != null) {
//			avpicture_free(picture);
//			picture = null;
//		}
    	
		if (pictureBuffer != null) {
            av_free(pictureBuffer);
            pictureBuffer = null;
        }
    	if (videoBuffer != null) {
            av_free(videoBuffer);
            videoBuffer = null;
        }
    	
		if (videoResampler != null) {
			videoResampler.close();
			videoResampler = null;
		}
		
        if (audioResampler != null) {
        	audioResampler.close();
            audioResampler = null;
        }
        
        super.close();
	}
    
    public MediaPacket encodeVideo(VideoFrame frame) throws JavaFFmpegException {
    	ByteBuffer imageBuffer = null; 
    	
    	if (frame != null) {
    		imageBuffer = frame.getData();
    		
    		int width = frame.getWidth();
    		int height = frame.getHeight();
    		int step = imageBuffer.capacity() / (width * height) * width;
    		int pixelFormat = frame.getPixelFormat().value();
    		
    		BytePointer data = new BytePointer(imageBuffer);
    		PictureFormat srcVideoFormat = frame.getPictureFormat();

    		// re-sample if necessary
    		if (!srcVideoFormat.equals(dstVideoFormat)) {
				if (videoResampler == null)
					videoResampler = new PictureResampler();

				videoResampler.open(srcVideoFormat, dstVideoFormat);
    			
				int codecWidth = avContext.width();
	    		int codecHeight = avContext.height();
				
    			avpicture_fill(picture, data, pixelFormat, width, height);
    			avpicture_fill(new AVPicture(avFrame), pictureBuffer, avContext.pix_fmt(), codecWidth, codecHeight);
    			
    			videoResampler.resample(picture, new AVPicture(avFrame));
    		}
    		else {
    			avpicture_fill(new AVPicture(avFrame), data, pixelFormat, width, height);
    			avFrame.linesize(0, step);
    		}
    	}
    	
		av_init_packet(avPacket);
		avPacket.data(videoBuffer);
		avPacket.size(videoBufferSize);
		
		avFrame.pts(sync_opts_video);
	    avFrame.format(avContext.pix_fmt());
	    avFrame.width(avContext.width());
	    avFrame.height(avContext.height());
		
		if (avFrame.interlaced_frame() != 0) {
            if (avContext.codec().id() == AV_CODEC_ID_MJPEG)
            	avContext.field_order(avFrame.top_field_first() != 0 ? AV_FIELD_TT : AV_FIELD_BB);
            else
            	avContext.field_order(avFrame.top_field_first() != 0 ? AV_FIELD_TB : AV_FIELD_BT);
        }
		else {
			avContext.field_order(AV_FIELD_PROGRESSIVE);
		}
		
		if (avcodec_encode_video2(avContext, avPacket, imageBuffer == null ? null : avFrame, gotFrame) < 0)
			throw new JavaFFmpegException("Could not encode video packet.");
		
		sync_opts_video++;
		
		if (gotFrame[0] != 0) {
			avPacket.pts(sync_opts_video_out);
			avPacket.dts(sync_opts_video_out);
			avPacket.duration(1);
			
			sync_opts_video_out++;
			
			MediaPacket mediaPacket = new MediaPacket(avPacket);
			mediaPacket.setTimebase(timeBase);
			mediaPacket.setPts(avPacket.pts());
			mediaPacket.setDts(avPacket.dts());
			mediaPacket.setKeyFrame(avFrame.key_frame() != 0);
			
			return mediaPacket;
		}
		
		return null;
    }

    public MediaPacket[] encodeAudio(AudioFrame audioFrame) throws JavaFFmpegException {
    	if (audioFormat == null)
    		throw new JavaFFmpegException("Could not encode audio. No audio format specified.");
    	
    	List<MediaPacket> packets = new ArrayList<MediaPacket>();
        AudioFormat srcFormat = audioFrame.getAudioFormat();
        
        AudioFrame[] frames;
        
        // create re-sampler if sample formats does not match
        if (!srcFormat.equals(audioFormat)) {
        	if (audioResampler == null) {
    			audioResampler = new AudioResampler();
    			audioResampler.open(srcFormat, audioFormat, avContext.frame_size());
    		}
    		
    		frames = audioResampler.resample(audioFrame);
    		
    		audioFrame.clear();
        }
        else {
        	frames = new AudioFrame[] { audioFrame };
        }
		
		for (AudioFrame frame : frames) {
			if (avFrame.address() != 0 && avFrame.data().address() != 0) {
				av_frame_unref(avFrame);
			}
			
			for (int i = 0; i < frame.getPlaneCount(); i++) {
				avFrame.data(i, frame.getPlane(i).position(0));
				avFrame.linesize(i, frame.getPlane(i).limit());
			}
		
			avFrame.nb_samples(frame.getSampleCount());
			
			MediaPacket mediaPacket = encodeAudioFrame(avFrame);
	        packets.add(mediaPacket);
	        
	        frame.clear();
		}
    	
        return packets.toArray(new MediaPacket[0]);
    }
    
    public MediaPacket flushVideo() throws JavaFFmpegException {
		return encodeVideo(null);
	}
    
	public MediaPacket flushAudio() throws JavaFFmpegException {
		return encodeAudioFrame(null);
	}

    private MediaPacket encodeAudioFrame(AVFrame frame) throws JavaFFmpegException {
		av_init_packet(avPacket);
		
		avPacket.data(audioBuffer);
		avPacket.size(audioBufferSize);

		if (frame != null) {
			if (frame.pts() == AV_NOPTS_VALUE) {
				frame.pts(sync_opts_audio);
			}
			
			sync_opts_audio = frame.pts() + frame.nb_samples();
		}
		
		if (avcodec_encode_audio2(avContext, avPacket, frame, gotFrame) < 0)
			throw new JavaFFmpegException("Could not encode audio packet.");

		if (gotFrame[0] != 0) {
			// Handle empty packet.
			if (avPacket.duration() == 0)
				return null;
			
			AVPacket copyPacket = new AVPacket();
			
			// Need to copy packet, since the encoder uses common packet buffer.
			av_copy_packet(copyPacket, avPacket);
			
			MediaPacket mediaPacket = new MediaPacket(copyPacket);
			mediaPacket.setTimebase(timeBase);
			mediaPacket.setPts(avPacket.pts());
			mediaPacket.setDts(avPacket.dts());
			mediaPacket.setKeyFrame(avFrame.key_frame() != 0);

			return mediaPacket;
		}
		
		return null;
    }
    
    private void createVideoBuffer() throws JavaFFmpegException {
    	picture = new AVPicture();
        
        if (avpicture_alloc(picture, avContext.pix_fmt(), avContext.width(), avContext.height()) < 0)
        	throw new JavaFFmpegException("Could not allocate decoding picture.");
        
		// like in ffmpeg.c
		videoBufferSize = Math.max(256 * 1024, 8 * avContext.width() * avContext.height());
		videoBuffer = new BytePointer(av_malloc(videoBufferSize));

		int size = avpicture_get_size(avContext.pix_fmt(), avContext.width(), avContext.height());
		if ((pictureBuffer = new BytePointer(av_malloc(size))).isNull()) {
			close();
			throw new JavaFFmpegException("Could not allocate picture buffer.");
		}
    }
    
    public Codec getCodec() {
    	return codec;
    }

    public void setSampleRate(int samplerate) throws JavaFFmpegException {
	    if (codec.getType() != MediaType.AUDIO)
		    throw new JavaFFmpegException("Cannot set sample rate for non-audio codec.");

	    Integer[] sampleRates = codec.getSupportedSampleRates();

	    if (sampleRates != null) {
	    	List<Integer> supportedRates = Arrays.asList(sampleRates);
	    	
	    	if (!supportedRates.contains(samplerate)) {
			    // pick the highest supported sample rate
			    samplerate = supportedRates.get(supportedRates.size() - 1);

			    if (LOG.isWarnEnabled()) {
				    LOG.warn("Sample rate {} is not supported by {}.", samplerate, getCodec().getName());
				    LOG.warn("-> Selected supported sample rate {}.", samplerate);
			    }
		    }
	    }

	    super.setSampleRate(samplerate);
    }
    
    @Override
    public void setAudioChannels(int channels) throws JavaFFmpegException {
	    if (codec.getType() != MediaType.AUDIO)
		    throw new JavaFFmpegException("Cannot set audio channels for non-audio codec.");

	    ChannelLayout[] channelLayouts = getCodec().getSupportedChannelLayouts();

	    if (channelLayouts != null) {
		    long layout = av_get_default_channel_layout(channels);
		    ChannelLayout channelLayout = ChannelLayout.byId(layout);
		    List<ChannelLayout> layouts = Arrays.asList(channelLayouts);

		    if (channelLayout == null || !layouts.contains(channelLayout)) {
		    	int oldChannels = channels;
			    channels = av_get_channel_layout_nb_channels(ChannelLayout.STEREO.value());
			    
			    if (LOG.isWarnEnabled()) {
				    LOG.warn("Codec {} does not support {} channels and channel layout {}.",
						    getCodec().getName(), oldChannels, channelLayout.asString());
				    LOG.warn("-> Selected {} channels as default.", channels);
			    }
		    }
	    }

	    super.setAudioChannels(channels);
    }
    
    public void setFramerate(double framerate) throws JavaFFmpegException {
	    if (codec.getType() != MediaType.VIDEO)
		    throw new JavaFFmpegException("Cannot set frame rate for non-video codec.");

	    AVRational frameRate = av_d2q(framerate, 1001000);
	    AVRational supportedFramerates = codec.getCodec().supported_framerates();

	    if (supportedFramerates != null) {
		    int idx = av_find_nearest_q_idx(frameRate, supportedFramerates);
		    frameRate = supportedFramerates.position(idx);
	    }

	    super.setFramerate(av_q2d(frameRate));
    }
    
    public void setImageWidth(int width) throws JavaFFmpegException {
	    if (codec.getType() != MediaType.VIDEO)
		    throw new JavaFFmpegException("Cannot set image width for non-video codec.");

	    if (width < 0)
		    throw new JavaFFmpegException("Image width cannot be < 0.");

	    super.setImageWidth(width);
    }
    
    public void setImageHeight(int height) throws JavaFFmpegException {
	    if (codec.getType() != MediaType.VIDEO)
		    throw new JavaFFmpegException("Cannot set image height for non-video codec.");

	    if (height < 0)
		    throw new JavaFFmpegException("Image height cannot be < 0.");

	    super.setImageHeight(height);
    }
    
    public void setGOPSize(int gopSize) throws JavaFFmpegException {
	    if (codec.getType() != MediaType.VIDEO)
		    throw new JavaFFmpegException("Cannot set Group Of Pictures for non-video codec.");

	    if (gopSize < 0)
		    throw new JavaFFmpegException("Group Of Pictures cannot be < 0.");

	    super.setGOPSize(gopSize);
    }
    
	public void setPixelFormat(PixelFormat format) throws JavaFFmpegException {
		if (codec.getType() != MediaType.VIDEO)
			throw new JavaFFmpegException("Cannot set pixel format for non-video codec.");

		List<PixelFormat> supportedFormats = Arrays.asList(codec.getSupportedPixelFormats());

		if (format == null || !supportedFormats.contains(format)) {
			// pick the first supported pixel format
			format = supportedFormats.get(0);

			if (LOG.isWarnEnabled()) {
				LOG.warn("No valid pixel format provided for codec: {}", getCodec().getName());
				LOG.warn("-> Selected default supported pixel format: {}", format);
			}
		}

		super.setPixelFormat(format);
	}
	
	public void setSampleFormat(SampleFormat format) throws JavaFFmpegException {
		if (codec.getType() != MediaType.AUDIO)
			throw new JavaFFmpegException("Cannot set sample format for non-audio codec.");

		if (format == null) {
			// Select one of supported sample formats ourselves.
			List<SampleFormat> supported = Arrays.asList(codec.getSupportedSampleFormats());
			// Prioritized formats.
			SampleFormat[] defaults = {
					SampleFormat.S16, SampleFormat.S16P,
					SampleFormat.S32, SampleFormat.S32P,
					SampleFormat.FLT, SampleFormat.FLTP,
					SampleFormat.DBL, SampleFormat.DBLP
			};

			for (SampleFormat defaultFormat : defaults) {
				if (supported.contains(defaultFormat)) {
					format = defaultFormat;
					break;
				}
			}

			if (LOG.isWarnEnabled()) {
				LOG.warn("No sample format provided for codec: {}", getCodec().getName());
				LOG.warn("-> Selected default supported sample format: {}", format);
			}
		}

		if (format == null)
			throw new JavaFFmpegException("Could not set sample format. No format available.");

		super.setSampleFormat(format);
	}

}
