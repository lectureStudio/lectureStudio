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

package org.lecturestudio.core.audio;

/**
 * Audio statistics produced by an audio processing module.
 *
 * @author Alex Andres
 */
public class AudioProcessingStats {

	/**
	 * The root-mean-square (RMS) level in dBFS (decibels from digital
	 * full-scale) of the last capture frame, after processing. It is
	 * constrained to [-127, 0].
	 * <p>
	 * The computation follows: https://tools.ietf.org/html/rfc6465 with the
	 * intent that it can provide the RTP audio level indication.
	 * <p>
	 * Only reported if level estimation is enabled via {@code
	 * AudioProcessingSettings}.
	 */
	public int outputRmsDbfs;

	/**
	 * True if voice is detected in the last capture frame, after processing.
	 * <p>
	 * It is conservative in flagging audio as speech, with low likelihood of
	 * incorrectly flagging a frame as voice. Only reported if voice detection
	 * is enabled via {@code AudioProcessingSettings}.
	 */
	public boolean voiceDetected;

	/**
	 * The instantaneous delay estimate produced in the AEC. The unit is in
	 * milliseconds and the value is the instantaneous value at the time of the
	 * call to getStatistics().
	 */
	public int delayMs;

}
