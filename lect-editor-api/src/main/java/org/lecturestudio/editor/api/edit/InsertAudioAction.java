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

package org.lecturestudio.editor.api.edit;

import dev.onvoid.webrtc.media.audio.AudioConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.sink.WavFileSink;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.util.AudioUtils;

public class InsertAudioAction extends RecordingInsertAction<RecordedAudio> {

	private RandomAccessAudioStream oldStream;

	private File newStreamFile;


	public InsertAudioAction(RecordedAudio recordedObject, RecordedAudio audio, int startTime) {
		super(recordedObject, audio, startTime);
	}

	@Override
	public void undo() throws RecordingEditException {
		try {
			getRecordedObject().setAudioStream(oldStream);

			newStreamFile.delete();
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		RandomAccessAudioStream insStream = objectToInsert.getAudioStream();
		RandomAccessAudioStream stream = getRecordedObject().getAudioStream();
		AudioFormat audioFormat = stream.getAudioFormat();
		AudioFormat insAudioFormat = insStream.getAudioFormat();

		oldStream = stream.clone();

		long startBytePosition = AudioUtils.getAudioBytePosition(audioFormat, startTime);
		byte[] buffer = new byte[8192];
		int readTotal = 0;
		int read;

		try {
			newStreamFile = Files.createTempFile("lect-editor-", ".wav").toFile();

			WavFileSink fileOutputStream = new WavFileSink(newStreamFile);
			fileOutputStream.setAudioFormat(audioFormat);
			fileOutputStream.open();

			// Copy current audio stream.
			boolean done = false;
			stream.reset();

			while (!done && (read = stream.read(buffer)) > 0) {
				readTotal += read;

				if (read >= startBytePosition) {
					read = (int) startBytePosition;
					done = true;
				}
				else if (readTotal >= startBytePosition) {
					read -= (int) (readTotal - startBytePosition);
					done = true;
				}

				fileOutputStream.write(buffer, 0, read);
			}

			// Copy inserted audio stream.
			if (audioFormat.equals(insAudioFormat)) {
				while ((read = insStream.read(buffer)) > 0) {
					fileOutputStream.write(buffer, 0, read);
				}
			}
			else {
				// Audio formats do not match. Need audio conversion.
				int insSampleRate = insAudioFormat.getSampleRate();
				int insChannels = insAudioFormat.getChannels();

				AudioConverter converter = new AudioConverter(
						insSampleRate, insChannels,
						audioFormat.getSampleRate(), audioFormat.getChannels());

				// kHz / 100 (10 ms frame) * channels * 2 (16-bit PCM sample)
				byte[] input = new byte[insSampleRate / 100 * insChannels * 2];

				// Fill the input buffer...
				while ((read = insStream.read(input)) > 0) {
					byte[] output = new byte[converter.getTargetBufferSize()];

					// 'nConverted' represents the number of samples in the output buffer.
					int nConverted = converter.convert(input, output);

					// write(output, 0, nConverted * 2); (16-bit PCM sample)
					fileOutputStream.write(output, 0, nConverted * 2);
				}

				converter.dispose();
			}

			// Copy current audio stream.
			stream.reset();
			stream.skip(startBytePosition);

			while ((read = stream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, read);
			}

			fileOutputStream.close();

			getRecordedObject().setAudioStream(new RandomAccessAudioStream(newStreamFile));
		}
		catch (Exception e) {
			throw new RecordingEditException(e);
		}
	}
}
