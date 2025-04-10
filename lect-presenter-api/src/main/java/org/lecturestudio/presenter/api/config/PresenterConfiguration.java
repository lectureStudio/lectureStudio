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

/**
 * Configuration class for the Presenter application.
 * <p>
 * This class provides configuration properties for various presenter features including:
 * - Recording settings (notification, autostart, confirmation)
 * - Page/document management settings
 * - Stream configuration
 * - Template configuration
 * - External window configurations for different UI components
 * - Slide view settings
 * <p>
 * Extends the base Configuration class to provide presenter-specific settings.
 *
 * @author Alex Andres
 */
public class PresenterConfiguration extends Configuration {

	/** Show a reminder notification, if desired, to start lecture recording. */
	private final BooleanProperty notifyToRecord = new BooleanProperty(null);

	/** Whether to automatically start recording when a lecture begins. */
	private final BooleanProperty autostartRecording = new BooleanProperty(null);

	/** Indicates whether to show a confirmation prompt before stopping a recording. */
	private final BooleanProperty confirmStopRecording = new BooleanProperty();

	/** The timeout duration in milliseconds between page recordings. */
	private final IntegerProperty pageRecordingTimeout = new IntegerProperty();

	/** Determines if documents should be saved automatically when closing. */
	private final BooleanProperty saveDocOnClose = new BooleanProperty();

	/** The delay in milliseconds before selecting a new page is processed. */
	private final IntegerProperty pageSelectionDelay = new IntegerProperty();

	/** Configuration settings for streaming functionality. */
	private final StreamConfiguration streamConfig = new StreamConfiguration();

	/** Configuration settings for document templates. */
	private final TemplateConfiguration templateConfig = new TemplateConfiguration();

	/** Configuration for the external messages window position and size. */
	private final ExternalWindowConfiguration externalMessagesConfig = new ExternalWindowConfiguration();

	/** Configuration for the external participants window position and size. */
	private final ExternalWindowConfiguration externalParticipantsConfig = new ExternalWindowConfiguration();

	/** Configuration for the external participant video window position and size. */
	private final ExternalWindowConfiguration externalParticipantVideoConfig = new ExternalWindowConfiguration();

	/** Configuration for the external slide preview window position and size. */
	private final ExternalWindowConfiguration externalSlidePreviewConfig = new ExternalWindowConfiguration();

	/** Configuration for the external slide notes window position and size. */
	private final ExternalWindowConfiguration externalSlideNotesConfig = new ExternalWindowConfiguration();

	/** Configuration for the external notes window position and size. */
	private final ExternalWindowConfiguration externalNotesConfig = new ExternalWindowConfiguration();

	/** Configuration settings for the slide view component. */
	private final SlideViewConfiguration slideViewConfiguration = new SlideViewConfiguration();


	/**
	 * Gets whether to show a reminder notification to start lecture recording.
	 *
	 * @return true if notification should be shown, false otherwise.
	 */
	public Boolean getNotifyToRecord() {
		return notifyToRecord.get();
	}

	/**
	 * Sets whether to show a reminder notification to start lecture recording.
	 *
	 * @param notify true to show notification, false to hide.
	 */
	public void setNotifyToRecord(Boolean notify) {
		this.notifyToRecord.set(notify);
	}

	/**
	 * Returns the notify to record property object.
	 *
	 * @return the BooleanProperty for notification setting
	 */
	public BooleanProperty notifyToRecordProperty() {
		return notifyToRecord;
	}

	/**
	 * Gets whether to automatically start recording when a lecture begins.
	 *
	 * @return true if recording should start automatically, false otherwise.
	 */
	public Boolean getAutostartRecording() {
		return autostartRecording.get();
	}

	/**
	 * Sets whether to automatically start recording when a lecture begins.
	 *
	 * @param record true to enable automatic recording start, false to disable.
	 */
	public void setAutostartRecording(Boolean record) {
		this.autostartRecording.set(record);
	}

	/**
	 * Returns the autostart recording property object.
	 *
	 * @return the BooleanProperty for autostart recording setting.
	 */
	public BooleanProperty autostartRecordingProperty() {
		return autostartRecording;
	}

	/**
	 * Gets whether to confirm before stopping the lecture recording.
	 *
	 * @return true if confirmation is required, false otherwise.
	 */
	public Boolean getConfirmStopRecording() {
		return confirmStopRecording.get();
	}

	/**
	 * Sets whether to confirm before stopping the lecture recording.
	 *
	 * @param confirm true to require confirmation, false to stop without confirmation.
	 */
	public void setConfirmStopRecording(Boolean confirm) {
		this.confirmStopRecording.set(confirm);
	}

