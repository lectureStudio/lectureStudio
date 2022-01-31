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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.util.SwingUtils;

/**
 * The PeerView displays the remote peers video, if activated, and buttons to
 * control the peer's media.
 *
 * @author Alex Andres
 */
public class PeerView extends JComponent {

	private final Dictionary dict;

	private final JLabel nameLabel;

	private final JLabel stateLabel;

	private final JToggleButton muteAudioButton;

	private final JToggleButton muteVideoButton;

	private final JButton stopConnectionButton;

	private final Box buttonsBox;

	private ExecutableState peerState;

	private Image image;

	private Long requestId;


	/**
	 * Creates a new PeerView.
	 */
	public PeerView(Dictionary dict) {
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

	public void setOnMuteAudio(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(muteAudioButton, action);
	}

	public void setOnMuteVideo(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(muteVideoButton, action);
	}

	public void setOnStopPeerConnection(ConsumerAction<Long> action) {
		stopConnectionButton.addActionListener(e -> {
			action.execute(requestId);
		});
	}

	/**
	 * Display the provided image.
	 *
	 * @param image The image to display.
	 */
	public void showImage(Image image) {
		if (isNull(image) || !muteVideoButton.isSelected()) {
			return;
		}

		this.image = image;

		repaint();
	}

	/**
	 * Clear the image.
	 */
	public void clearImage() {
		this.image = null;

		repaint();
	}

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

	public void setHasVideo(boolean hasVideo) {
		if (peerState != ExecutableState.Started) {
			return;
		}

		muteVideoButton.setEnabled(hasVideo);

		setHasVideoIcon(hasVideo);
	}

	public void setHasVideoIcon(boolean hasVideo) {
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

	/**
	 * @return The unique request ID of the peer.
	 */
	public Long getRequestId() {
		return requestId;
	}

	/**
	 * Set the peer's unique request ID.
	 *
	 * @param id The unique request ID of the peer.
	 */
	public void setRequestId(Long id) {
		this.requestId = id;
	}

	/**
	 * Set the peer's name.
	 *
	 * @param name The name of the remote peer.
	 */
	public void setPeerName(String name) {
		nameLabel.setText(name);
	}

	@Override
	public void paintComponent(Graphics g) {
		final int bottomHeight = buttonsBox.getHeight();

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setPaint(new Color(228, 228, 231));
		g2.fillRect(0, 0, getWidth(), getHeight());

		if (nonNull(image)) {
			paintWithImage(g2);
		}

		g2.setPaint(new Color(228, 228, 231, 215));
		g2.fillRect(0, getHeight() - bottomHeight, getWidth(), bottomHeight);
		g2.dispose();
	}

	private void paintWithImage(Graphics2D g2) {
		AffineTransform transform = g2.getTransform();
		AffineTransform imageTransform = new AffineTransform();
		imageTransform.translate(transform.getTranslateX(), transform.getTranslateY());

		g2.setTransform(imageTransform);
		g2.drawImage(image, (int) ((getWidth() * transform.getScaleX() - image.getWidth(null)) / 2), 0, null);
		g2.setTransform(transform);
	}
}
