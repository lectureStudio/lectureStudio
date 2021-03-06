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

package org.lecturestudio.javafx.control;

import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.transform.Affine;

import org.lecturestudio.media.track.control.MediaTrackControl;

public abstract class MediaTrackControlSkinBase extends SkinBase<MediaTrackControlBase<?>> {

	private final MediaTrackControlBase<?> mediaTrackControl;

	protected final Map<MediaTrackControl, Node> controlNodeMap;


	protected MediaTrackControlSkinBase(MediaTrackControlBase control) {
		super(control);

		mediaTrackControl = control;
		controlNodeMap = new HashMap<>();

		initLayout(control);
	}

	abstract protected void updateControl();

	@Override
	public void dispose() {
		Affine transform = mediaTrackControl.getTransform();

		if (nonNull(transform)) {
			unregisterChangeListeners(transform.mxxProperty());
			unregisterChangeListeners(transform.txProperty());
		}
	}

	private void initLayout(MediaTrackControlBase<?> control) {
		Affine transform = control.getTransform();

		if (nonNull(transform)) {
			registerChangeListener(transform.mxxProperty(), value -> updateControl());
			registerChangeListener(transform.txProperty(), value -> updateControl());
		}

		control.transformProperty().addListener((observable, oldValue, newValue) -> {
			if (nonNull(oldValue)) {
				unregisterChangeListeners(oldValue.mxxProperty());
				unregisterChangeListeners(oldValue.txProperty());
			}
			if (nonNull(newValue)) {
				registerChangeListener(newValue.mxxProperty(), value -> updateControl());
				registerChangeListener(newValue.txProperty(), value -> updateControl());
			}

			updateControl();
		});
	}
}
