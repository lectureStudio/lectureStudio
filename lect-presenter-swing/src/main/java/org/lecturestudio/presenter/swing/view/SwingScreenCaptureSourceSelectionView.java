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

import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.DesktopSourceType;
import org.lecturestudio.core.util.ImageUtils;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.ScreenCaptureSourceSelectionView;
import org.lecturestudio.swing.components.ImageView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@SwingView(name = "screen-capture-selection", presenter = org.lecturestudio.presenter.api.presenter.ScreenCaptureSelectionPresenter.class)
public class SwingScreenCaptureSourceSelectionView extends JPanel implements ScreenCaptureSourceSelectionView {

    private JLabel windowSelectionLabel;
    private JLabel screenSelectionLabel;

    private JPanel windowSelectionContainer;
    private JPanel screenSelectionContainer;
    private JPanel buttonContainer;

    private JButton closeButton;
    private JButton okButton;

    private final Map<Long, SourcePreview> previewMap = new HashMap<>();

    private final MouseListener previewMouseListener;
    private SourcePreview selectedPreview;

    public SwingScreenCaptureSourceSelectionView() {
        // Use mouseReleased instead of mouseClick to catch all clicks correctly
        previewMouseListener = new MouseAdapter() {

            private boolean isMouseButtonDown = false;

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isMouseButtonDown && e.getSource() instanceof SourcePreview) {
                    SourcePreview preview = (SourcePreview) e.getSource();
                    if (selectedPreview != null) {
                        selectedPreview.setActive(false);
                    }
                    preview.setActive(true);
                    selectedPreview = preview;
                }
                isMouseButtonDown = false;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isMouseButtonDown = true;
            }
        };
    }

    @Override
    public void addDesktopSource(DesktopSource source, DesktopSourceType type) {

        System.out.println("Add Source: " + source.title);

        SwingUtils.invoke(() -> {
            SourcePreview preview = previewMap.getOrDefault(source.id, null);
            if (preview == null) {
                preview = new SourcePreview(source, type);
                preview.setMouseListener(previewMouseListener);
                previewMap.put(source.id, preview);
                addPreviewToContainer(preview, type);
            } else {
                preview.setWindowTitle(source.title);

                DesktopSourceType currentType = preview.getType();
                if (currentType != type) {
                    addPreviewToContainer(preview, type);
                }
                preview.setType(type);
            }
        });
    }

    @Override
    public void removeDesktopSource(DesktopSource source, DesktopSourceType type) {

        System.out.println("Remove Source: " + source.title);

        SwingUtils.invoke(() -> {
           SourcePreview preview = previewMap.get(source.id);
           if (preview != null) {
               removePreviewFromContainer(preview, type);
               previewMap.remove(source.id);
           }
        });
    }

    @Override
    public void updateSourcePreviewImage(DesktopSource source, BufferedImage image) {
        if (previewMap.containsKey(source.id)) {
            SourcePreview preview = previewMap.get(source.id);
            preview.setImage(image);
        }
    }

    @Override
    public DesktopSource getSelectedSource() {
        return selectedPreview != null ? selectedPreview.getSource() : null;
    }

    @Override
    public void setOnOk(Action action) {
        SwingUtils.bindAction(okButton, action);
    }

    @Override
    public void setOnClose(Action action) {
        SwingUtils.bindAction(closeButton, action);
    }

    @ViewPostConstruct
    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        Font sectionTitleFont = windowSelectionLabel.getFont();
        sectionTitleFont = new Font(sectionTitleFont.getName(), Font.BOLD, 16);

        windowSelectionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        windowSelectionLabel.setFont(sectionTitleFont);
        windowSelectionLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        windowSelectionLabel.setVerticalAlignment(SwingConstants.CENTER);
        windowSelectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridLayout previewContainerLayout = new GridLayout(0, 3, 10, 10);
        windowSelectionContainer.setLayout(previewContainerLayout);

