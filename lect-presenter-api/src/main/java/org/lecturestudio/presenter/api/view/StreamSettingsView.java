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

package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.model.ParticipantVideoLayout;
import org.lecturestudio.presenter.api.net.ScreenShareProfile;

/**
 * Interface defining the view for stream settings configuration.
 * Provides methods to configure streaming parameters, credentials,
 * and layout options for presentation streaming functionality.
 *
 * @author Alex Andres
 */
public interface StreamSettingsView extends SettingsBaseView {

	/**
	 * Sets whether the stream should be recorded.
	 *
	 * @param record The property controlling stream recording state.
	 */
	void setRecordStream(BooleanProperty record);

	/**
	 * Sets the access token for stream authentication.
	 *
	 * @param accessToken The property containing the authentication token.
	 */
	void setAccessToken(StringProperty accessToken);

	/**
	 * Sets the validation state of the current access token.
	 *
	 * @param valid True if the access token is valid, false otherwise.
	 */
	void setAccessTokenValid(boolean valid);

	/**
	 * Sets an error message for access token validation issues.
	 *
	 * @param error The error message to display.
	 */
	void setAccessTokenError(String error);

	/**
	 * Sets the stream server name.
	 *
	 * @param serverName The property containing the server name.
	 */
	void setServerName(StringProperty serverName);

	/**
	 * Sets the action to execute when checking access token validity.
	 *
	 * @param action The action to perform for token validation.
	 */
	void setOnCheckAccessToken(Action action);

	/**
	 * Sets the screen sharing profile.
	 *
	 * @param profile The property containing the screen sharing profile.
	 */
	void setScreenShareProfile(ObjectProperty<ScreenShareProfile> profile);

	/**
	 * Sets the available screen sharing profiles.
	 *
	 * @param profiles Array of available screen sharing profiles.
	 */
	void setScreenShareProfiles(ScreenShareProfile[] profiles);

	/**
	 * Sets the layout for participant video display.
	 *
	 * @param layoutProperty The property containing the participant video layout.
	 */
	void setParticipantVideoLayout(ObjectProperty<ParticipantVideoLayout> layoutProperty);

}
