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

package org.lecturestudio.editor.api.presenter.command;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.editor.api.presenter.InsertPagePresenter;

public class InsertPageCommand extends ShowPresenterCommand<InsertPagePresenter> {

	private final Document doc;


	public InsertPageCommand(Document doc) {
		super(InsertPagePresenter.class);

		this.doc = doc;
	}

	@Override
	public void execute(InsertPagePresenter presenter) {
		presenter.setNewDocument(doc);
	}
}
