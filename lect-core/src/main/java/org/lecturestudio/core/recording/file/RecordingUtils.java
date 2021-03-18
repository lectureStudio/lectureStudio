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
import java.io.FileOutputStream;

import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.io.WaveOutputStream;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.util.ProgressCallback;

public final class RecordingUtils {

	public static void exportAudio(Recording recFile, File audioFile, ProgressCallback callback) throws Exception {
		exportAudio(recFile.getRecordedAudio().getAudioStream(), audioFile, callback);
	}

	public static void exportAudio(RandomAccessAudioStream audioStream, File audioFile, ProgressCallback callback) throws Exception {
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
			int read;

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

	private static void updateProgress(ProgressCallback callback, long current, long total) {
		if (nonNull(callback)) {
			callback.onProgress(current / (float) total);
		}
	}

}
