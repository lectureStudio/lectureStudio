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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.core.view.Action;
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

	private final JToggleButton muteAudioButton;

	private final JToggleButton muteVideoButton;

	private final JButton stopConnectionButton;

	private Image image;

	private String peerName;


	/**
	 * Create a new PeerView.
	 */
	public PeerView() {
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new BorderLayout());

		muteAudioButton = new JToggleButton();
		muteAudioButton.setIcon(AwtResourceLoader.getIcon("microphone-off.svg", 20));
		muteAudioButton.setSelectedIcon(AwtResourceLoader.getIcon("microphone.svg", 20));
		muteAudioButton.setSelected(true);

		muteVideoButton = new JToggleButton();
		muteVideoButton.setIcon(AwtResourceLoader.getIcon("camera-off.svg", 20));
		muteVideoButton.setSelectedIcon(AwtResourceLoader.getIcon("camera.svg", 20));
		muteVideoButton.setSelected(true);

		stopConnectionButton = new JButton("Stop");

		Box buttonsBox = Box.createHorizontalBox();
		buttonsBox.setBorder(new EmptyBorder(0, 5, 5, 5));
		buttonsBox.add(Box.createHorizontalGlue());
		buttonsBox.add(muteAudioButton);
		buttonsBox.add(stopConnectionButton);
		buttonsBox.add(muteVideoButton);
		buttonsBox.add(Box.createHorizontalGlue());

		add(buttonsBox, BorderLayout.SOUTH);
	}

	public void setOnMuteAudio(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(muteAudioButton, action);
	}

	public void setOnMuteVideo(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(muteVideoButton, action);
	}

	public void setOnStopPeerConnection(Action action) {
		SwingUtils.bindAction(stopConnectionButton, action);
	}

	/**
	 * Display the provided image.
	 *
	 * @param image The image to display.
	 */
	public void showImage(Image image) {
		if (isNull(image)) {
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
	}

	/**
	 * Set the peer's name.
	 *
	 * @param name The name of the remote peer.
	 */
	public void setPeerName(String name) {
		this.peerName = name;
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();

		if (nonNull(image)) {
			paintWithImage(g2);
		}
		else {
			paintNoImage(g2);
		}

		g2.dispose();
	}

	private void paintWithImage(Graphics2D g2) {
		g2.drawImage(image, (getWidth() - image.getWidth(null)) / 2, 0, null);

		if (isNull(peerName)) {
			return;
		}

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(getFont().deriveFont(16.f));

		FontMetrics metrics = g2.getFontMetrics(g2.getFont());
		int w = metrics.stringWidth(peerName);
		int h = metrics.getHeight();

		g2.setColor(Color.WHITE);
		g2.drawString(peerName, (getWidth() - w) / 2, getHeight() - h - 20);
	}

	private void paintNoImage(Graphics2D g2) {
		g2.setPaint(Color.LIGHT_GRAY);
		g2.fillRect(0, 0, getWidth(), getHeight());

		if (isNull(peerName)) {
			return;
		}

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(getFont().deriveFont(20.f));

		FontMetrics metrics = g2.getFontMetrics(g2.getFont());
		int w = metrics.stringWidth(peerName);
		int h = metrics.getHeight();

		g2.setColor(Color.DARK_GRAY);
		g2.drawString(peerName, (getWidth() - w) / 2, (getHeight() - h) / 2);
	}

}
