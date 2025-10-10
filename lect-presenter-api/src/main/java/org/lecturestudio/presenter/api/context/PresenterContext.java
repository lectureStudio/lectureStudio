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

package org.lecturestudio.presenter.api.context;

import static java.util.Objects.isNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cef.CefApp;
import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableHashSet;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.util.ObservableSet;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.config.PresenterConfigService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.model.ManualStateObserver;
import org.lecturestudio.presenter.api.model.Stopwatch;
import org.lecturestudio.presenter.api.service.UserPrivilegeService;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.stream.model.CourseParticipant;

/**
 * The PresenterContext class extends the core ApplicationContext to provide
 * functionalities specific to the presentation environment.
 *
 * <p>This context manages various aspects of the presentation system, including:</p>
 * <ul>
 *   <li>Course and participant information</li>
 *   <li>Communication services (messenger, speech requests)</li>
 *   <li>Media controls (streaming, recording, screen sharing)</li>
 *   <li>UI state management</li>
 *   <li>Chromium Embedded Framework integration</li>
 * </ul>
 *
 * <p>The context serves as the central point of access for presentation-related
 * data and state information throughout the application.
 * </p>
 *
 * @author Alex Andres
 */
public class PresenterContext extends ApplicationContext {

	/**
	 * Record that holds the count of classroom and stream participants.
	 *
	 * @param classroomCount The number of participants in the physical classroom.
	 * @param streamCount    The number of participants connected via stream.
	 */
	public record ParticipantCount(int classroomCount, int streamCount) {
	}

	/** Context identifier for slides mode. */
	public static final String SLIDES_CONTEXT = "Slides";

	/** Context identifier for slides to PDF conversion. */
	public static final String SLIDES_TO_PDF_CONTEXT = "SlidesToPDF";

	/** File extension for slide documents. */
	public static final String SLIDES_EXTENSION = "pdf";

	/** File extension for recording files. */
	public static final String RECORDING_EXTENSION = "presenter";

	/** Tracks the currently visible view in the UI. */
	private final ObjectProperty<Class<? extends View>> currentlyVisibleView = new ObjectProperty<>();

	/** The current course being presented. */
	private final ObjectProperty<Course> course = new ObjectProperty<>();

	/** Chromium Embedded Framework application instance. */
	private final ObjectProperty<CefApp> cefApp = new ObjectProperty<>();

	/** Set of participants in the current course. */
	private final ObservableSet<CourseParticipant> courseParticipants = new ObservableHashSet<>();

	/** Count of participants categorized by a presence type. */
	private final ObjectProperty<ParticipantCount> courseParticipantsCount = new ObjectProperty<>();

	/** List of messenger messages currently displayed. */
	private final ObservableList<MessengerMessage> messengerMessages = new ObservableArrayList<>();

	/** Complete history of all received messenger messages. */
	private final List<MessengerMessage> allReceivedMessengerMessages = new ArrayList<>();

	/** Count of current messenger messages. */
	private final IntegerProperty messageCount = new IntegerProperty();

	/** List of pending speech requests from participants. */
	private final ObservableList<SpeechRequestMessage> speechRequests = new ObservableArrayList<>();

	/** Count of pending speech requests. */
	private final IntegerProperty speechRequestCount = new IntegerProperty();

	/** Flag indicating whether the messenger service is active. */
	private final BooleanProperty messengerStarted = new BooleanProperty();

	/** Flag indicating whether streaming is active. */
	private final BooleanProperty streamStarted = new BooleanProperty();

	/** Flag indicating whether the stream should be visible. */
	private final BooleanProperty viewStream = new BooleanProperty();

	/** Flag indicating whether the recording is active. */
	private final BooleanProperty recordingStarted = new BooleanProperty();

	/** Flag indicating whether there are unsaved changes in the recording. */
	private final BooleanProperty hasRecordedChanges = new BooleanProperty();

	/** Flag indicating whether screen sharing is active. */
	private final BooleanProperty screenSharingStarted = new BooleanProperty();

	/** Flag indicating whether the outline should be displayed. */
	private final BooleanProperty showOutline = new BooleanProperty();

	/** Flag indicating whether recording notifications are enabled for users. */
	private final BooleanProperty notifyToRecord = new BooleanProperty();

	/** Service managing user privileges. */
	private final UserPrivilegeService userPrivilegeService;

	/** File containing the application configuration. */
	private final File configFile;

	/** Directory path for storing recordings. */
	private final String recordingDir;

	/** Stopwatch for tracking presentation time. */
	private final Stopwatch stopwatch = new Stopwatch();

	/** Observer for tracking manual states and changes in the presentation. */
	private final ManualStateObserver manualStateObserver = new ManualStateObserver();


