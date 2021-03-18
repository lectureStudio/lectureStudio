package com.github.javaffmpeg;

import org.bytedeco.javacpp.avcodec.AVCodecContext;
import org.bytedeco.javacpp.avcodec.AVPacket;
import org.bytedeco.javacpp.avformat.*;

import static org.bytedeco.javacpp.avcodec.av_free_packet;
import static org.bytedeco.javacpp.avcodec.av_packet_rescale_ts;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;

public class Muxer extends Configurable {

	private String outputPath;
	private String format;

	private AVOutputFormat outputFormat;
	private AVFormatContext formatContext;

	private AVStream videoStream;
	private AVStream audioStream;
	
	private Encoder videoEncoder;
	private Encoder audioEncoder;
	
	private Codec videoCodec;
	private Codec audioCodec;
	
	private Options videoOptions;
	private Options audioOptions;

	private int videoBitrate;
	private int audioBitrate;
	
	private double videoQuality = -1;
	private double audioQuality = -1;

	
	public Muxer(String outputPath) {
		this.outputPath = outputPath;
	}
	
    public void open() throws Exception {
        formatContext = null;
        videoStream = null;
        audioStream = null;

        // Auto detect the output format from the name.
        String format_name = format == null || format.length() == 0 ? null : format;
        if ((outputFormat = av_guess_format(format_name, outputPath, null)) == null) {
            int proto = outputPath.indexOf("://");
            if (proto > 0) {
                format_name = outputPath.substring(0, proto);
            }
            
            if ((outputFormat = av_guess_format(format_name, outputPath, null)) == null)
                throw new JavaFFmpegException("Could not guess output format for " + outputPath);
        }
        format_name = outputFormat.name().getString();

        // Allocate the output media context.
		formatContext = new AVFormatContext(null);
		int ret = avformat_alloc_output_context2(formatContext, outputFormat, format_name, (String) null);
		if (ret < 0) {
			throw new JavaFFmpegException("Could not deduce output format from file extension.");
		}
        
        if (videoCodec != null && getImageWidth() > 0 && getImageHeight() > 0) {
			outputFormat.video_codec(videoCodec.getID().value());

			if ((videoStream = avformat_new_stream(formatContext, videoCodec.getCodec())) == null) {
				release();
				throw new JavaFFmpegException("Could not allocate video stream.");
			}
            
            videoEncoder = new Encoder(videoCodec, videoStream.codec());
            videoEncoder.setMediaType(MediaType.VIDEO);
            videoEncoder.setBitrate(getVideoBitrate());
            videoEncoder.setImageWidth((getImageWidth() + 15) / 16 * 16);
            videoEncoder.setImageHeight(getImageHeight());
            videoEncoder.setFramerate(getFramerate());
            videoEncoder.setGOPSize(getGOPSize());
            videoEncoder.setQuality(getVideoQuality());
            videoEncoder.setPixelFormat(getPixelFormat());
            
            if (getTwoPass()) {
            	if (getPass() == 1) {
            		videoEncoder.setFlag(CodecFlag.PASS1);
            		videoOptions.put("flags", "+pass1");
            	}
            	if (getPass() == 2) {
            		videoEncoder.setFlag(CodecFlag.PASS2);
            		videoOptions.put("flags", "+pass2");
            	}
            }
            
            if (videoCodec.getID() == CodecID.H264) {
            	videoEncoder.setProfile(AVCodecContext.FF_PROFILE_H264_BASELINE);
            	
            	if (getTwoPass()) {
            		videoOptions.put("stats", getProfilePath());
            	}
            }
            if (videoCodec.getID() == CodecID.HEVC) {
            	videoEncoder.setProfile(AVCodecContext.FF_PROFILE_HEVC_MAIN);
            }
            
            if ((outputFormat.flags() & AVFMT_GLOBALHEADER) != 0) {
            	videoEncoder.setFlag(CodecFlag.GLOBAL_HEADER);
            }
            
            if (frameRate > 0) {
            	videoStream.time_base(av_inv_q(av_d2q(frameRate, 1001000)));
			}
        }
        
        /*
         * Add an audio output stream.
         */
        if (audioCodec != null && getAudioChannels() > 0 && getSampleRate() > 0) {
            outputFormat.audio_codec(audioCodec.getID().value());

	        audioStream = avformat_new_stream(formatContext, audioCodec.getCodec());
            
			if (audioStream == null) {
				release();
				throw new JavaFFmpegException("Could not allocate audio stream.");
			}
            
			audioEncoder = new Encoder(audioCodec, audioStream.codec());
			audioEncoder.setMediaType(MediaType.AUDIO);
			audioEncoder.setBitrate(getAudioBitrate());
			audioEncoder.setSampleRate(getSampleRate());
			audioEncoder.setAudioChannels(getAudioChannels());
			audioEncoder.setSampleFormat(getSampleFormat());
			audioEncoder.setQuality(getAudioQuality());
			
            if ((outputFormat.flags() & AVFMT_GLOBALHEADER) != 0)
            	audioEncoder.setFlag(CodecFlag.GLOBAL_HEADER);
            
            audioStream.time_base().num(1).den(sampleRate);
        }
        
        if (videoStream != null)
        	videoEncoder.open(videoOptions);

        if (audioStream != null)
        	audioEncoder.open(audioOptions);
        
        //av_dump_format(formatContext, 0, outputPath, 1);
        
        // Open the output file.
        if ((outputFormat.flags() & AVFMT_NOFILE) == 0) {
            AVIOContext pb = new AVIOContext(null);
            if (avio_open(pb, outputPath, AVIO_FLAG_WRITE) < 0) {
                release();
                throw new JavaFFmpegException("Could not open " + outputPath);
            }
            formatContext.pb(pb);
        }

        // Write the stream header.
        avformat_write_header(formatContext, (AVDictionary) null);
    }

