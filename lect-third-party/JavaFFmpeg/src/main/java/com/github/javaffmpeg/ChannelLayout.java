package com.github.javaffmpeg;

import org.bytedeco.javacpp.avutil;

public enum ChannelLayout {

	MONO				(avutil.AV_CH_LAYOUT_MONO,				"Mono"),
	STEREO				(avutil.AV_CH_LAYOUT_STEREO,			"Stereo"),
	_2POINT1			(avutil.AV_CH_LAYOUT_2POINT1,			"2.1"),
	_2_1				(avutil.AV_CH_LAYOUT_2_1,				"2_1"),
	SURROUND			(avutil.AV_CH_LAYOUT_SURROUND,			"Surround"),
	_3POINT1			(avutil.AV_CH_LAYOUT_3POINT1,			"3.1"),
	_4POINT0			(avutil.AV_CH_LAYOUT_4POINT0,			"4.0"),
	_4POINT1			(avutil.AV_CH_LAYOUT_4POINT1,			"4.1"),
	_2_2				(avutil.AV_CH_LAYOUT_2_2,				"2_2"),
	QUAD				(avutil.AV_CH_LAYOUT_QUAD,				"Quad"),
	_5POINT0			(avutil.AV_CH_LAYOUT_5POINT0,			"5.0"),
	_5POINT1			(avutil.AV_CH_LAYOUT_5POINT1,			"5.1"),
	_5POINT0_BACK		(avutil.AV_CH_LAYOUT_5POINT0_BACK,		"5.0 Back"),
	_5POINT1_BACK		(avutil.AV_CH_LAYOUT_5POINT1_BACK,		"5.1 Back"),
	_6POINT0			(avutil.AV_CH_LAYOUT_6POINT0,			"6.0"),
	_6POINT0_FRONT		(avutil.AV_CH_LAYOUT_6POINT0_FRONT,		"6.0 Front"),
	HEXAGONAL			(avutil.AV_CH_LAYOUT_HEXAGONAL,			"Hexagonal"),
	_6POINT1			(avutil.AV_CH_LAYOUT_6POINT1,			"6.1"),
	_6POINT1_BACK		(avutil.AV_CH_LAYOUT_6POINT1_BACK,		"6.1 Back"),
	_6POINT1_FRONT		(avutil.AV_CH_LAYOUT_6POINT1_FRONT,		"6.1 Front"),
	_7POINT0			(avutil.AV_CH_LAYOUT_7POINT0,			"7.0"),
	_7POINT0_FRONT		(avutil.AV_CH_LAYOUT_7POINT0_FRONT,		"7.0 Front"),
	_7POINT1			(avutil.AV_CH_LAYOUT_7POINT1,			"7.1"),
	_7POINT1_WIDE		(avutil.AV_CH_LAYOUT_7POINT1_WIDE,		"7.1 Wide"),
	_7POINT1_WIDE_BACK	(avutil.AV_CH_LAYOUT_7POINT1_WIDE_BACK,	"7.1 Wide Back"),
	OCTAGONAL			(avutil.AV_CH_LAYOUT_OCTAGONAL,			"Octagonal"),
	STEREO_DOWNMIX		(avutil.AV_CH_LAYOUT_STEREO_DOWNMIX,	"Stereo Downmix");
	
	
	private final long id;
	private final String name;
	
	
	private ChannelLayout(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public final long value() {
		return id;
	}
	
	public final String asString() {
		return name;
	}
	
	public static ChannelLayout byId(long id) {
		for (ChannelLayout value : values()) {
			if (value.id == id)
				return value;
		}
		
		return null;
	}
	
	public static ChannelLayout byChannelCount(int channels) {
		switch (channels) {
			case 1:
				return MONO;
			case 2:
				return STEREO;
		}
		
		return null;
	}
	
}
