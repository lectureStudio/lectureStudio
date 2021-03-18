package com.github.javaffmpeg;

import org.bytedeco.javacpp.avutil;

public enum MediaType {

	UNKNOWN		(avutil.AVMEDIA_TYPE_UNKNOWN),
	VIDEO		(avutil.AVMEDIA_TYPE_VIDEO),
	AUDIO		(avutil.AVMEDIA_TYPE_AUDIO),
	DATA		(avutil.AVMEDIA_TYPE_DATA),
	SUBTITLE	(avutil.AVMEDIA_TYPE_SUBTITLE),
	ATTACHMENT	(avutil.AVMEDIA_TYPE_ATTACHMENT),
	NB			(avutil.AVMEDIA_TYPE_NB);
	
	
	private final int id;
	
	
	private MediaType(int id) {
		this.id = id;
	}

	public final int value() {
		return id;
	}
	
	public static MediaType byId(int id) {
		for (MediaType value : values()) {
			if (value.id == id)
				return value;
		}
		
		return null;
	}
	
}
