/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.model;

import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.media.video.VideoFrame;

import java.util.Objects;
import java.util.function.Consumer;

public class SharedScreenSource {

	private final String title;

	private final long id;

	private final boolean isWindow;

	private Consumer<VideoFrame> videoFrameConsumer;


	public SharedScreenSource(String title, long id, boolean isWindow) {
		this.title = title;
		this.id = id;
		this.isWindow = isWindow;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public boolean isWindow() {
		return isWindow;
	}

	public void setVideoFrame(VideoFrame videoFrame) {
		if (nonNull(videoFrameConsumer)) {
			videoFrameConsumer.accept(videoFrame);
		}
	}

	public void setVideoFrameConsumer(Consumer<VideoFrame> consumer) {
		this.videoFrameConsumer = consumer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SharedScreenSource that = (SharedScreenSource) o;

		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}