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

package org.lecturestudio.editor.javafx.view;

import javafx.util.Pair;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.presenter.MediaControlsPresenter;
import org.lecturestudio.editor.api.view.MediaControlsView;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.beans.LectDoubleProperty;
import org.lecturestudio.javafx.control.MediaControls;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "media-controls", presenter = MediaControlsPresenter.class)
public class FxMediaControlsView extends MediaControls implements MediaControlsView {

	public FxMediaControlsView() {
		super();
	}

	@Override
	public void bindPlaying(BooleanProperty property) {
		playingProperty().bindBidirectional(new LectBooleanProperty(property));
	}

	@Override
	public void bindMute(BooleanProperty property) {
		muteProperty().bindBidirectional(new LectBooleanProperty(property));
	}

	@Override
	public void bindSeek(DoubleProperty property) {
		timeProperty().bindBidirectional(new LectDoubleProperty(property));
	}

	@Override
	public void bindVolume(DoubleProperty property) {
		volumeProperty().bindBidirectional(new LectDoubleProperty(property));
	}

	@Override
	public void setCurrentPage(Integer current, Integer total) {
		FxUtils.invoke(() -> {
			setCurrentPage(new Pair<>(current, total));
		});
	}

	@Override
	public void setOnSeekPressed(Action action) {
		setOnSeekPressed(event -> action.execute());
	}

	@Override
	public void setOnPreviousPage(Action action) {
		setOnPrevAction(event -> action.execute());
	}

	@Override
	public void setOnNextPage(Action action) {
		setOnNextAction(event -> action.execute());
	}
}
