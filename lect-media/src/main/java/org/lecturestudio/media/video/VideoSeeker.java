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

package org.lecturestudio.media.video;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bytedeco.javacv.Frame;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.action.ScreenAction;

/**
 * Retrieves video frames of recorded videos during a presentation. A recorded presentation may have multiple recorded
 * videos that can be post-processed and exported. This class is mainly used to seek fast through the recorded videos.
 * 
 * @author Alex Andres
 */
public class VideoSeeker {

	/** Mapping of ScreenActions to corresponding VideoReaders. */
	private final Map<Map.Entry<Recording, ScreenAction>, VideoReader> screenVideoReaders = new HashMap<>();


	/**
	 * Selects a recording for video rendering and seeking within the recorded video. A recorded video is represented by
	 * a ScreenAction. Each recorded ScreenAction has its own video reader allocated.
	 *
	 * @param recording The recording that contains recorded videos.
	 *
	 * @throws ExecutableException If the recorded videos could not be loaded.
	 * 
	 * @see #seekToVideoKeyFrame(long)
	 */
	public void selectRecording(Recording recording) throws ExecutableException {
		var recordedPages = recording.getRecordedEvents().getRecordedPages();

		for (var page : recordedPages) {
			for (var action : page.getPlaybackActions()) {
				// Each ScreenAction has its own VideoReader.
				if (action instanceof ScreenAction screenAction) {
					VideoReader videoReader = new VideoReader(recording.getSourceFile().getParentFile());
					videoReader.setVideoFile(screenAction.getFileName());
					videoReader.setVideoOffset(screenAction.getVideoOffset());
					videoReader.setVideoLength(screenAction.getVideoLength());
					videoReader.start();

					screenVideoReaders.put(Map.entry(recording, screenAction), videoReader);
				}
			}
		}
	}

	/**
	 * Removes the recording and clears all allocated resources used for video rendering.
	 *
	 * @param recording The recording and corresponding resources to remove.
	 *
	 * @throws ExecutableException If the resources could not be disposed.
	 */
	public void removeRecording(Recording recording) throws ExecutableException {
		for (var entry : screenVideoReaders.entrySet()) {
			Map.Entry<Recording, ScreenAction> actionEntry = entry.getKey();

			if (actionEntry.getKey() == recording) {
				screenVideoReaders.remove(entry);

				VideoReader videoReader = entry.getValue();
				if (nonNull(videoReader) && (videoReader.started() || videoReader.suspended())) {
					videoReader.stop();
					videoReader.destroy();
				}
				break;
			}
		}
	}

	/**
	 * Jumps to the specified timestamp in a recorded video and retrieves the video keyframe at this position.
	 *
	 * @param timeMs The timestamp in milliseconds.
	 *
	 * @return The video frame at the specified timestamp or {@code null} if no frame found.
	 *
	 * @throws IOException If the video keyframe could not be retrieved.
	 *
	 * @see #selectRecording(Recording)
	 */
	public Frame seekToVideoKeyFrame(long timeMs) throws IOException {
		for (var entry : screenVideoReaders.entrySet()) {
			Map.Entry<Recording, ScreenAction> actionEntry = entry.getKey();
			VideoReader videoReader = entry.getValue();
			ScreenAction action = actionEntry.getValue();

			long actionTime = action.getTimestamp();
			long actionTimeEnd = actionTime + action.getVideoOffset() + action.getVideoLength();

			if (actionTime < timeMs && actionTimeEnd > timeMs) {
				long videoTime = timeMs - actionTime;

				return videoReader.seekToVideoKeyFrame(videoTime);
			}
		}

		return null;
	}
}
