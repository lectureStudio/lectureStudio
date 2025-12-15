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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioPlayer;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.SyncState;
import org.lecturestudio.core.audio.bus.AudioBus;
import org.lecturestudio.core.audio.device.AudioDevice;
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
import org.lecturestudio.media.video.VideoPlayer;
import org.lecturestudio.media.video.VideoRenderSurface;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A player that manages the synchronized playback of recorded lecture content.
 * <p>
 * This class coordinates the playback of audio, video, and document events from a recording.
 * It provides functionality for controlling playback (play, pause, stop), seeking to specific
 * positions, navigating between pages, and managing audio settings.
 * </p>
 * <p>
 * The player maintains synchronization between audio playback and recorded events to ensure
 * that document annotations appear at the correct times. It also supports preloading of static
 * content and proper page navigation during playback.
 * </p>
 *
 * @author Alex Andres
 */
public class RecordingPlayer extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(RecordingPlayer.class);

	/** The application context providing access to application services. */
	private final ApplicationContext context;

	/** Audio configuration settings for playback. */
	private final AudioConfiguration audioConfig;

	/** Provider for audio system functionality. */
	private final AudioSystemProvider audioSystemProvider;

	/** Tracks synchronization state between audio and events. */
	private final SyncState syncState = new SyncState();

	/** Stream for random access to audio data. */
	private RandomAccessAudioStream audioStream;

	/** The recording being played. */
	private Recording recording;

	/** Total duration of the recording. */
	private Time duration;

	/** Executes recorded events during playback. */
	private EventExecutor actionExecutor;

	/** Handles audio playback. */
	private AudioPlayer audioPlayer;

	/** Handles video playback for the recording. */
	private VideoPlayer videoPlayer;

	/** Controls document tools during playback. */
	private ToolController toolController;

	/** Surface where video frames are rendered. */
	private VideoRenderSurface videoRenderSurface;

	/** Current seek position in milliseconds (-1 if not seeking). */
	private int seekTime;

	/** Index of the previously selected page. */
	private int previousPage;

	/** Flag indicating whether the player is currently in seeking mode. */
	private boolean seeking;

	/** Event for reporting playback progress. */
	private MediaPlayerProgressEvent progressEvent;


	/**
	 * Creates a new RecordingPlayer instance for playing recorded lecture content.
	 *
	 * @param context             The application context providing access to application services.
	 * @param audioConfig         The audio configuration settings for playback.
	 * @param audioSystemProvider The provider for audio system functionality.
	 */
	public RecordingPlayer(ApplicationContext context,
			AudioConfiguration audioConfig,
			AudioSystemProvider audioSystemProvider) {
		this.context = context;
		this.audioConfig = audioConfig;
		this.audioSystemProvider = audioSystemProvider;
	}

	/**
	 * Sets the surface where to render the video frames.
	 *
	 * @param renderSurface The surface where to render the video frames.
	 */
	public void setVideoRenderSurface(VideoRenderSurface renderSurface) {
		videoRenderSurface = renderSurface;
	}

	/**
	 * Sets the recording to be played by this player.
	 * <p>
	 * This method must be called when the player is in a clean state,
	 * meaning either before initialization or after destruction.
	 * </p>
	 *
	 * @param recording The recording to be played.
	 *
	 * @throws IllegalStateException If the player is not in a clean state.
	 */
	public synchronized void setRecording(Recording recording) {
		if (!created() && !destroyed()) {
			throw new IllegalStateException("Recording player must have clean state");
		}

		this.recording = recording;
		this.duration = new Time(recording.getRecordedAudio().getAudioStream().getLengthInMillis());
	}

	@Override
	protected synchronized void initInternal() throws ExecutableException {
		if (isNull(recording)) {
			throw new ExecutableException("A Recording must be provided");
		}
		if (isNull(videoRenderSurface)) {
			throw new ExecutableException("A VideoRenderSurface must be provided");
		}

		seekTime = -1;
		previousPage = 0;
		seeking = false;
		syncState.setAudioTime(0);

		toolController = new ToolController(context, context.getDocumentService());
		toolController.start();

		var pages = new ArrayList<>(recording.getRecordedEvents().getRecordedPages());

		videoPlayer = new VideoPlayer(recording.getSourceFile().getParentFile());
		videoPlayer.setVideoRenderSurface(videoRenderSurface);

		actionExecutor = new FileEventExecutor(toolController, pages, videoPlayer, syncState);
		actionExecutor.init();

		initAudioPlayer(recording.getRecordedAudio());

		preloadDocument(getDocument(), recording.getRecordedEvents());

		progressEvent = new MediaPlayerProgressEvent(new Time(0), new Time(0), 0, 0);
		progressEvent.setCurrentTime(new Time(0));
		progressEvent.setTotalTime(new Time(recording.getRecordingHeader().getDuration()));
		progressEvent.setPageNumber(1);
		progressEvent.setPageCount(recording.getRecordedEvents().getRecordedPages().size());
		progressEvent.setEventNumber(0);

		audioConfig.playbackDeviceNameProperty().addListener((observable, oldDevice, newDevice) -> {
			audioPlayer.setAudioDeviceName(newDevice);
		});

		AudioBus.register(this);
	}

	@Override
	protected synchronized void startInternal() throws ExecutableException {
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
	protected synchronized void stopInternal() throws ExecutableException {
		if (audioPlayer.suspended() || audioPlayer.started()) {
			audioPlayer.stop();
		}
		if (videoPlayer.suspended() || videoPlayer.started()) {
			videoPlayer.stop();
		}
		if (actionExecutor.suspended() || actionExecutor.started()) {
			actionExecutor.stop();
		}

		setSeeking(false);
		reset();
	}

	@Override
	protected synchronized void suspendInternal() throws ExecutableException {
		if (audioPlayer.started()) {
			audioPlayer.suspend();
		}
		if (videoPlayer.started()) {
			videoPlayer.suspend();
		}
		if (actionExecutor.started()) {
			actionExecutor.suspend();
		}
	}

	@Override
	protected synchronized void destroyInternal() throws ExecutableException {
		AudioBus.unregister(this);

		toolController.destroy();
		audioPlayer.destroy();
		actionExecutor.destroy();

		if (!videoPlayer.destroyed()) {
			videoPlayer.destroy();
		}
	}

	/**
	 * Gets the audio stream used for playback.
	 *
	 * @return The random access audio stream for the current recording.
	 */
	public RandomAccessAudioStream getAudioStream() {
		return audioStream;
	}

	/**
	 * Gets the total duration of the current recording.
	 *
	 * @return The total duration as a Time object.
	 */
	public Time getDuration() {
		return duration;
	}

	/**
	 * Gets the elapsed time in the current playback session.
	 *
	 * @return The elapsed time in milliseconds.
	 */
	public long getElapsedTime() {
		return syncState.getAudioTime();
	}

	/**
	 * Sets the audio volume for playback.
	 *
	 * @param volume The volume level (0.0 to 1.0).
	 *
	 * @throws NullPointerException If the audio player has not been initialized.
	 */
	public void setVolume(float volume) {
		if (isNull(audioPlayer)) {
			throw new NullPointerException("Audio player not initialized");
		}

		audioPlayer.setAudioVolume(volume);
	}

	/**
	 * Navigates to the next page in the document.
	 * If the current page is the last page, this method does nothing.
	 *
	 * @throws Exception If page selection fails.
	 */
	public void selectNextPage() throws Exception {
		Document document = getDocument();

		int pageNumber = document.getCurrentPageNumber() + 1;
		if (pageNumber > document.getPageCount() - 1) {
			return;
		}

		selectPage(pageNumber, started());
	}

	/**
	 * Navigates to the previous page in the document.
	 * If the current page is the first page, this method does nothing.
	 *
	 * @throws Exception If page selection fails.
	 */
	public void selectPreviousPage() throws Exception {
		Document document = getDocument();

		int pageNumber = document.getCurrentPageNumber() - 1;
		if (pageNumber < 0) {
			return;
		}

		selectPage(pageNumber, started());
	}

	/**
	 * Selects a specific page by page number.
	 *
	 * @param pageNumber Zero-based index of the page to select.
	 *
	 * @throws Exception If page selection fails.
	 */
	public void selectPage(int pageNumber) throws Exception {
		selectPage(pageNumber, started());
	}

	/**
	 * Selects a specific page by Page object.
	 *
	 * @param page The Page object to select.
	 *
	 * @throws Exception If page selection fails.
	 */
	public void selectPage(Page page) throws Exception {
		int pageNumber = getDocument().getPageIndex(page);

		selectPage(pageNumber, started());
	}

	/**
	 * Stops video playback if the video player is in a started or suspended state.
	 * This method gracefully stops the video component of the recording without
	 * affecting other playback elements.
	 * <p>
	 * If the video player has not been initialized or is already stopped, this method
	 * has no effect.
	 * </p>
	 */
	public void stopVideo() {
		if (nonNull(videoPlayer)) {
			try {
				if (videoPlayer.initialized()) {
					videoPlayer.destroy();
				}
				else if (videoPlayer.suspended() || videoPlayer.started()) {
					videoPlayer.stop();
				}
			}
			catch (ExecutableException e) {
				logException(e, "Stop video player failed.");
			}
		}
	}

	/**
	 * Seeks to a position in the recording using a normalized time value.
	 * The normalized time should be between 0.0 (beginning) and 1.0 (end).
	 *
	 * @param time Normalized time position (between 0.0 and 1.0).
	 *
	 * @throws ExecutableException If seeking fails.
	 */
	public synchronized void seek(double time) throws ExecutableException {
		seek((int) (time * duration.getMillis() + 0.5));
	}

	/**
	 * Seeks to a specific time position in the recording.
	 * This method will:
	 * 1. Enter seeking mode if not already seeking
	 * 2. Reset the current page state
	 * 3. Determine the correct page for seek position
	 * 4. Update the UI with the new position
	 *
	 * @param timeMs Position to seek to in milliseconds.
	 *
	 * @throws ExecutableException If seeking fails.
	 */
	public synchronized void seek(int timeMs) throws ExecutableException {
		if (seekTime == timeMs) {
			return;
		}

		seekTime = timeMs;

		// Only enter the seeking state if not already seeking.
		if (!seeking) {
			setSeeking(true);

		    // Suspend playback if currently playing.
			if (started()) {
				suspend();
			}
		    // Reset document state to prepare for seeking.
			reset();
		}

		// Determine which page contains the target seek position.
		int pageNumber = actionExecutor.getPageNumber(seekTime);

		// Reset and reload pages between previous position and new position.
		resetPages(pageNumber, previousPage);

		// Perform the actual seeking operation to position actions at the requested time.
		actionExecutor.seekByTime(seekTime);

		// Update tracking of the current page position.
		previousPage = pageNumber;

		// Update the UI with new playback position information.
		onAudioPlaybackProgress(new Time(timeMs), duration);
	}

	/**
	 * Notify state listeners about the new state.
	 */
	@Override
	protected void fireStateChanged() {
		super.fireStateChanged();

		ApplicationBus.post(new MediaPlayerStateEvent(getState()));
	}

	private void selectPage(int pageNumber, boolean startPlayback) throws Exception {
		if (started()) {
			suspend();
		}

		// Disable seeking mode since we're performing a direct page selection.
		setSeeking(false);
		// Reset document state to prepare for new page selection.
		reset();

		// Request the timestamp for the selected page from the action executor.
		// This returns the time position in milliseconds where the page begins.
		Integer time = actionExecutor.seekByPage(pageNumber);

		if (nonNull(time)) {
		    // If a valid timestamp was found for the page, position the audio playback
		    // to the beginning of this page.
			audioPlayer.seek(time);
		    // Track the newly selected page as our current position.
			previousPage = pageNumber;
		}
		else {
		    // If no timestamp was found (invalid page), reset to the first page.
			previousPage = 0;
		}

		// Update UI components to reflect the new playback position.
		onAudioPlaybackProgress(new Time(time), duration);

		// If playback was already in progress before page selection,
		// restart playback from the new position.
		if (startPlayback) {
			start();
		}
	}

	private void initAudioPlayer(RecordedAudio audio)
			throws ExecutableException {
		audioStream = audio.getAudioStream().clone();

		AudioFormat sourceFormat = audioStream.getAudioFormat();
		AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.S16LE,
				sourceFormat.getSampleRate(), sourceFormat.getChannels());

		audioStream.setAudioFormat(targetFormat);

		AudioInputStreamSource audioSource = new AudioInputStreamSource(audioStream, targetFormat);
		String outputDeviceName = audioConfig.getPlaybackDeviceName();

		Optional<AudioDevice> opt = Arrays.stream(audioSystemProvider
						.getPlaybackDevices())
				.filter(audioDevice -> audioDevice.getName()
						.equals(audioConfig.getPlaybackDeviceName()))
				.findFirst();

		if (opt.isEmpty()) {
			outputDeviceName = null;
		}
		if (isNull(outputDeviceName)) {
			outputDeviceName = audioSystemProvider.getDefaultPlaybackDevice().getName();
		}

		audioPlayer = audioSystemProvider.createAudioPlayer();
		audioPlayer.setAudioVolume(1.0);
		audioPlayer.setAudioDeviceName(outputDeviceName);
		audioPlayer.setAudioSource(audioSource);
		audioPlayer.setAudioProgressListener(this::onAudioPlaybackProgress);
		audioPlayer.addStateListener(this::onAudioStateChange);
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

		syncState.setAudioTime(progress.getMillis());

		progressEvent.setCurrentTime(progress);
		progressEvent.setTotalTime(this.duration);
		progressEvent.setPageNumber(pageNumber);
		progressEvent.setPageCount(pageCount);
		progressEvent.setPrevEventNumber(progressEvent.getEventNumber());
		progressEvent.setEventNumber(syncState.getEventNumber());

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
			// Remember the currently selected page.
			int lastPageNumber = doc.getCurrentPageNumber();

			// Select the page to which to add static actions.
			doc.selectPage(recPage.getNumber());

			while (iter.hasNext()) {
				StaticShapeAction staticAction = iter.next();
				PlaybackAction action = staticAction.getAction();
				
				// Execute the static action on the selected page.
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
		PresentationParameterProvider ppp = context.getPagePropertyProvider(ViewType.User);
		PresentationParameter para = ppp.getParameter(page);
		para.resetPageRect();

		ppp = context.getPagePropertyProvider(ViewType.Presentation);
		para = ppp.getParameter(page);
		para.resetPageRect();

		ppp = context.getPagePropertyProvider(ViewType.Preview);
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
		context.getPagePropertyProvider(ViewType.User).clearParameters();

		preloadDocument(document, recording.getRecordedEvents());

		previousPage = 0;
	}

	private void resetPage(int number) {
		Document document = getDocument();
		Page page = document.getPage(number);
		
		if (page.hasAnnotations()) {
			page.reset();
		}
		
		resetView(page);
		
		context.getPagePropertyProvider(ViewType.User).clearParameter(page);
		
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
