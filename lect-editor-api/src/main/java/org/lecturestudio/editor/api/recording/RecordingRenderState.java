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

package org.lecturestudio.editor.api.recording;

/**
 * Enumeration of possible states during the recording rendering process.
 * These states represent different phases of audio and video rendering,
 * including vector-based rendering and multi-pass encoding.
 */
public enum RecordingRenderState {

	/** Rendering audio from the recording. */
	RENDER_AUDIO,
	/** Rendering video from the recording. */
	RENDER_VIDEO,

	/** Rendering audio with vector graphics processing. */
	RENDER_VECTOR_AUDIO,
	/** Rendering video with vector graphics processing. */
	RENDER_VECTOR_VIDEO,

	/** First pass of multi-pass rendering. */
	PASS_1,
	/** Second pass of multi-pass rendering. */
	PASS_2,

	/** An error occurred during the rendering process. */
	ERROR

}
