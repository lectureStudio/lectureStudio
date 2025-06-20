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

import org.lecturestudio.core.view.PresentationViewComponent;
import org.lecturestudio.core.view.PresentationViewContext;
import org.lecturestudio.core.view.PresentationViewType;
import org.lecturestudio.web.api.event.LocalScreenVideoFrameEvent;

/**
 * Implementation of the PresentationViewContext interface for handling screen presentations.
 * This class manages screen view components that consume video frames.
 * It provides functionality to configure components, determine the view type,
 * and distribute screen video frame events to registered listeners.
 *
 * @author Alex Andres
 */
public class ScreenPresentationViewContext implements PresentationViewContext {

	/** List of screen view components that will receive video frame events. */
	private final List<ScreenViewComponent> eventListeners = new ArrayList<>();


	@Override
	public void configure(PresentationViewComponent component) {
		ScreenViewComponent screenComponent = (ScreenViewComponent) component;

		if (!eventListeners.contains(screenComponent)) {
			eventListeners.add(screenComponent);
		}
	}

	@Override
	public PresentationViewType getViewType() {
		return PresentationViewType.SCREEN;
	}

	/**
	 * Processes a screen video frame event and distributes it to registered screen view components.
	 * <p>
	 * This method extracts the video frame from the event, retains it for processing,
	 * sends it to all registered screen view components, and then releases the frame.
	 *
	 * @param event The local screen video frame event to process.
	 */
	public void addScreenVideoFrameEvent(LocalScreenVideoFrameEvent event) {
		final VideoFrame videoFrame = event.getFrame();
		videoFrame.retain();

		for (ScreenViewComponent consumer : eventListeners) {
			consumer.setVideoFrame(videoFrame);
		}

		videoFrame.release();
	}
}
