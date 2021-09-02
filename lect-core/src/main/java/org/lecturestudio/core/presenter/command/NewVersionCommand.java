/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.core.presenter.command;

import org.lecturestudio.core.model.VersionInfo;
import org.lecturestudio.core.presenter.NewVersionPresenter;

public class NewVersionCommand extends ShowPresenterCommand<NewVersionPresenter> {

	/** The new version. */
	private final VersionInfo version;


	/**
	 * Create a new {@link NewVersionCommand} with the specified parameters.
	 *
	 * @param cls The presenter class.
	 * @param version The new version.
	 */
	public NewVersionCommand(Class<NewVersionPresenter> cls, VersionInfo version) {
		super(cls);

		this.version = version;
	}

	@Override
	public void execute(NewVersionPresenter presenter) {
		presenter.setVersion(version);
	}
}
