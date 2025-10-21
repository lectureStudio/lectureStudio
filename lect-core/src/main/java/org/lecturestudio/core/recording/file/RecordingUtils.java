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

package org.lecturestudio.core.recording.file;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.io.WaveOutputStream;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.ScreenAction;
import org.lecturestudio.core.util.ProgressCallback;

/**
 * Utility class providing methods for analyzing recording data.
 * <p>
 * This class contains various helper methods to examine recordings from the lecture
 * studio platform, including functionality to check for specific content types,
 * analyze time intervals, and process recording components.
 * <p>
 * The utilities in this class work with the {@link Recording} model and its
 * associated components like {@link RecordedPage} and {@link PlaybackAction}.
 *
 * @author Alex Andres
 */
public final class RecordingUtils {

	/**
	 * Determines if a specified time interval contains a screen section in the recording.
	 * <p>
	 * Screen sections are typically video segments embedded within the recording. This method
	 * examines the recorded pages and their playback actions to check if any screen actions
	 * (video segments) overlap with the specified time interval.
	 * <p>
	 * The method performs the following steps:
	 * <ul>
	 *   <li>Converts normalized time values to milliseconds</li>
	 *   <li>Locates the page containing the start time</li>
	 *   <li>Checks for screen action overlap with the specified interval</li>
	 * </ul>
	 *
	 * @param start     The normalized start time (between 0 and 1) of the interval to check.
	 * @param end       The normalized end time (between 0 and 1) of the interval to check.
	 * @param recording The recording to examine for screen sections.
	 *
	 * @return {@code true} if the specified interval contains or overlaps with a screen section.
	 */
	public static boolean containsScreenSection(double start, double end, Recording recording) {
		List<RecordedPage> recordedPages = recording.getRecordedEvents().getRecordedPages();
		long duration = recording.getRecordingHeader().getDuration();
		int startMs = (int) (start * duration);
		int endMs = (int) (end * duration);

		RecordedPage startPage = null;

		// Find the page that contains a screen section.
		for (RecordedPage page : recordedPages) {
			int timestamp = page.getTimestamp();

			if (startMs == timestamp) {
				// Found an exact match - use this page and exit the loop.
				startPage = page;
				break;
			}
			else if (startMs < timestamp) {
				// The current page is after our start time - use the last found page.
				break;
			}

			startPage = page;
		}

		// No appropriate page was found, the selection cannot be a screen section.
		if (isNull(startPage)) {
			return false;
		}

		// The time interval of a screen section.
		Interval<Integer> screenTimeInterval;

		// Check all playback actions on the found page.
		for (PlaybackAction action : startPage.getPlaybackActions()) {
			int timestamp = action.getTimestamp();

			// If we've reached actions after the end time, no need to check further.
			if (timestamp > endMs) {
				return false;
			}

			if (action.getType() == ActionType.SCREEN) {
				ScreenAction screenAction = (ScreenAction) action;
				// Calculate the time interval covered by this screen section.
				screenTimeInterval = new Interval<>(timestamp, timestamp + screenAction.getVideoLength());

				if (screenTimeInterval.contains(startMs) || screenTimeInterval.contains(endMs)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Retrieves all screen-related actions from the provided recording.

	 * @param recording The recording to extract screen actions from.
	 *
	 * @return A list containing all screen actions in the recording.
	 */
	public static List<ScreenAction> getScreenActions(Recording recording) {
		return recording.getRecordedEvents().getRecordedPages().stream()
				.flatMap(page -> page.getPlaybackActions().stream())
				.filter(action -> action.getType() == ActionType.SCREEN)
				.map(action -> (ScreenAction) action)
				.toList();
	}

	/**
	 * Validates that all screen actions in the recording have corresponding video files.
	 * <p>
	 * This method checks if the video files referenced by screen actions in the recording
	 * actually exist in the filesystem. If any referenced video file is missing,
	 * a FileNotFoundException is thrown.
	 *
	 * @param recording The recording whose screen actions need to be validated.
	 *
	 * @throws FileNotFoundException If any referenced video file is not found.
	 */
	public static void validateScreenActions(Recording recording) throws FileNotFoundException {
		Path sourcePath = Paths.get(recording.getSourceFile().getParent());
		List<ScreenAction> actions = getScreenActions(recording);

		for (ScreenAction action : actions) {
			Path videoFile = sourcePath.resolve(action.getFileName());
			if (Files.notExists(videoFile)) {
				throw new FileNotFoundException(videoFile.toString());
			}
		}
	}

	/**
	 * Exports the audio from a recording to a separate audio file.
	 * <p>
	 * This method extracts the audio stream from the provided recording and
	 * saves it as a separate audio file. The export progress can be monitored
	 * through the provided callback.
	 *
	 * @param recFile   The recording containing the audio to export.
	 * @param audioFile The destination file where the audio will be saved.
	 * @param callback  A callback to monitor the export progress.
	 *
	 * @throws Exception If an error occurs during the audio extraction or export process.
	 */
	public static void exportAudio(Recording recFile, File audioFile, ProgressCallback callback) throws Exception {
		exportAudio(recFile.getRecordedAudio().getAudioStream(), audioFile, callback);
	}

	/**
	 * Exports the provided audio stream to a separate audio file.
	 * <p>
	 * This method processes the given audio stream and saves it as a separate audio file.
	 * The method adds a short silenced section at the beginning of the audio to prevent
	 * potential clicks. The export progress can be monitored through the provided callback.
	 *
	 * @param audioStream The audio stream to export.
	 * @param audioFile   The destination file where the audio will be saved.
	 * @param callback    A callback to monitor the export progress.
	 *
	 * @throws IOException          If an I/O error occurs during the export process.
	 * @throws NullPointerException If the provided audioFile is null.
	 */
	public static void exportAudio(RandomAccessAudioStream audioStream, File audioFile, ProgressCallback callback) throws IOException {
		if (isNull(audioFile)) {
			throw new NullPointerException("No audio export file provided.");
		}
		if (audioFile.exists()) {
			audioFile.delete();
		}

		RandomAccessAudioStream stream = null;
		FileOutputStream fileStream = null;
		WaveOutputStream outStream = null;

		try {
			stream = audioStream.clone();

			long totalBytes = stream.getLength() - 44;
			long totalRead = 0;

			updateProgress(callback, 0, totalBytes);

			fileStream = new FileOutputStream(audioFile);
			outStream = new WaveOutputStream(fileStream.getChannel());
			outStream.setAudioFormat(stream.getAudioFormat());

			byte[] buffer = new byte[8192];
			byte[] silenced = AudioUtils.silenceAudio(stream, stream.getAudioFormat(), 20);
			int read;

			totalRead += silenced.length;

			// Write silenced audio at the beginning to avoid eventual clicks.
			outStream.write(silenced, 0, silenced.length);

			while ((read = stream.read(buffer)) > 0) {
				outStream.write(buffer, 0, read);

				totalRead += read;

				updateProgress(callback, totalRead, totalBytes);
	        }
		}
		finally {
			if (nonNull(outStream)) {
				outStream.close();
			}
			if (nonNull(stream)) {
				stream.close();
			}
			if (nonNull(fileStream)) {
				fileStream.close();
			}
		}
	}

	/**
	 * Updates the progress callback with the current export progress.
	 * <p>
	 * This helper method calculates the progress as a fraction of the total
	 * and invokes the provided callback if not null.
	 *
	 * @param callback The progress callback to update.
	 * @param current  The current number of bytes processed.
	 * @param total    The total number of bytes to process.
	 */
	private static void updateProgress(ProgressCallback callback, long current, long total) {
		if (nonNull(callback)) {
			callback.onProgress(current / (float) total);
		}
	}

}
