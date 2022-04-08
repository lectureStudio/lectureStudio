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

package org.lecturestudio.presenter.api.recording;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.io.RandomAccessStream;
import org.lecturestudio.core.io.WaveHeader;
import org.lecturestudio.core.io.WaveOutputStream;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingHeader;
import org.lecturestudio.core.recording.file.RecordingFileWriter;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.RecordedDocument;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.util.AudioUtils;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.ProgressCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecordingBackup {

	private static final Logger LOG = LogManager.getLogger(RecordingBackup.class);
	
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
	
	private final String backupDir;
	
	private String sessionPathPrefix;
	
	
	public RecordingBackup(String backupDir) throws IOException {
		this.backupDir = backupDir;
		
		initBackupDir(new File(backupDir));
	}
	
	public void writePages(Stack<RecordedPage> pages) {
		// Write recorded events asynchronously to disk.
		executorService.execute(new WriteRecordedEventsTask(pages));
	}
	
	public void writeDocument(Document doc) {
		// Write recorded document asynchronously to disk.
		executorService.execute(new WriteRecordedDocTask(doc));
	}
	
	public boolean hasCheckpoint() {
		return getCheckpoint() != null;
	}
	
	public void writeCheckpoint(String path, ProgressCallback progressCallback) throws Exception {
		String checkpointName = getCheckpoint();
		
		File destFile = new File(path);
		File audioFile = new File(backupDir + File.separator + checkpointName + ".wav");
		File documentFile = new File(backupDir + File.separator + checkpointName + ".pdf");
		File eventsFile = new File(backupDir + File.separator + checkpointName + ".dat");
		
		// Read WAV header.
		InputStream audioFileStream = new RandomAccessStream(audioFile);
		WaveHeader wavHeader = new WaveHeader(audioFileStream);
		audioFileStream.close();
		
		AudioFormat audioFormat = wavHeader.getAudioFormat();
		
		// Fix WAV header.
		RandomAccessFile audioRAStream = new RandomAccessFile(audioFile, "rw");
		WaveOutputStream wavStream = new WaveOutputStream(audioRAStream.getChannel());
		wavStream.setAudioFormat(audioFormat);
		wavStream.close();
		audioRAStream.close();
		
		float bps = AudioUtils.getBytesPerSecond(audioFormat);
		long duration = (long) ((audioFile.length() - wavHeader.getHeaderLength()) / bps) * 1000;
		
		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(audioFile);
		audioStream.reset();

		RecordingHeader fileHeader = new RecordingHeader();
		fileHeader.setDuration(duration);
		
		Recording recording = new Recording();
		recording.setRecordingHeader(fileHeader);
		recording.setRecordedAudio(new RecordedAudio(audioStream));
		recording.setRecordedEvents(new RecordedEvents(FileUtils.getByteArray(eventsFile)));
		recording.setRecordedDocument(new RecordedDocument(FileUtils.getByteArray(documentFile)));

		RecordingFileWriter.write(recording, destFile, progressCallback);
	}
	
	public String getCheckpoint() {
		Path dir = Paths.get(backupDir);
		Date lastDate = null;
		int parts = 0;
		
		// Read the recording directory by filtering the required extensions.
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{dat,pdf,wav}")) {
			for (Path entry : stream) {
				String fileName = entry.getFileName().toString();
				String dateString = FileUtils.stripExtension(fileName);
				Date date = null;
				
				try {
					date = dateFormat.parse(dateString);
				}
				catch (ParseException e) {
					// ignore
				}
				
				if (date != null) {
					if (!date.equals(lastDate)) {
						lastDate = date;
						parts = 0;
					}

					if (fileName.endsWith(".pdf") || fileName.endsWith(".wav")) {
						parts++;
					}
					
					// Need at least document and audio to be present.
					if (parts > 1) {
						return dateString;
					}
				}
			}
		}
		catch (IOException e) {
			LOG.error("Read recording backup failed.", e);
		}

		return null;
	}
	
	private void writeDocumentAsync(Document doc) {
		try {
			String docFile = sessionPathPrefix + ".pdf";
			
			OutputStream outStream = new BufferedOutputStream(new FileOutputStream(docFile));
			doc.toOutputStream(outStream);
			outStream.flush();
			outStream.close();
		}
		catch (Exception e) {
			LOG.error("Write document to a temporary file failed.", e);
		}
	}
	
	private void writeEventsAsync(Stack<RecordedPage> pages) {
		try {
			String eventsFile = sessionPathPrefix + ".dat";
			
			OutputStream eventStream = new BufferedOutputStream(new FileOutputStream(eventsFile));

			for (RecordedPage recPage : pages) {
				byte[] pageData = recPage.toByteArray();
				eventStream.write(pageData);
			}
			
			eventStream.flush();
			eventStream.close();
		}
		catch (Exception e) {
			LOG.error("Write events to a temporary file failed.", e);
		}
	}
	
	public void open() {
		String sessionName = dateFormat.format(new Date());
		sessionPathPrefix = backupDir + File.separator + sessionName;
	}
	
	public void close() {
		
	}
	
	public void clean() {
		Path dir = Paths.get(backupDir);
		
		// Read the recording directory by filtering the required extensions.
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
				"*.{dat,pdf,wav}")) {
			for (Path entry : stream) {
				Files.deleteIfExists(entry);
			}
		}
		catch (IOException e) {
			LOG.error(e);
		}
	}

	public String getAudioFile() {
		return sessionPathPrefix + ".wav";
	}

	public String getSessionPathPrefix() {
		return sessionPathPrefix;
	}

	private void initBackupDir(File dir) throws IOException {
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Couldn't create backup directory: "
					+ dir.getAbsolutePath());
		}
	}



	private class WriteRecordedDocTask implements Runnable {

		private final Document doc;


		WriteRecordedDocTask(Document doc) {
			this.doc = doc;
		}

		@Override
		public void run() {
			writeDocumentAsync(doc);
		}

	}



	private class WriteRecordedEventsTask implements Runnable {

		private final Stack<RecordedPage> pages;


		WriteRecordedEventsTask(Stack<RecordedPage> pages) {
			this.pages = pages;
		}

		@Override
		public void run() {
			writeEventsAsync(pages);
		}

	}
}
