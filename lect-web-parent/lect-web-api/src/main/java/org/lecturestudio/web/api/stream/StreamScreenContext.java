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

package org.lecturestudio.web.api.stream;

import java.util.function.Consumer;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.web.api.event.ScreenVideoFrameEvent;
import org.lecturestudio.web.api.model.ScreenSource;

public class StreamScreenContext {

	private final ObjectProperty<ScreenSource> screenSource;

	private Consumer<ScreenVideoFrameEvent> localFrameConsumer;

	private Runnable screenSourceEnded;

	private final IntegerProperty framerate;

	private final IntegerProperty bitrate;

	private final BooleanProperty sendVideo;


	public StreamScreenContext() {
		screenSource = new ObjectProperty<>();
		framerate = new IntegerProperty();
		bitrate = new IntegerProperty();
		sendVideo = new BooleanProperty();
	}

	public ObjectProperty<ScreenSource> screenSourceProperty() {
		return screenSource;
	}

	public ScreenSource getScreenSource() {
		return screenSource.get();
	}

	public void setScreenSource(ScreenSource source) {
		screenSource.set(source);
	}

	public Runnable getScreenSourceEndedCallback() {
		return screenSourceEnded;
	}

	public void setScreenSourceEndedCallback(Runnable callback) {
		screenSourceEnded = callback;
	}

	public Consumer<ScreenVideoFrameEvent> getLocalFrameConsumer() {
		return localFrameConsumer;
	}

	public void setLocalFrameConsumer(Consumer<ScreenVideoFrameEvent> consumer) {
		this.localFrameConsumer = consumer;
	}

	public IntegerProperty framerateProperty() {
		return framerate;
	}

	public Integer getFramerate() {
		return framerate.get();
	}

	public void setFramerate(int rate) {
		framerate.set(rate);
	}

	public IntegerProperty bitrateProperty() {
		return bitrate;
	}

	public int getBitrate() {
		return bitrate.get();
	}

	public void setBitrate(int rate) {
		bitrate.set(rate);
	}

	public BooleanProperty sendVideoProperty() {
		return sendVideo;
	}

	public boolean getSendVideo() {
		return sendVideo.get();
	}

	public void setSendVideo(boolean send) {
		sendVideo.set(send);
	}
}