//        screenSelectionLabel.setHorizontalAlignment(SwingConstants.LEFT);
//        screenSelectionLabel.setFont(sectionTitleFont);
//        screenSelectionLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
//        screenSelectionLabel.setVerticalAlignment(SwingConstants.CENTER);
//        screenSelectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        screenSelectionContainer.setLayout(previewContainerLayout);

        buttonContainer.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonContainer.setBorder(new EmptyBorder(5, 5, 5, 5));
//        buttonContainer.add(okButton);
//        buttonContainer.add(closeButton);
    }

    private void addPreviewToContainer(SourcePreview preview, DesktopSourceType type) {
        JPanel container = getContainerComponent(type);
        container.add(preview);
        container.revalidate();
        container.repaint();
    }

    private void removePreviewFromContainer(SourcePreview preview, DesktopSourceType type) {
        JPanel container = getContainerComponent(type);

        Component[] components = container.getComponents();
        for (Component component : components) {
            if (component instanceof SourcePreview) {
                SourcePreview previewComponent = (SourcePreview) component;
                if (preview.equals(previewComponent)) {
                    container.remove(previewComponent);
                }
            }
        }

        container.revalidate();
        container.repaint();
    }

    private JPanel getContainerComponent(DesktopSourceType type) {
        switch (type) {
            case WINDOW:
            case SCREEN:
            default:
                return windowSelectionContainer;
        }
    }



    private static class SourcePreview extends JPanel {

        private final static int MAX_TITLE_LENGTH = 40;
        private final static int PREVIEW_IMG_WIDTH = 250;
        private final static int PREVIEW_IMG_HEIGHT = 140;

        private final static Border ACTIVE_BORDER = new LineBorder(Color.red, 3);
        private final static Border DEFAULT_BORDER = new EmptyBorder(3, 3, 3, 3);

        private final AtomicBoolean isActive = new AtomicBoolean();
        private final DesktopSource source;

        private DesktopSourceType type;

        private ImageView imageView;
        private JLabel titleLabel;

        public SourcePreview(DesktopSource source, DesktopSourceType type) {
            this.source = source;
            this.type = type;
            initialize();
        }

        public DesktopSource getSource() {
            return source;
        }

        public DesktopSourceType getType() {
            return type;
        }

        public void setType(DesktopSourceType type) {
            this.type = type;
        }

        public void setMouseListener(MouseListener listener) {
            addMouseListener(listener);
        }

        public void setActive(boolean active) {
            isActive.set(active);
            SwingUtils.invoke(() -> imageView.setBorder(active ? ACTIVE_BORDER : DEFAULT_BORDER));
        }

        public void setImage(BufferedImage image) {
            SwingUtils.invoke(() -> imageView.setImage(image));
        }

        private void initialize() {
            setLayout(new BorderLayout(0, 0));
            setPreferredSize(new Dimension(250, 165));

            imageView = new ImageView();
            imageView.setPreferredSize(new Dimension(PREVIEW_IMG_WIDTH, PREVIEW_IMG_HEIGHT));
            imageView.setMinimumSize(new Dimension(PREVIEW_IMG_WIDTH, PREVIEW_IMG_HEIGHT));
            imageView.setBorder(DEFAULT_BORDER);

            BufferedImage placeholderImage = ImageUtils.createBufferedImage(PREVIEW_IMG_WIDTH, PREVIEW_IMG_HEIGHT, Color.lightGray);
            imageView.setImage(placeholderImage);

            titleLabel = new JLabel();
            titleLabel.setVerticalAlignment(SwingConstants.CENTER);
            titleLabel.setPreferredSize(new Dimension(PREVIEW_IMG_WIDTH, 25));
            titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 14));

            setWindowTitle(source.title);

            add(imageView, BorderLayout.CENTER);
            add(titleLabel, BorderLayout.SOUTH);
        }

        public void setWindowTitle(String title) {
            if (title != null) {
                String windowTitle = title.length() > MAX_TITLE_LENGTH ?
                        title.substring(0, MAX_TITLE_LENGTH) + "..." :
                        title;
                SwingUtils.invoke(() -> titleLabel.setText(windowTitle));
            }
        }
    }
}
