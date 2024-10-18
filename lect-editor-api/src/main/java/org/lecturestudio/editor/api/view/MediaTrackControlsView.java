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

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;
import org.lecturestudio.editor.api.model.ZoomConstraints;
import org.lecturestudio.media.search.SearchState;

public interface MediaTrackControlsView extends View {

	void bindCanCut(BooleanProperty property);

	void bindCanDeletePage(BooleanProperty property);

	void bindCanUndo(BooleanProperty property);

	void bindCanRedo(BooleanProperty property);

	void bindZoomLevel(ZoomConstraints constraints, DoubleProperty property);

	void bindCanSplitAndSaveRecording(BooleanProperty property);

	void setOnCollapseSelection(Action action);

	void setOnUndo(Action action);

	void setOnRedo(Action action);

	void setOnCut(Action action);

	void setOnAdjustVolume(Action action);

	void setOnInsertPage(Action action);

	void setOnDeletePage(Action action);

	void setOnReplacePage(Action action);

	void setOnImportRecording(Action action);

	void setOnZoomIn(Action action);

	void setOnZoomOut(Action action);

	void setOnSearch(ConsumerAction<String> action);

	void setOnPreviousFoundPage(Action action);

	void setOnNextFoundPage(Action action);

	void setOnSplitAndSaveRecording(Action action);

	void setSearchState(SearchState searchState);

}