	/**
	 * Returns the confirm stop recording property object.
	 *
	 * @return the BooleanProperty for confirmation setting.
	 */
	public BooleanProperty confirmStopRecordingProperty() {
		return confirmStopRecording;
	}

	/**
	 * Gets the timeout duration for page recording in milliseconds.
	 *
	 * @return the page recording timeout value
	 */
	public Integer getPageRecordingTimeout() {
		return pageRecordingTimeout.get();
	}

	/**
	 * Sets the timeout duration for page recording in milliseconds.
	 *
	 * @param timeout the timeout value to set.
	 */
	public void setPageRecordingTimeout(Integer timeout) {
		this.pageRecordingTimeout.set(timeout);
	}

	/**
	 * Returns the page recording timeout property object.
	 *
	 * @return the IntegerProperty for page recording timeout setting.
	 */
	public IntegerProperty pageRecordingTimeoutProperty() {
		return pageRecordingTimeout;
	}

	/**
	 * Gets whether to save the document automatically when closing.
	 *
	 * @return true if document should be saved on close, false otherwise.
	 */
	public Boolean getSaveDocumentOnClose() {
		return saveDocOnClose.get();
	}

	/**
	 * Sets whether to save the document automatically when closing.
	 *
	 * @param saveDocOnClose true to save document on close, false otherwise.
	 */
	public void setSaveDocumentOnClose(Boolean saveDocOnClose) {
		this.saveDocOnClose.set(saveDocOnClose);
	}

	/**
	 * Returns the save document on close property object.
	 *
	 * @return the BooleanProperty for document auto-save setting.
	 */
	public BooleanProperty saveDocOnCloseProperty() {
		return saveDocOnClose;
	}

	/**
	 * Gets the delay duration for page selection in milliseconds.
	 *
	 * @return the page selection delay value.
	 */
	public Integer getPageSelectionDelay() {
		return pageSelectionDelay.get();
	}

	/**
	 * Sets the delay duration for page selection in milliseconds.
	 *
	 * @param delay the delay value to set.
	 */
	public void setPageSelectionDelay(Integer delay) {
		this.pageSelectionDelay.set(delay);
	}

	/**
	 * Returns the page selection delay property object.
	 *
	 * @return the IntegerProperty for page selection delay setting.
	 */
	public IntegerProperty pageSelectionDelayProperty() {
		return pageSelectionDelay;
	}

	/**
	 * Gets the stream configuration settings.
	 *
	 * @return the StreamConfiguration object.
	 */
	public StreamConfiguration getStreamConfig() {
		return streamConfig;
	}

	/**
	 * Gets the template configuration settings.
	 *
	 * @return the TemplateConfiguration object.
	 */
	public TemplateConfiguration getTemplateConfig() {
		return templateConfig;
	}

	/**
	 * Gets the configuration for the external messages window.
	 *
	 * @return the ExternalWindowConfiguration object for the message window.
	 */
	public ExternalWindowConfiguration getExternalMessagesConfig() {
		return externalMessagesConfig;
	}

	/**
	 * Gets the configuration for the external participants window.
	 *
	 * @return the ExternalWindowConfiguration object for participants window.
	 */
	public ExternalWindowConfiguration getExternalParticipantsConfig() {
		return externalParticipantsConfig;
	}

	/**
	 * Gets the configuration for the external participant video window.
	 *
	 * @return the ExternalWindowConfiguration object for the participant video window.
	 */
	public ExternalWindowConfiguration getExternalParticipantVideoConfig() {
		return externalParticipantVideoConfig;
	}

	/**
	 * Gets the configuration for the external slide preview window.
	 *
	 * @return the ExternalWindowConfiguration object for the slide preview window.
	 */
	public ExternalWindowConfiguration getExternalSlidePreviewConfig() {
		return externalSlidePreviewConfig;
	}

	/**
	 * Gets the configuration for the external notes window.
	 *
	 * @return the ExternalWindowConfiguration object for the notes window.
	 */
	public ExternalWindowConfiguration getExternalNotesConfig() {
		return externalNotesConfig;
	}

	/**
	 * Gets the slide view configuration settings.
	 *
	 * @return the SlideViewConfiguration object
	 */
	public SlideViewConfiguration getSlideViewConfiguration() {
		return slideViewConfiguration;
	}

	/**
	 * Gets the configuration for the external slide notes window.
	 *
	 * @return the ExternalWindowConfiguration object for the slide notes window.
	 */
	public ExternalWindowConfiguration getExternalSlideNotesConfig() {
		return externalSlideNotesConfig;
	}
}