	/**
	 * Creates a new presenter context.
	 *
	 * @param dataLocator          The application data locator for finding application resources.
	 * @param configFile           The file containing application configuration.
	 * @param config               The application configuration object.
	 * @param dict                 The dictionary for internationalization.
	 * @param eventBus             The main event bus for application-wide events.
	 * @param audioBus             The event bus specific to audio events.
	 * @param userPrivilegeService The service managing user privileges.
	 */
	public PresenterContext(AppDataLocator dataLocator, File configFile,
			Configuration config, Dictionary dict, EventBus eventBus,
			EventBus audioBus, UserPrivilegeService userPrivilegeService) {
		super(dataLocator, config, dict, eventBus, audioBus);

		this.configFile = configFile;
		this.userPrivilegeService = userPrivilegeService;
		this.recordingDir = getDataLocator().toAppDataPath("recording");

		PresenterConfiguration presenterConfig = (PresenterConfiguration) config;
		setNotifyToRecord(presenterConfig.getNotifyToRecord());

		messengerMessages.addListener(new ListChangeListener<>() {

			@Override
			public void listChanged(ObservableList<MessengerMessage> list) {
				messageCount.set(list.size());
			}
		});

		speechRequests.addListener(new ListChangeListener<>() {

			@Override
			public void listChanged(ObservableList<SpeechRequestMessage> list) {
				speechRequestCount.set(list.size());
			}
		});

		courseParticipants.addListener(set -> {
			int streamCount = 0;
			int classroomCount = 0;

			for (CourseParticipant participant : set) {
				if (isNull(participant.getPresenceType())) {
					continue;
				}

				switch (participant.getPresenceType()) {
					case STREAM -> streamCount++;
					case CLASSROOM -> classroomCount++;
				}
			}

			courseParticipantsCount.set(new ParticipantCount(classroomCount, streamCount));
		});
	}

	@Override
	public PresenterConfiguration getConfiguration() {
		return (PresenterConfiguration) super.getConfiguration();
	}

	@Override
	public void saveConfiguration() throws Exception {
		var configService = new PresenterConfigService();
		configService.save(configFile, getConfiguration());
	}

	/**
	 * Gets the user privilege service that manages user permissions.
	 *
	 * @return The user privilege service instance.
	 */
	public UserPrivilegeService getUserPrivilegeService() {
		return userPrivilegeService;
	}

	/**
	 * Gets the class of the currently visible view in the UI.
	 *
	 * @return The class of the currently visible view.
	 */
	public Class<? extends View> getCurrentlyVisibleView() {
		return currentlyVisibleView.get();
	}

	/**
	 * Sets the currently visible view in the UI.
	 *
	 * @param view The class of the view to set as currently visible.
	 */
	public void setCurrentlyVisibleView(Class<? extends View> view) {
		this.currentlyVisibleView.set(view);
	}

	/**
	 * Gets the property that tracks the currently visible view.
	 *
	 * @return The object property containing the currently visible view class.
	 */
	public ObjectProperty<Class<? extends View>> currentlyVisibleViewProperty() {
		return currentlyVisibleView;
	}

	/**
	 * Gets the current course being presented.
	 *
	 * @return The current course object.
	 */
	public Course getCourse() {
		return course.get();
	}

	/**
	 * Sets the current course for presentation.
	 *
	 * @param course The course object to set as current.
	 */
	public void setCourse(Course course) {
		this.course.set(course);
	}

	/**
	 * Gets the observable property for the current course.
	 *
	 * @return The course property object.
	 */
	public ObjectProperty<Course> courseProperty() {
		return course;
	}

	/**
	 * Gets the Chromium Embedded Framework application instance.
	 *
	 * @return The CefApp instance.
	 */
	public CefApp getCefApp() {
		return cefApp.get();
	}

	/**
	 * Sets the Chromium Embedded Framework application instance.
	 *
	 * @param cefApp The CefApp instance to set.
	 */
	public void setCefApp(CefApp cefApp) {
		this.cefApp.set(cefApp);
	}

	/**
	 * Gets the set of participants in the current course.
	 *
	 * @return The set of course participants.
	 */
	public Set<CourseParticipant> getCourseParticipants() {
		return courseParticipants;
	}

	/**
	 * Gets the observable property for the count of course participants.
	 *
	 * @return The course participants count property.
	 */
	public ObjectProperty<ParticipantCount> courseParticipantsCountProperty() {
		return courseParticipantsCount;
	}

	/**
	 * Gets the list of currently displayed messenger messages.
	 *
	 * @return The list of current messenger messages.
	 */
	public List<MessengerMessage> getMessengerMessages() {
		return messengerMessages;
	}

	/**
	 * Gets the complete history of all received messenger messages.
	 *
	 * @return The list of all received messenger messages.
	 */
	public List<MessengerMessage> getAllReceivedMessengerMessages() {
		return allReceivedMessengerMessages;
	}

	/**
	 * Gets the list of pending speech requests from participants.
	 *
	 * @return The list of speech request messages.
	 */
	public List<SpeechRequestMessage> getSpeechRequests() {
		return speechRequests;
	}

	/**
	 * Gets the property for the count of current messenger messages.
	 *
	 * @return The message count property.
	 */
	public IntegerProperty messageCountProperty() {
		return messageCount;
	}

	/**
	 * Gets the property for the count of pending speech requests.
	 *
	 * @return The speech request count property.
	 */
	public IntegerProperty speechRequestCountProperty() {
		return speechRequestCount;
	}

