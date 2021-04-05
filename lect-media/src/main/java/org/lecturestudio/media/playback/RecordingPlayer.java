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

package org.lecturestudio.media.playback;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Iterator;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioPlayer;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.media.avdev.AvdevAudioPlayer;
import org.lecturestudio.core.audio.Player;
import org.lecturestudio.core.audio.SyncState;
import org.lecturestudio.core.audio.bus.AudioBus;
import org.lecturestudio.media.avdev.AVdevAudioOutputDevice;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.audio.source.AudioInputStreamSource;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.EventExecutor;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.StaticShapeAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.media.event.MediaPlayerProgressEvent;
import org.lecturestudio.media.event.MediaPlayerStateEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecordingPlayer extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(RecordingPlayer.class);

	private final ApplicationContext context;

	private final AudioConfiguration audioConfig;

	private final SyncState syncState = new SyncState();

	private RandomAccessAudioStream audioStream;

	private Recording recording;

	private Time duration;

	private EventExecutor actionExecutor;

	private Player audioPlayer;

	private ToolController toolController;

	private int seekTime;

	private int previousPage;

	private boolean seeking;

	private MediaPlayerProgressEvent progressEvent;


	public RecordingPlayer(ApplicationContext context, AudioConfiguration audioConfig) {
		this.context = context;
		this.audioConfig = audioConfig;
	}

	public void setRecording(Recording recording) {
		if (!created() && !destroyed()) {
			throw new IllegalStateException("Recording player must have clean state");
		}

		this.recording = recording;
		this.duration = new Time(recording.getRecordedAudio().getAudioStream().getLengthInMillis());
	}

	@Override
	protected void initInternal() throws ExecutableException {
		seekTime = -1;
		previousPage = 0;
		seeking = false;
		syncState.setAudioTime(0);

		toolController = new ToolController(context, context.getDocumentService());
		toolController.start();

		actionExecutor = new FileEventExecutor(toolController, recording.getRecordedEvents().getRecordedPages(), syncState);
		actionExecutor.init();

		try {
			initAudioPlayer(recording.getRecordedAudio());
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		preloadDocument(getDocument(), recording.getRecordedEvents());

		progressEvent = new MediaPlayerProgressEvent(new Time(0), new Time(0), 0, 0);
		progressEvent.setCurrentTime(new Time(0));
		progressEvent.setTotalTime(new Time(recording.getRecordingHeader().getDuration()));
		progressEvent.setPageNumber(1);
		progressEvent.setPageCount(recording.getRecordedEvents().getRecordedPages().size());

		AudioBus.register(this);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (seekTime > -1) {
			try {
				audioPlayer.seek(seekTime);
			}
			catch (Exception e) {
				throw new ExecutableException(e);
			}
		}

		actionExecutor.start();
		audioPlayer.start();

		setSeeking(false);
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (audioPlayer.getState() == ExecutableState.Suspended || audioPlayer.getState() == ExecutableState.Started) {
			audioPlayer.stop();
		}
		if (actionExecutor.getState() == ExecutableState.Suspended || actionExecutor.getState() == ExecutableState.Started) {
			actionExecutor.stop();
		}

		setSeeking(false);
		reset();
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		if (audioPlayer.getState() == ExecutableState.Started) {
			audioPlayer.suspend();
		}
		if (actionExecutor.getState() == ExecutableState.Started) {
			actionExecutor.suspend();
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		AudioBus.unregister(this);

		toolController.destroy();
		audioPlayer.destroy();
		actionExecutor.destroy();
	}

	public RandomAccessAudioStream getAudioStream() {
		return audioStream;
	}

	public Time getDuration() {
		return duration;
	}

	public Recording getRecordingFile() {
		return recording;
	}

	public void setVolume(float volume) {
		if (isNull(audioPlayer)) {
			throw new NullPointerException("Audio player not initialized");
		}

		audioPlayer.setVolume(volume);
	}

	public void selectNextPage() throws Exception {
		Document document = getDocument();
		
		int pageNumber = document.getCurrentPageNumber() + 1;
		if (pageNumber > document.getPageCount() - 1) {
			return;
		}
		
		selectPage(pageNumber, started());
	}

	public void selectPreviousPage() throws Exception {
		Document document = getDocument();

		int pageNumber = document.getCurrentPageNumber() - 1;
		if (pageNumber < 0) {
			return;
		}

		selectPage(pageNumber, started());
	}

	public void selectPage(int pageNumber) throws Exception {
		selectPage(pageNumber, started());
	}

	public void selectPage(Page page) throws Exception {
		int pageNumber = getDocument().getPageIndex(page);

		selectPage(pageNumber, started());
	}

	public synchronized void seek(double time) throws ExecutableException {
		seek((int) (time * duration.getMillis()));
	}

	public synchronized void seek(int timeMs) throws ExecutableException {
		seekTime = timeMs;
		
		if (!seeking) {
			setSeeking(true);
			
			if (started()) {
				suspend();
			}
			reset();
		}
		
		int pageNumber = actionExecutor.getPageNumber(seekTime);
		
		resetPages(pageNumber, previousPage);
		
		actionExecutor.seekByTime(seekTime);
		
		previousPage = pageNumber;
		
		onAudioPlaybackProgress(new Time(timeMs), duration);
	}

	/**
	 * Notify state listeners about the new state.
	 */
	protected void fireStateChanged() {
		super.fireStateChanged();

		ApplicationBus.post(new MediaPlayerStateEvent(getState()));
	}

	private void selectPage(int pageNumber, boolean startPlayback) throws Exception {
		if (started()) {
			suspend();
		}

		setSeeking(false);
		reset();

		Integer time = actionExecutor.seekByPage(pageNumber);

		if (nonNull(time)) {
			audioPlayer.seek(time);

			previousPage = pageNumber;
		}
		else {
			previousPage = 0;
		}

		onAudioPlaybackProgress(new Time(time), duration);

		if (startPlayback) {
			start();
		}
	}

	private void initAudioPlayer(RecordedAudio audio) throws Exception {
		audioStream = audio.getAudioStream().clone();

		AudioFormat sourceFormat = audioStream.getAudioFormat();
		AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.S16LE, sourceFormat.getSampleRate(), sourceFormat.getChannels());

		audioStream.setAudioFormat(targetFormat);
		
		AudioInputStreamSource audioSource = new AudioInputStreamSource(audioStream, targetFormat);
		
		String providerName = audioConfig.getSoundSystem();
		String outputDeviceName = audioConfig.getOutputDeviceName();
		
		AudioOutputDevice outputDevice = AudioUtils.getAudioOutputDevice(providerName, outputDeviceName);
		
		if (providerName.equals("AVdev")) {
			audioPlayer = new AvdevAudioPlayer((AVdevAudioOutputDevice) outputDevice, audioSource, syncState);
		}
		else {
			audioPlayer = new AudioPlayer(outputDevice, audioSource, syncState);
		}
		
		audioPlayer.setProgressListener(this::onAudioPlaybackProgress);
		audioPlayer.setStateListener(this::onAudioStateChange);
		audioPlayer.init();
	}
	
	private void onAudioStateChange(ExecutableState oldState, ExecutableState newState) {
		if (stopped()) {
			return;
		}

		if (newState == ExecutableState.Stopped && getState() != ExecutableState.Stopping) {
			try {
				stop();
			}
			catch (ExecutableException e) {
				LOG.error("Stop media player failed.", e);
			}
		}
	}

	private void onAudioPlaybackProgress(Time progress, Time duration) {
		Document document = getDocument();

		int pageNumber = document.getCurrentPageNumber() + 1;
		int pageCount = document.getPageCount();
		
		progressEvent.setCurrentTime(progress);
		progressEvent.setTotalTime(this.duration);
		progressEvent.setPageNumber(pageNumber);
		progressEvent.setPageCount(pageCount);

		ApplicationBus.post(progressEvent);
	}

	private void preloadDocument(Document doc, RecordedEvents actions) {
		for (RecordedPage recPage : actions.getRecordedPages()) {
			loadStaticShapes(doc, recPage);
		}
	}

	private void loadStaticShapes(Document doc, RecordedPage recPage) {
		Page page = doc.getPage(recPage.getNumber());
		
		if (isNull(page)) {
			return;
		}
		
		Iterator<StaticShapeAction> iter = recPage.getStaticActions().iterator();

		if (iter.hasNext()) {
			// Remember currently selected page.
			int lastPageNumber = doc.getCurrentPageNumber();
			
			// Select the page to which to add static actions.
			doc.selectPage(recPage.getNumber());
			
			while (iter.hasNext()) {
				StaticShapeAction staticAction = iter.next();
				PlaybackAction action = staticAction.getAction();
				
				// Execute static action on selected page.
				try {
					action.execute(toolController);
				}
				catch (Exception e) {
					LOG.error("Execute static action failed.", e);
				}
			}
			
			// Go back to the page which was selected prior preloading.
			doc.selectPage(lastPageNumber);
			
			page.sendChangeEvent();
		}
	}
	
	private void resetView(Page page) {
		// Reset presentation on all view types.
		PresentationParameterProvider ppp = context.getPagePropertyPropvider(ViewType.User);
		PresentationParameter para = ppp.getParameter(page);
		para.resetPageRect();

		ppp = context.getPagePropertyPropvider(ViewType.Presentation);
		para = ppp.getParameter(page);
		para.resetPageRect();

		ppp = context.getPagePropertyPropvider(ViewType.Preview);
		para = ppp.getParameter(page);
		para.resetPageRect();
	}

	private void reset() {
		Document document = getDocument();

		// Reset page content.
		for (int i = 0; i < document.getPageCount(); i++) {
			Page page = document.getPage(i);
			if (page.hasAnnotations()) {
				page.reset();
			}
			
			resetView(page);
		}
		
		// Reset page parameters.
		context.getPagePropertyPropvider(ViewType.User).clearParameters();

		preloadDocument(document, recording.getRecordedEvents());

		toolController.selectPage(0);

		previousPage = 0;
	}

	private void resetPage(int number) {
		Document document = getDocument();
		Page page = document.getPage(number);
		
		if (page.hasAnnotations()) {
			page.reset();
		}
		
		resetView(page);
		
		context.getPagePropertyPropvider(ViewType.User).clearParameter(page);
		
		// Load static page shapes.
		RecordedPage recPage = recording
				.getRecordedEvents().getRecordedPage(number);
		
		loadStaticShapes(document, recPage);
	}

	private void resetPages(int newPage, int previousPage) {
		if (newPage == previousPage) {
			resetPage(newPage);
		}
		else if (newPage < previousPage) {
			int resetPage = newPage;

			while (resetPage <= previousPage) {
				resetPage(resetPage++);
			}
		}
	}
	
	private void setSeeking(boolean seeking) {
		this.seeking = seeking;
		
		if (!seeking) {
			seekTime = -1;
		}
	}
	
	private Document getDocument() {
		return recording.getRecordedDocument().getDocument();
	}

}
