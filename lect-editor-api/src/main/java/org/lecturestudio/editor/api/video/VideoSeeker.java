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

package org.lecturestudio.editor.api.video;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bytedeco.javacv.Frame;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.action.ScreenAction;

public class VideoSeeker {

	private final Map<ScreenAction, VideoReader> screenReaders = new HashMap<>();


	public void selectRecording(Recording recording) throws ExecutableException {
		var recordedPages = recording.getRecordedEvents().getRecordedPages();

		for (var page : recordedPages) {
			for (var action : page.getPlaybackActions()) {
				if (action instanceof ScreenAction screenAction) {
					VideoReader videoReader = new VideoReader(recording.getSourceFile().getParentFile());
					videoReader.setVideoFile(screenAction.getFileName());
					videoReader.setVideoOffset(screenAction.getVideoOffset());
					videoReader.setVideoLength(screenAction.getVideoLength());
//					videoReader.setTargetImageSize(renderView.getImageSize());
					videoReader.start();

					screenReaders.put(screenAction, videoReader);
				}
			}
		}
	}

	public Frame seek(long timeMs) throws IOException {
		long t = System.currentTimeMillis();

		for (var entry : screenReaders.entrySet()) {
			ScreenAction action = entry.getKey();
			VideoReader videoReader = entry.getValue();

			long actionTime = entry.getKey().getTimestamp();
			long actionLength = action.getVideoLength();
			long actionTimeEnd = actionTime + action.getVideoOffset() + actionLength;

			if (actionTime < timeMs && actionTimeEnd > timeMs) {
				long videoTime = timeMs - actionTime;

				System.out.println("action: " + videoTime + ", " + action.getVideoLength());

				return videoReader.seekToVideoFrame(videoTime);
			}
		}

		System.out.println(System.currentTimeMillis() - t);

		return null;
	}
}
