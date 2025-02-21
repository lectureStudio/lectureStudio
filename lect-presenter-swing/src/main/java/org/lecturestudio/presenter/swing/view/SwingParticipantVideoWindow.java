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

package org.lecturestudio.presenter.swing.view;

import dev.onvoid.webrtc.media.video.VideoFrame;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.service.UserPrivilegeService;
import org.lecturestudio.presenter.api.view.ParticipantVideoWindow;
import org.lecturestudio.swing.components.VideoFrameView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "participant-video-window")
public class SwingParticipantVideoWindow extends JFrame implements ParticipantVideoWindow {

	private final Dictionary dict;

	private final UserPrivilegeService userPrivilegeService;

	private VideoFrameView frameView;


	@Inject
	SwingParticipantVideoWindow(Dictionary dictionary,
								UserPrivilegeService userPrivilegeService) {
		super();

		this.dict = dictionary;
		this.userPrivilegeService = userPrivilegeService;
	}

	@Override
	public void close() {
		SwingUtils.invoke(() -> {
			setVisible(false);
			dispose();
		});
	}

	@Override
	public void open() {
		SwingUtils.invoke(() -> setVisible(true));
	}

	@Override
	public void setOnClose(Action action) {
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				executeAction(action);
			}
		});
	}

	@Override
	public void setTextSize(double size) {

	}

	@Override
	public void setVideoFrame(VideoFrame frame) {
		SwingUtils.invoke(() -> {
			try {
				frameView.paintVideoFrame(frame);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@ViewPostConstruct
	private void initialize() {
		frameView = new VideoFrameView();
		frameView.setBorder(new EmptyBorder(0, 0, 0, 0));

		add(frameView, BorderLayout.CENTER);
	}
}