    public void flush() throws JavaFFmpegException {
    	if (formatContext != null) {
			// Write buffered frames.
			while (videoStream != null && flushVideo());
			
			av_interleaved_write_frame(formatContext, null);
			
			while (audioStream != null && flushAudio());

			av_interleaved_write_frame(formatContext, null);
		}
    }
    
    public void close() throws JavaFFmpegException {
		if (formatContext != null) {
			try {
				flush();

				av_write_trailer(formatContext);
			}
			finally {
				release();
			}
		}
    }

    public MediaPacket addImage(VideoFrame frame) throws JavaFFmpegException {
    	if (videoEncoder == null)
    		return null;
    	
    	MediaPacket mediaPacket = videoEncoder.encodeVideo(frame);
    	
    	if (mediaPacket != null) {
    		AVPacket avPacket = mediaPacket.getAVPacket();
    		writePacket(avPacket, videoStream, videoEncoder.getCodec().getContext());
    	}
    	
    	return mediaPacket;
    }
    
    public MediaPacket[] addSamples(AudioFrame frame) throws JavaFFmpegException {
    	if (audioEncoder == null)
    		return null;
    	
    	MediaPacket[] mediaPackets = audioEncoder.encodeAudio(frame);
    	
    	for (MediaPacket mediaPacket : mediaPackets) {
    		if (mediaPacket == null)
    			continue;
    		
    		AVPacket avPacket = mediaPacket.getAVPacket();
    		
    		if (avPacket == null)
    			continue;
    		
    		writePacket(avPacket, audioStream, audioEncoder.getCodec().getContext());
    		
    		av_free_packet(avPacket);
    	}
    	
    	return mediaPackets;
    }
    
    public void setVideoCodec(Codec videoCodec) {
		this.videoCodec = videoCodec;
	}
	
	public void setAudioCodec(Codec audioCodec) {
		this.audioCodec = audioCodec;
	}
	
	public void setVideoQuality(double videoQuality) {
		this.videoQuality = videoQuality;
	}
	
	public double getVideoQuality() {
		return videoQuality;
	}

	public void setAudioQuality(double audioQuality) {
		this.audioQuality = audioQuality;
	}
	
	public double getAudioQuality() {
		return audioQuality;
	}
	
	public void setVideoOptions(Options options) {
		this.videoOptions = options;
	}
    
	public void setAudioOptions(Options options) {
		this.audioOptions = options;
	}

	public void setVideoBitrate(int bitrate) {
		this.videoBitrate = bitrate;
	}

	public int getVideoBitrate() {
		return videoBitrate;
	}

	public void setAudioBitrate(int bitrate) {
		this.audioBitrate = bitrate;
	}

	public int getAudioBitrate() {
		return audioBitrate;
	}
	
    private boolean flushVideo() throws JavaFFmpegException {
		MediaPacket mediaPacket = videoEncoder.flushVideo();

		if (mediaPacket == null)
			return false;

		AVPacket avPacket = mediaPacket.getAVPacket();
		// Write flushed video.
		writePacket(avPacket, videoStream, videoEncoder.getCodec().getContext());

		return mediaPacket.isKeyFrame();
    }
    
    private boolean flushAudio() throws JavaFFmpegException {
        MediaPacket mediaPacket = audioEncoder.flushAudio();
        
        if (mediaPacket == null)
            return false;
        
        AVPacket avPacket = mediaPacket.getAVPacket();
        // Write flushed audio.
    	writePacket(avPacket, audioStream, audioEncoder.getCodec().getContext());
        
        return true;
    }

    private void writePacket(AVPacket avPacket, AVStream stream, AVCodecContext context) throws JavaFFmpegException {
		AVRational inTimeBase = context.time_base();
		AVRational outTimeBase = stream.time_base();
		
		// Rescale output packet timestamp values from codec to stream timebase.
		avPacket.stream_index(stream.index());
		
		av_packet_rescale_ts(avPacket, inTimeBase, outTimeBase);
		
		
//		System.out.println(String.format("%d: %d \t timebase: %d",
//				avPacket.stream_index(),
//				avPacket.pts(),
//				outTimeBase.den()
//		));
		

		if (av_interleaved_write_frame(formatContext, avPacket) < 0) {
			throw new JavaFFmpegException("Could not write interleaved frame.");
		}
	}
    
	private void release() throws JavaFFmpegException {
		if (videoEncoder != null) {
			videoEncoder.close();
			videoEncoder = null;
		}
		if (audioEncoder != null) {
			audioEncoder.close();
			audioEncoder = null;
		}

		if (formatContext != null && !formatContext.isNull()) {
			if ((outputFormat.flags() & AVFMT_NOFILE) == 0) {
				// Close the output file.
				avio_close(formatContext.pb());
			}

			// Free the streams.
			int nb_streams = formatContext.nb_streams();
			for (int i = 0; i < nb_streams; i++) {
				//av_free(oc.streams(i).codec());
				av_free(formatContext.streams(i));
			}

			// Free the stream.
			av_free(formatContext);
			formatContext = null;
		}
		
		videoStream = null;
		audioStream = null;
    }
	
}
