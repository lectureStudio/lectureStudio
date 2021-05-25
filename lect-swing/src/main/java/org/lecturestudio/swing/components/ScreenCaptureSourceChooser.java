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

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopFrame;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import org.lecturestudio.core.service.ScreenCaptureService;
import org.lecturestudio.swing.layout.WrapFlowLayout;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScreenCaptureSourceChooser extends JPanel {

    private final ScreenCaptureService service;

    private JButton confirmButton;
    private JButton cancelButton;

    private SourcePreview selectedPreview;

    @Inject
    public ScreenCaptureSourceChooser(ResourceBundle resources, ScreenCaptureService service) {
        super();
        this.service = service;

        initialize(resources);
    }

    public void setOnOk(ActionListener listener) {
        confirmButton.addActionListener(listener);
    }

    public void setOnCancel(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }

    public DesktopSource getSelectedSource() {
        return selectedPreview != null ? selectedPreview.getSource() : null;
    }

    private void initialize(ResourceBundle resources) {
        MouseListener previewClickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Object source = e.getSource();
                if (source instanceof SourcePreview) {
                    SourcePreview preview = (SourcePreview) source;

                    // Deactivate previous preview if exists
                    if (selectedPreview != null) {
                        selectedPreview.setActive(false);
                    }

                    preview.setActive(true);
                    selectedPreview = preview;

                    // Notify capture service about new capture source
                    service.setSelectedSource(preview.getSource());
                }
            }
        };

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        // Create window preview with title

        JLabel windowSelectionLabel = new JLabel();
        windowSelectionLabel.setText("Window Selection");
        windowSelectionLabel.setHorizontalAlignment(SwingConstants.LEFT);

        Font font = windowSelectionLabel.getFont();
        windowSelectionLabel.setFont(new Font(font.getName(), Font.BOLD, 18));

//        windowSelectionLabel.setBorder(new TitledBorder(resources.getString("toolbar.screenCapture.windowSelection")));
//        windowSelectionLabel.setMinimumSize(new Dimension(400, 400));
//        windowSelectionLabel.setPreferredSize(new Dimension(1200, 400));

        JPanel windowPreviewPanel = createPreviewPanel(service.getWindowSources(), previewClickListener);

        // Create screen preview with title

        JLabel screenSelectionLabel = new JLabel();
        screenSelectionLabel.setText("Screen Selection");
        screenSelectionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        screenSelectionLabel.setFont(new Font(font.getName(), Font.BOLD, 18));

//        screenSelectionLabel.setBorder(new TitledBorder(resources.getString("toolbar.screenCapture.screenSelection")));
//        screenSelectionLabel.setMinimumSize(new Dimension(400, 400));
//        screenSelectionLabel.setPreferredSize(new Dimension(1200, 400));

        JPanel screenPreviewPanel = createPreviewPanel(service.getWindowSources(), previewClickListener);

        // Initialize buttons

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        confirmButton = new JButton();
        confirmButton.setText(resources.getString("button.ok"));

        cancelButton = new JButton();
        cancelButton.setText(resources.getString("button.cancel"));

        JButton startRecordingButton = new JButton();
        JButton stopRecordingButton = new JButton();

        startRecordingButton.setText("Start Recording");
        startRecordingButton.addActionListener(event -> {
            if (!service.isRecording()) {
                if (service.startCapture()) {
                    startRecordingButton.setEnabled(false);
                    stopRecordingButton.setEnabled(true);
                }
                else {
                    System.out.println("Failed to start recording, no source selected.");
                }
            }
        });

        stopRecordingButton.setText("Stop Recording");
        stopRecordingButton.addActionListener(event -> {
            if (service.isRecording()) {
                service.stopCapture();
                stopRecordingButton.setEnabled(false);
                startRecordingButton.setEnabled(true);
            }
        });

        buttonPanel.add(startRecordingButton);
        buttonPanel.add(stopRecordingButton);

        add(windowSelectionLabel);
        add(windowPreviewPanel);
//        add(screenSelectionLabel);
//        add(screenPreviewPanel);
        add(buttonPanel);
    }

    private JPanel createPreviewPanel(List<DesktopSource> sources, MouseListener previewClickListener) {
        JPanel panel = new JPanel();
        panel.setLayout(new WrapFlowLayout(FlowLayout.LEFT, 10, 10));

//        Dimension size = panel.getPreferredSize();
//        size.width = 1200;
//        panel.setPreferredSize(size);

        for (var source : sources) {
            service.captureWindowScreenshot(source, (result, frame) -> {
                if (result == DesktopCapturer.Result.SUCCESS) {
                    SourcePreview preview = new SourcePreview(source);
                    preview.addMouseListener(previewClickListener);
                    preview.drawFrame(frame);
                    panel.add(preview);
                }
            });
        }

        return panel;
    }

    private static class SourcePreview extends JPanel {

        private final AtomicBoolean isActive = new AtomicBoolean();
        private final DesktopSource source;

        private final Border activeBorder = new LineBorder(Color.red, 3);
        private final Border defaultBorder = new EmptyBorder(3, 3, 3, 3);

        private ImageView imageView;

        public SourcePreview(DesktopSource source) {
            this.source = source;
            initialize();
        }

        public DesktopSource getSource() {
            return source;
        }

        public boolean isActive() {
            return isActive.get();
        }

        public void setActive(boolean active) {
            isActive.set(active);

            if (active) {
                setBorder(activeBorder);
            } else {
                setBorder(defaultBorder);
            }
        }

        public void drawFrame(DesktopFrame frame) {
            imageView.drawFrame(frame);
        }

        private void initialize() {
            setLayout(new BorderLayout(10, 10));
            setMaximumSize(new Dimension(1200, 200));
            setBorder(defaultBorder);

            imageView = new ImageView();
            imageView.setPreferredSize(new Dimension(250, 150));

            JLabel windowLabel = new JLabel();
            windowLabel.setText(source.title);
            windowLabel.setPreferredSize(new Dimension(250, 15));

            add(imageView, BorderLayout.CENTER);
            add(windowLabel, BorderLayout.SOUTH);
        }
    }
}