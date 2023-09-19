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

package org.lecturestudio.editor.api.view;

import java.io.File;
import java.util.List;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;

public interface MenuView extends View {

	void setDocument(Document doc);

	/**
	 * File Menu
	 */

	void setRecentDocuments(List<RecentDocument> recentDocs);

	void setOnOpenRecording(Action action);

	void setOnOpenRecording(ConsumerAction<File> action);

	void setOnCloseRecording(Action action);

	void setOnSaveDocument(Action action);

	void setOnSaveCurrentPage(Action action);

	void setOnSaveRecordingAs(Action action);

	void setOnExportAudio(Action action);

	void setOnImportAudio(Action action);

	void setOnExit(Action action);

	/**
	 * Edit Menu
	 */

	void bindCanCut(BooleanProperty property);

	void bindCanUndo(BooleanProperty property);

	void bindCanRedo(BooleanProperty property);

	void setOnUndo(Action action);

	void setOnRedo(Action action);

	void setOnCut(Action action);

	void setOnDeletePage(Action action);

	void setOnSettings(Action action);

	/**
	 * View Menu
	 */

	void bindFullscreen(BooleanProperty fullscreen);

	/**
	 * Info Menu
	 */

	void setOnOpenLog(Action action);

	void setOnOpenAbout(Action action);

}
