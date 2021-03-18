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

import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.presenter.MediaTracksPresenter;
import org.lecturestudio.editor.api.view.MediaTracksView;
import org.lecturestudio.javafx.beans.LectDoubleProperty;
import org.lecturestudio.javafx.control.MediaTracks;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.media.track.MediaTrack;

@FxmlView(name = "media-tracks", presenter = MediaTracksPresenter.class)
public class FxMediaTracksView extends MediaTracks implements MediaTracksView {

	public FxMediaTracksView() {
		super();
	}

	@Override
	public void bindPrimarySelection(DoubleProperty property) {
		primarySelectionProperty().bindBidirectional(new LectDoubleProperty(property));
	}

	@Override
	public void bindLeftSelection(DoubleProperty property) {
		leftSelectionProperty().bindBidirectional(new LectDoubleProperty(property));
	}

	@Override
	public void bindRightSelection(DoubleProperty property) {
		rightSelectionProperty().bindBidirectional(new LectDoubleProperty(property));
	}

	@Override
	public void bindZoomLevel(DoubleProperty property) {
		getTransform().mxxProperty().bindBidirectional(new LectDoubleProperty(property));
	}

	@Override
	public void stickSliders() {
		FxUtils.invoke(this::stickSlidersTogether);
	}

	@Override
	public void setMediaTracks(MediaTrack<?>... tracks) {
		FxUtils.invoke(() -> getTracks().setAll(tracks));
	}

	@Override
	public void setOnSeekPressed(Action action) {
		setOnSeekPressed(event -> action.execute());
	}
}
