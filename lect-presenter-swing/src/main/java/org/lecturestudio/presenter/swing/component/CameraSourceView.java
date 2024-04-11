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
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormats;
import org.lecturestudio.presenter.api.model.SharedScreenSource;
import org.lecturestudio.swing.components.CameraPreviewPanel;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.util.VideoFrameConverter;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

public class CameraSourceView extends JPanel {

	private static final Border BORDER = new EmptyBorder(2, 2, 2, 2);

	private static final Border BORDER_HOVER = new LineBorder(Color.GRAY, 2);

	private static final Border BORDER_SELECTED = new LineBorder(Color.BLUE, 2);

	private final List<Consumer<CameraSourceView>> actionConsumers = new ArrayList<>();


	private final ResourceBundle resources;

	private final CameraPreviewPanel cameraPreviewPanel;

	private final JLabel nameLabel;

	private Camera camera;

	private boolean selected;


	public CameraSourceView(ResourceBundle resources) {
		this.resources = resources;
		setBorder(BORDER);
		setLayout(new BorderLayout(2, 2));

		cameraPreviewPanel = new CameraPreviewPanel();
		cameraPreviewPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		cameraPreviewPanel.setMinimumSize(new Dimension(240, 1));
		cameraPreviewPanel.setMaximumSize(new Dimension(240, 1));
		cameraPreviewPanel.setPreferredSize(new Dimension(240, 1));

		nameLabel = new JLabel();
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

		add(cameraPreviewPanel, BorderLayout.CENTER);
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

	public void addSelectionConsumer(Consumer<CameraSourceView> consumer) {
		actionConsumers.add(consumer);
	}

	public void removeSelectionConsumer(Consumer<CameraSourceView> consumer) {
		actionConsumers.remove(consumer);
	}

	public void setSelected(boolean selected) {
		this.selected = selected;

		updateBorder(BORDER);
	}


	public Camera getCamera() {
		return camera;
	}


	public void setCamera(Camera camera) {
		this.camera = camera;
		cameraPreviewPanel.setCamera(camera);
		cameraPreviewPanel.setCameraFormat(CameraFormats.HD720p);


		nameLabel.setText(camera.getName());
		cameraPreviewPanel.repaint();
	}

	public void startCam() {
		CompletableFuture.runAsync(() -> {
			// sleep 5ms
			Camera cam = cameraPreviewPanel.getCamera();
			if (cam.isOpened()) {
				System.out.println("Camera already opened: " + cam.getName());
				return;
			}

			System.out.println("Starting camera preview: " + cam.getName());
			setCameraStatus(resources.getString("camera.settings.camera.starting"));
			try {
//				cameraPreviewPanel.setCameraFormat(CameraFormats.HD720p);
				cameraPreviewPanel.startCapture();
				setCameraStatus(null);
				System.out.println("Started camera preview: " + cam.getName());
			} catch (Exception e) {
				System.out.println("Failed to start camera" + cam.getName() + " preview: " + e.getMessage());
				setCameraStatus(resources.getString("camera.settings.camera.unavailable"));
			}
		});
	}

	public void stopCam() {
		cameraPreviewPanel.stopCapture();
	}

	private void setCameraStatus(String status) {
		SwingUtils.invoke(() -> {
			cameraPreviewPanel.setStatusMessage(status);
		});
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