	/**
	 * Sets whether there are unsaved changes in the recording.
	 *
	 * @param changes True if there are unsaved changes, false otherwise.
	 */
	public void setHasRecordedChanges(boolean changes) {
		hasRecordedChanges.set(changes);
	}

	/**
	 * Checks if there are unsaved changes in the recording.
	 *
	 * @return True if there are unsaved changes, false otherwise.
	 */
	public boolean hasRecordedChanges() {
		return hasRecordedChanges.get();
	}

	/**
	 * Gets the property for tracking unsaved recording changes.
	 *
	 * @return The property tracking recording changes.
	 */
	public BooleanProperty hasRecordedChangesProperty() {
		return hasRecordedChanges;
	}

	/**
	 * Sets whether streaming is active.
	 *
	 * @param started True if streaming is active, false otherwise.
	 */
	public void setStreamStarted(boolean started) {
		streamStarted.set(started);
	}

	/**
	 * Checks if streaming is currently active.
	 *
	 * @return True if streaming is active, false otherwise.
	 */
	public boolean getStreamStarted() {
		return streamStarted.get();
	}

	/**
	 * Gets the property for tracking streaming status.
	 *
	 * @return The stream started property.
	 */
	public BooleanProperty streamStartedProperty() {
		return streamStarted;
	}

	/**
	 * Sets whether the stream should be visible.
	 *
	 * @param view True if the stream should be visible, false otherwise.
	 */
	public void setViewStream(boolean view) {
		viewStream.set(view);
	}

	/**
	 * Checks if the stream is visible.
	 *
	 * @return True if the stream is visible, false otherwise.
	 */
	public boolean getViewStream() {
		return viewStream.get();
	}

	/**
	 * Gets the property for tracking stream visibility.
	 *
	 * @return The view stream property.
	 */
	public BooleanProperty viewStreamProperty() {
		return viewStream;
	}

	/**
	 * Sets whether the messenger service is active.
	 *
	 * @param started True if the messenger service is active, false otherwise.
	 */
	public void setMessengerStarted(boolean started) {
		messengerStarted.set(started);
	}

	/**
	 * Checks if the messenger service is active.
	 *
	 * @return True if the messenger service is active, false otherwise.
	 */
	public boolean getMessengerStarted() {
		return messengerStarted.get();
	}

	/**
	 * Gets the property for tracking messenger service status.
	 *
	 * @return The messenger started property.
	 */
	public BooleanProperty messengerStartedProperty() {
		return messengerStarted;
	}

	/**
	 * Sets whether recording is active.
	 *
	 * @param started True if recording is active, false otherwise.
	 */
	public void setRecordingStarted(boolean started) {
		recordingStarted.set(started);
	}

	/**
	 * Checks if recording is active.
	 *
	 * @return True if recording is active, false otherwise.
	 */
	public boolean getRecordingStarted() {
		return recordingStarted.get();
	}

	/**
	 * Gets the property for tracking recording status.
	 *
	 * @return The recording started property.
	 */
	public BooleanProperty recordingStartedProperty() {
		return recordingStarted;
	}

	/**
	 * Sets whether the outline should be displayed.
	 *
	 * @param show True if the outline should be displayed, false otherwise.
	 */
	public void setShowOutline(boolean show) {
		showOutline.set(show);
	}

	/**
	 * Gets the property for tracking outline visibility.
	 *
	 * @return The show outline property.
	 */
	public BooleanProperty showOutlineProperty() {
		return showOutline;
	}

	/**
	 * Checks if users should be notified about recording functionality.
	 *
	 * @return True if recording notifications are enabled, false otherwise.
	 */
	public boolean getNotifyToRecord() {
		return notifyToRecord.get();
	}

	/**
	 * Sets whether to notify users about recording functionality.
	 *
	 * @param notify True if recording notifications should be enabled, false otherwise.
	 */
	public void setNotifyToRecord(boolean notify) {
		notifyToRecord.set(notify);
	}

	/**
	 * Sets whether screen sharing is active.
	 *
	 * @param started True if screen sharing is active, false otherwise.
	 */
	public void setScreenSharingStarted(boolean started) {
		screenSharingStarted.set(started);
	}

	/**
	 * Checks if screen sharing is active.
	 *
	 * @return True if screen sharing is active, false otherwise.
	 */
	public boolean getScreenSharingStarted() {
		return screenSharingStarted.get();
	}

	/**
	 * Gets the property for tracking screen sharing status.
	 *
	 * @return The screen sharing started property.
	 */
	public BooleanProperty screenSharingStartedProperty() {
		return screenSharingStarted;
	}

	/**
	 * Gets the directory path for storing recordings.
	 *
	 * @return The recording directory path.
	 */
	public String getRecordingDirectory() {
		return recordingDir;
	}

	/**
	 * Gets the stopwatch for tracking presentation time.
	 *
	 * @return The presentation stopwatch.
	 */
	public Stopwatch getStopwatch() {
		return stopwatch;
	}

	/**
	 * Gets the observer for tracking manual states and changes in the presentation.
	 *
	 * @return The manual state observer instance.
	 */
	public ManualStateObserver getManualStateObserver() {
		return manualStateObserver;
	}
}
