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

import java.io.File;
import java.util.List;

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
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.presenter.api.config.PresenterConfigService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.model.Stopwatch;
import org.lecturestudio.presenter.api.service.UserPrivilegeService;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;
import org.lecturestudio.web.api.stream.model.Course;

public class PresenterContext extends ApplicationContext {

	public static final String SLIDES_CONTEXT = "Slides";
	public static final String SLIDES_TO_PDF_CONTEXT = "SlidesToPDF";
	public static final String SLIDES_EXTENSION = "pdf";

	public static final String RECORDING_CONTEXT = "Recording";
	public static final String RECORDING_EXTENSION = "presenter";

	private final ObjectProperty<Course> course = new ObjectProperty<>();

	private final ObservableList<MessengerMessage> messengerMessages = new ObservableArrayList<>();

	private final IntegerProperty messageCount = new IntegerProperty();

	private final ObservableList<SpeechRequestMessage> speechRequests = new ObservableArrayList<>();

	private final IntegerProperty speechRequestCount = new IntegerProperty();

	private final IntegerProperty attendeesCount = new IntegerProperty();

	private final BooleanProperty messengerStarted = new BooleanProperty();

	private final BooleanProperty streamStarted = new BooleanProperty();

	private final BooleanProperty recordingStarted = new BooleanProperty();

	private final BooleanProperty hasRecordedChanges = new BooleanProperty();

	private final BooleanProperty screenSharingStarted = new BooleanProperty();

	private final BooleanProperty showOutline = new BooleanProperty();

	private final UserPrivilegeService userPrivilegeService;

	private final File configFile;

	private final String recordingDir;

	private final Stopwatch stopwatch = new Stopwatch();

	public PresenterContext(AppDataLocator dataLocator, File configFile,
			Configuration config, Dictionary dict, EventBus eventBus,
			EventBus audioBus, UserPrivilegeService userPrivilegeService) {
		super(dataLocator, config, dict, eventBus, audioBus);

		this.configFile = configFile;
		this.userPrivilegeService = userPrivilegeService;
		this.recordingDir = getDataLocator().toAppDataPath("recording");

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
	}

	public PresenterConfiguration getConfiguration() {
		return (PresenterConfiguration) super.getConfiguration();
	}

	@Override
	public void saveConfiguration() throws Exception {
		var configService = new PresenterConfigService();
		configService.save(configFile, getConfiguration());
	}

	public UserPrivilegeService getUserPrivilegeService() {
		return userPrivilegeService;
	}

	public Course getCourse() {
		return course.get();
	}

	public void setCourse(Course course) {
		this.course.set(course);
	}

	public ObjectProperty<Course> courseProperty() {
		return course;
	}

	public List<MessengerMessage> getMessengerMessages() {
		return messengerMessages;
	}

	public List<SpeechRequestMessage> getSpeechRequests() {
		return speechRequests;
	}

	public IntegerProperty messageCountProperty() {
		return messageCount;
	}

	public IntegerProperty speechRequestCountProperty() {
		return speechRequestCount;
	}

	public IntegerProperty attendeesCountProperty() {
		return attendeesCount;
	}

	public int getAttendeesCount() {
		return attendeesCount.get();
	}

	public void setAttendeesCount(int count) {
		attendeesCount.set(count);
	}

	public void setHasRecordedChanges(boolean changes) {
		hasRecordedChanges.set(changes);
	}

	public boolean hasRecordedChanges() {
		return hasRecordedChanges.get();
	}

	public BooleanProperty hasRecordedChangesProperty() {
		return hasRecordedChanges;
	}

	public void setStreamStarted(boolean started) {
		streamStarted.set(started);
	}

	public boolean getStreamStarted() {
		return streamStarted.get();
	}

	public BooleanProperty streamStartedProperty() {
		return streamStarted;
	}

	public void setMessengerStarted(boolean started) {
		messengerStarted.set(started);
	}

	public boolean getMessengerStarted() {
		return messengerStarted.get();
	}

	public BooleanProperty messengerStartedProperty() {
		return messengerStarted;
	}

	public void setRecordingStarted(boolean started) {
		recordingStarted.set(started);
	}

	public boolean getRecordingStarted() {
		return recordingStarted.get();
	}

	public BooleanProperty recordingStartedProperty() {
		return recordingStarted;
	}

	public void setShowOutline(boolean show) {
		showOutline.set(show);
	}

	public BooleanProperty showOutlineProperty() {
		return showOutline;
	}

	public void setScreenSharingStarted(boolean started) {
		screenSharingStarted.set(started);
	}

	public boolean getScreenSharingStarted() {
		return screenSharingStarted.get();
	}

	public BooleanProperty screenSharingStartedProperty() {
		return screenSharingStarted;
	}

	public String getRecordingDirectory() {
		return recordingDir;
	}

	public Stopwatch getStopwatch(){
		return stopwatch;
	}
}
