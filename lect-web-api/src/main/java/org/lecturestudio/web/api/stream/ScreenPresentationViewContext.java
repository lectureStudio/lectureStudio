/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import dev.onvoid.webrtc.media.video.VideoFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lecturestudio.core.view.PresentationViewComponent;
import org.lecturestudio.core.view.PresentationViewContext;
import org.lecturestudio.core.view.PresentationViewType;
import org.lecturestudio.web.api.event.LocalScreenVideoFrameEvent;

public class ScreenPresentationViewContext implements PresentationViewContext {

	private final List<Consumer<VideoFrame>> eventListeners = new ArrayList<>();


	@Override
	public void configure(PresentationViewComponent component) {
		ScreenViewComponent screenComponent = (ScreenViewComponent) component;

		Consumer<VideoFrame> listener = screenComponent::setVideoFrame;

		if (!eventListeners.contains(listener)) {
			eventListeners.add(screenComponent::setVideoFrame);
		}
	}

	@Override
	public PresentationViewType getViewType() {
		return PresentationViewType.SCREEN;
	}

	public void addScreenVideoFrameEvent(LocalScreenVideoFrameEvent event) {
		VideoFrame videoFrame = event.getFrame();

		for (Consumer<VideoFrame> consumer : eventListeners) {
			consumer.accept(videoFrame);
		}
	}
}
