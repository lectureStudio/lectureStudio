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

import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;
import org.lecturestudio.media.track.MediaTrack;

public interface MediaTracksView extends View {

	void bindPrimarySelection(DoubleProperty property);

	void bindLeftSelection(DoubleProperty property);

	void bindRightSelection(DoubleProperty property);

	void bindZoomLevel(DoubleProperty property);

	void stickSliders();

	void setDuration(Time duration);

	void setMediaTracks(MediaTrack<?>... tracks);

	void setOnSeekPressed(Action action);

	void setOnMovePage(ConsumerAction<RecordedPage> action);

	void setOnHidePage(ConsumerAction<RecordedPage> action);

	void setOnHideAndMoveNextPage(ConsumerAction<RecordedPage> action);
}
