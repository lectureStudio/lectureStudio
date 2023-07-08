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

package org.lecturestudio.presenter.api.config;

import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;

public class PresenterConfiguration extends Configuration {

	/** Show a reminder notification, if desired, to start lecture recording. */
	private final BooleanProperty notifyToRecord = new BooleanProperty();

	private final BooleanProperty confirmStopRecording = new BooleanProperty();

	private final IntegerProperty pageRecordingTimeout = new IntegerProperty();

	private final BooleanProperty saveDocOnClose = new BooleanProperty();

	private final IntegerProperty pageSelectionDelay = new IntegerProperty();

	private final StreamConfiguration streamConfig = new StreamConfiguration();

	private final TemplateConfiguration templateConfig = new TemplateConfiguration();

	private final ExternalWindowConfiguration externalMessagesConfig = new ExternalWindowConfiguration();

	private final ExternalWindowConfiguration externalParticipantsConfig = new ExternalWindowConfiguration();

	private final ExternalWindowConfiguration externalSlidePreviewConfig = new ExternalWindowConfiguration();

	private final ExternalWindowConfiguration externalSpeechConfig = new ExternalWindowConfiguration();

	private final ExternalWindowConfiguration externalNotesConfig = new ExternalWindowConfiguration();

	private final SlideViewConfiguration slideViewConfiguration = new SlideViewConfiguration();


	/**
	 * @return the notifyToRecord
	 */
	public Boolean getNotifyToRecord() {
		return notifyToRecord.get();
	}

	public void setNotifyToRecord(Boolean notify) {
		this.notifyToRecord.set(notify);
	}

	public BooleanProperty notifyToRecordProperty() {
		return notifyToRecord;
	}

	/**
	 * @return the warnOnStopRecording
	 */
	public Boolean getConfirmStopRecording() {
		return confirmStopRecording.get();
	}

	public void setConfirmStopRecording(Boolean confirm) {
		this.confirmStopRecording.set(confirm);
	}

	public BooleanProperty confirmStopRecordingProperty() {
		return confirmStopRecording;
	}

	/**
	 * @return the pageRecordingTimeout
	 */
	public Integer getPageRecordingTimeout() {
		return pageRecordingTimeout.get();
	}

	public void setPageRecordingTimeout(Integer timeout) {
		this.pageRecordingTimeout.set(timeout);
	}

	public IntegerProperty pageRecordingTimeoutProperty() {
		return pageRecordingTimeout;
	}

	/**
	 * @return the saveDocOnClose
	 */
	public Boolean getSaveDocumentOnClose() {
		return saveDocOnClose.get();
	}

	/**
	 * @param saveDocOnClose the saveDocOnClose to set
	 */
	public void setSaveDocumentOnClose(Boolean saveDocOnClose) {
		this.saveDocOnClose.set(saveDocOnClose);
	}

	public BooleanProperty saveDocOnCloseProperty() {
		return saveDocOnClose;
	}

	public Integer getPageSelectionDelay() {
		return pageSelectionDelay.get();
	}

	public void setPageSelectionDelay(Integer delay) {
		this.pageSelectionDelay.set(delay);
	}

	public IntegerProperty pageSelectionDelayProperty() {
		return pageSelectionDelay;
	}

	/**
	 * @return the streamConfig
	 */
	public StreamConfiguration getStreamConfig() {
		return streamConfig;
	}

	public TemplateConfiguration getTemplateConfig() {
		return templateConfig;
	}

	/**
	 * @return External messages configuration
	 */
	public ExternalWindowConfiguration getExternalMessagesConfig() {
		return externalMessagesConfig;
	}

	/**
	 * @return External participants configuration
	 */
	public ExternalWindowConfiguration getExternalParticipantsConfig() {
		return externalParticipantsConfig;
	}

	/**
	 * @return External slide preview configuration
	 */
	public ExternalWindowConfiguration getExternalSlidePreviewConfig() {
		return externalSlidePreviewConfig;
	}

	/**
	 * @return External speech configuration
	 */
	public ExternalWindowConfiguration getExternalSpeechConfig() {
		return externalSpeechConfig;
	}

	/**
	 * @return External notes configuration
	 */

	public ExternalWindowConfiguration getExternalNotesConfig() {
		return externalNotesConfig;
	}

	public SlideViewConfiguration getSlideViewConfiguration() {
		return slideViewConfiguration;
	}
}
