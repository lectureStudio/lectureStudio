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

package org.lecturestudio.presenter.swing.component;

import dev.onvoid.webrtc.media.video.VideoFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.lecturestudio.presenter.api.model.SharedScreenSource;
import org.lecturestudio.swing.components.VideoFrameView;

public class ScreenSourceView extends JPanel {

	private static final Border BORDER = new EmptyBorder(2, 2, 2, 2);

	private static final Border BORDER_HOVER = new LineBorder(Color.GRAY, 2);

	private static final Border BORDER_SELECTED = new LineBorder(Color.BLUE, 2);

	private final List<Consumer<ScreenSourceView>> actionConsumers = new ArrayList<>();

	private final VideoFrameView imageView;

	private final JLabel nameLabel;

	private SharedScreenSource screenSource;

	private boolean selected;


	public ScreenSourceView() {
		setBorder(BORDER);
		setLayout(new BorderLayout(2, 2));

		imageView = new VideoFrameView();
		imageView.setBorder(new EmptyBorder(0, 0, 0, 0));

		nameLabel = new JLabel();
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

		add(imageView, BorderLayout.CENTER);
		add(nameLabel, BorderLayout.SOUTH);

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				updateBorder(BORDER_HOVER);
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				updateBorder(BORDER);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				notifyAction();
			}
		});
	}

	public void addSelectionConsumer(Consumer<ScreenSourceView> consumer) {
		actionConsumers.add(consumer);
	}

	public void removeSelectionConsumer(Consumer<ScreenSourceView> consumer) {
		actionConsumers.remove(consumer);
	}

	public void setSelected(boolean selected) {
		this.selected = selected;

		updateBorder(BORDER);
	}

	public SharedScreenSource getScreenSource() {
		return screenSource;
	}

	public void setScreenSource(SharedScreenSource screenSource) {
		this.screenSource = screenSource;
		this.screenSource.setVideoFrameConsumer(this::onVideoFrame);

		nameLabel.setText(screenSource.getTitle());
		imageView.repaint();
	}

	private void onVideoFrame(VideoFrame videoFrame) {
		try {
			imageView.paintVideoFrame(videoFrame);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void updateBorder(Border border) {
		setBorder(selected ? BORDER_SELECTED : border);
	}

	private void notifyAction() {
		for (var consumer : actionConsumers) {
			consumer.accept(this);
		}
	}
}