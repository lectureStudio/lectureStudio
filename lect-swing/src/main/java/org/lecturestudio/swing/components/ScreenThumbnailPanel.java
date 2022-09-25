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

package org.lecturestudio.swing.components;

import javax.swing.JButton;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.util.SwingUtils;

public class ScreenThumbnailPanel extends ThumbnailPanel {

	private final Dictionary dict;

	private final RecordButton startScreenShareButton;

	private final JButton stopScreenShareButton;


	public ScreenThumbnailPanel(Dictionary dict) {
		super();

		this.dict = dict;

		startScreenShareButton = new RecordButton();
		startScreenShareButton.setIcon(AwtResourceLoader.getIcon("record-tool.svg", 20));
		startScreenShareButton.setPauseIcon(AwtResourceLoader.getIcon("record-pause-tool.svg", 20));
		startScreenShareButton.setPausedIcon(AwtResourceLoader.getIcon("record-resume-tool.svg", 20));
		startScreenShareButton.setToolTipText(dict.get("screen.share.start"));

		stopScreenShareButton = new JButton();
		stopScreenShareButton.setIcon(AwtResourceLoader.getIcon("record-stop-tool.svg", 20));
		stopScreenShareButton.setToolTipText(dict.get("screen.share.stop"));

		addButton(startScreenShareButton);
		addButton(stopScreenShareButton);
	}

	public void setOnToggleScreenShare(BooleanProperty property) {
		startScreenShareButton.addActionListener(e -> {
			property.set(!property.get());
		});
	}

	public void setOnStopScreenShare(Action action) {
		SwingUtils.bindAction(stopScreenShareButton, action);
	}

	public void setScreenShareState(ExecutableState state) {
		startScreenShareButton.setState(state);

		switch (state) {
			case Started -> startScreenShareButton.setToolTipText(dict.get("screen.share.suspend"));
			case Stopped -> startScreenShareButton.setToolTipText(dict.get("screen.share.start"));
		}
	}
}
