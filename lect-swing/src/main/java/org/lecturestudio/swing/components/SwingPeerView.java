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

package org.lecturestudio.swing.components;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import dev.onvoid.webrtc.media.video.VideoFrame;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.util.VideoFrameConverter;
import org.lecturestudio.swing.view.PeerView;

/**
 * The PeerView displays the remote peers video, if activated, and buttons to
 * control the peer's media.
 *
 * @author Alex Andres
 */
public class SwingPeerView extends JComponent implements PeerView {

	private final Dictionary dict;

	private final JLabel nameLabel;

	private final JLabel stateLabel;

	private final JToggleButton muteAudioButton;

	private final JToggleButton muteVideoButton;

	private final JButton stopConnectionButton;

	private final Box buttonsBox;

	private ExecutableState peerState;

	private BufferedImage image;


	/**
	 * Creates a new PeerView.
	 */
	@Inject
	public SwingPeerView(Dictionary dict) {
		this.dict = dict;

		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new BorderLayout());

		nameLabel = new JLabel();
		nameLabel.setFont(nameLabel.getFont().deriveFont(14.f));

		muteAudioButton = new JToggleButton();
		muteAudioButton.setIcon(AwtResourceLoader.getIcon("microphone-off.svg", 22));
		muteAudioButton.setSelectedIcon(AwtResourceLoader.getIcon("microphone.svg", 22));
		muteAudioButton.setContentAreaFilled(false);
		muteAudioButton.setSelected(true);
		muteAudioButton.setEnabled(false);

		muteVideoButton = new JToggleButton();
		muteVideoButton.setIcon(AwtResourceLoader.getIcon("camera-off.svg", 22));
		muteVideoButton.setSelectedIcon(AwtResourceLoader.getIcon("camera.svg", 22));
		muteVideoButton.setContentAreaFilled(false);
		muteVideoButton.setSelected(true);
		muteVideoButton.setEnabled(false);
		muteVideoButton.addItemListener(e -> {
			setHasVideoIcon(e.getStateChange() == ItemEvent.SELECTED);
		});

		stopConnectionButton = new JButton(dict.get("button.end"));
		stopConnectionButton.setBackground(Color.decode("#FEE2E2"));

		buttonsBox = Box.createHorizontalBox();
		buttonsBox.setBorder(new EmptyBorder(0, 5, 0, 5));
		buttonsBox.add(nameLabel);
		buttonsBox.add(Box.createHorizontalGlue());
		buttonsBox.add(muteAudioButton);
		buttonsBox.add(muteVideoButton);
		buttonsBox.add(stopConnectionButton);

		stateLabel = new JLabel();
		stateLabel.setHorizontalAlignment(SwingConstants.CENTER);
		stateLabel.setFont(nameLabel.getFont().deriveFont(16.f));
		stateLabel.setForeground(Color.DARK_GRAY);

		GridBagConstraints c = new GridBagConstraints();

		JPanel labelBox = new JPanel(new GridBagLayout());
		labelBox.setOpaque(false);

		c.gridy = 0;
		labelBox.add(stateLabel, c);

		add(labelBox, BorderLayout.CENTER);
		add(buttonsBox, BorderLayout.SOUTH);
	}

	@Override
	public void setPeerName(String name) {
		SwingUtils.invoke(() -> nameLabel.setText(name));
	}

	@Override
	public void setVideoFrame(VideoFrame frame) {
		SwingUtils.invoke(() -> {
			if (!muteVideoButton.isSelected()) {
				return;
			}

			try {
				image = VideoFrameConverter.convertVideoFrameToComponentSize(frame, image, this);
			}
			catch (Exception e) {
				return;
			}

			repaint();
		});
	}

	@Override
	public void clearImage() {
		image = null;

		repaint();
	}

	@Override
	public void setState(ExecutableState state) {
		this.peerState = state;

		String stateText = null;

		if (state == ExecutableState.Starting) {
			stateText = dict.get("speech.waiting");
		}
		else if (state == ExecutableState.Started) {
			muteAudioButton.setEnabled(true);

			stateText = "";
		}

		if (nonNull(stateText)) {
			stateLabel.setText(stateText);
		}
	}

	@Override
	public void setHasVideo(boolean hasVideo) {
		if (peerState != ExecutableState.Started) {
			return;
		}

		muteVideoButton.setEnabled(hasVideo);

		setHasVideoIcon(hasVideo);
	}

	@Override
	public void setMediaControlsEnabled(boolean enabled) {
		SwingUtils.invoke(() -> {
			muteAudioButton.setVisible(enabled);
			muteVideoButton.setVisible(enabled);
			stopConnectionButton.setVisible(enabled);
		});
	}

	@Override
	public void setOnMuteAudio(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(muteAudioButton, action);
	}

	@Override
	public void setOnMuteVideo(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(muteVideoButton, action);
	}

	@Override
	public void setOnKick(Action action) {
		SwingUtils.bindAction(stopConnectionButton, action);
	}

	private void setHasVideoIcon(boolean hasVideo) {
		if (peerState != ExecutableState.Started) {
			return;
		}

		if (!hasVideo) {
			stateLabel.setIcon(AwtResourceLoader.getIcon("user.svg", 50));
		}
		else {
			stateLabel.setIcon(null);
		}

		clearImage();
	}

	@Override
	public void paintComponent(Graphics g) {
		final int bottomHeight = buttonsBox.getHeight();

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setPaint(new Color(228, 228, 231));
		g2.fillRect(0, 0, getWidth(), getHeight());

		if (nonNull(image)) {
			paintPeerImage(g2);
		}

		g2.setPaint(new Color(228, 228, 231, 215));
		g2.fillRect(0, getHeight() - bottomHeight, getWidth(), bottomHeight);
		g2.dispose();
	}

	private void paintPeerImage(Graphics2D g2) {
		AffineTransform transform = g2.getTransform();
		AffineTransform imageTransform = new AffineTransform();
		imageTransform.translate(transform.getTranslateX(), transform.getTranslateY());

		int x = (int) ((getWidth() * transform.getScaleX() - image.getWidth(null)) / 2);
		int y = (int) ((getHeight() * transform.getScaleX() - image.getHeight(null)) / 2);

		g2.setTransform(imageTransform);
		g2.drawImage(image, x, y, null);
		g2.setTransform(transform);
	}
}
