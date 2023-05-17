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

package org.lecturestudio.presenter.api.util;

import org.lecturestudio.core.util.ShutdownHandler;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.command.QuitSaveDocumentsCommand;

public class SaveDocumentsHandler extends ShutdownHandler {

	private final PresenterContext context;


	public SaveDocumentsHandler(PresenterContext context) {
		this.context = context;
	}

	@Override
	public boolean execute() throws Exception {
		PresenterConfiguration config = context.getConfiguration();

		if (Boolean.TRUE.equals(config.getSaveDocumentOnClose()) && (context.hasRecordedChanges())) {
			executeAndWait(() -> context.getEventBus()
					.post(new QuitSaveDocumentsCommand(this::resume)));

		}

		return true;
	}
}
