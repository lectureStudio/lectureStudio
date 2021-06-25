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

import org.lecturestudio.core.io.DigestRandomAccessFile;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingHeader;
import org.lecturestudio.core.util.ProgressCallback;

import java.io.File;

public final class RecordingFileWriter {

	public static int write(Recording recFile, File destFile) throws Exception {
		return write(recFile, destFile, null);
	}

	public static int write(Recording recFile, File destFile, ProgressCallback progressCallback) throws Exception {
		if (destFile.exists()) {
			destFile.delete();
		}

		DigestRandomAccessFile raFile = new DigestRandomAccessFile(destFile, "rw", RecordingHeader.CHECKSUM_ALGORITHM);

		RecordingHeader header = recFile.getRecordingHeader();
		RandomAccessAudioStream audioStream = recFile.getRecordedAudio().getAudioStream().clone();
		audioStream.reset();

		byte[] eventData = recFile.getRecordedEvents().toByteArray();
		byte[] docData = recFile.getRecordedDocument().toByteArray();

		int headerLength = header.getHeaderLength();
		int eventsLength = eventData.length;
		int documentLength = docData.length;
		int audioLength = (int) audioStream.getLength();
		int screenCaptureLength = (int) recFile.getRecordedScreenCapture().getScreenCaptureStream().getTotalBytesWritten();
		int totalSize = headerLength + eventsLength + documentLength + audioLength + screenCaptureLength;

		float written = headerLength;

		// Skip the header and write it when the file checksum is computed.
		raFile.seek(headerLength);

		// Write events.
		raFile.write(eventData);

		written += eventsLength;
		setProgress(written / totalSize, progressCallback);

		// Write document.
		raFile.write(docData);

		written += documentLength;
		setProgress(written / totalSize, progressCallback);

		// Write audio.
		byte[] audioBuffer = new byte[4096];

		while (true) {
			int bytesRead = audioStream.read(audioBuffer);
			if (bytesRead == -1) {
				break;
			}

			raFile.write(audioBuffer, 0, bytesRead);

			written += bytesRead;
			setProgress(written / totalSize, progressCallback);
		}

		audioStream.close();

		// Update file header.
		byte[] checksum = raFile.getDigest();

		// Set header values.
		header.setVersion(Recording.FORMAT_VERSION);
		header.setChecksum(checksum);
		header.setEventsLength(eventsLength);
		header.setDocumentLength(documentLength);
		header.setAudioLength(audioLength);
		header.setScreenCaptureLength(screenCaptureLength);

		System.out.println(header);

		// Write file header at the beginning of the file.
		raFile.seek(0);
		raFile.write(header.toByteArray());
		raFile.close();

		written += headerLength;
		setProgress(written / totalSize, progressCallback);

		return totalSize;
	}

	private static void setProgress(float progress, ProgressCallback progressCallback) {
		if (progressCallback != null) {
			progressCallback.onProgress(progress);
		}
	}

}
