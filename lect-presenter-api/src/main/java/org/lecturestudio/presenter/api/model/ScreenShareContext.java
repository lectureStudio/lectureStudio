/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.model;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.presenter.api.net.ScreenShareProfile;
import org.lecturestudio.web.api.model.ScreenSource;

public class ScreenShareContext {

	private final ObjectProperty<ScreenShareProfile> profile = new ObjectProperty<>();

	private final ObjectProperty<ScreenSource> source = new ObjectProperty<>();


	public ObjectProperty<ScreenSource> sourceProperty() {
		return source;
	}

	public ObjectProperty<ScreenShareProfile> profileProperty() {
		return profile;
	}

	public ScreenSource getSource() {
		return source.get();
	}

	public ScreenShareProfile getProfile() {
		return profile.get();
	}
}
