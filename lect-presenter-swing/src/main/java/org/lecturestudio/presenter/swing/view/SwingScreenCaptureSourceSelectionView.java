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

import javax.inject.Inject;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@SwingView(name = "screen-capture-selection")
public class SwingScreenCaptureSourceSelectionView extends JPanel implements ScreenCaptureSourceSelectionView {

    private JLabel windowSelectionLabel;
    private JScrollPane windowSelectionScrollPane;
    private JPanel windowSelectionContainer;

    private JLabel screenSelectionLabel;
    private JScrollPane screenSelectionScrollPane;
    private JPanel screenSelectionContainer;

    private JPanel buttonContainer;
    private JButton closeButton;
    private JButton okButton;

    private final Map<Long, SourcePreview> previewMap = new HashMap<>();

    private final MouseListener previewMouseListener;

    private SourcePreview selectedPreview;

    @Inject
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
        SwingUtils.invoke(() -> {
            SourcePreview preview = previewMap.getOrDefault(source.id, null);
            if (preview == null) {
                preview = new SourcePreview(source, type);
                preview.setMouseListener(previewMouseListener);
                addPreviewToContainer(preview, type);
                previewMap.put(source.id, preview);
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
        SwingUtils.invoke(() -> {
            SourcePreview preview = previewMap.get(source.id);
            if (preview != null) {
                removePreviewFromContainer(preview, type);
                previewMap.remove(source.id);

                if (selectedPreview == preview) {
                    selectedPreview = null;
                }
            }
        });
    }

    @Override
    public void updateSourcePreviewImage(DesktopSource source, BufferedImage image) {
        SourcePreview preview = previewMap.getOrDefault(source.id, null);
        if (preview != null) {
            preview.updateImage(image);
        }
    }

    @Override
    public SelectedDesktopSource getSelectedDesktopSource() {
        return selectedPreview;
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
        setBorder(new EmptyBorder(0, 20, 20, 20));
        setAlignmentX(CENTER_ALIGNMENT);

        GridLayout previewContainerLayout = new GridLayout(0, 3, 10, 10);

        Font sectionTitleFont = windowSelectionLabel.getFont();
        sectionTitleFont = new Font(sectionTitleFont.getName(), Font.BOLD, 16);

        // Init window selection
        initSectionLabel(windowSelectionLabel, sectionTitleFont);
        initScrollPane(windowSelectionScrollPane, 2);
        windowSelectionContainer.setLayout(previewContainerLayout);

        // Init screen selection
        initSectionLabel(screenSelectionLabel, sectionTitleFont);
        initScrollPane(screenSelectionScrollPane, 1);

        screenSelectionContainer.setLayout(previewContainerLayout);

        // Init buttons
        buttonContainer.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonContainer.setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    private void initScrollPane(JScrollPane pane, int numberOfRows) {
        pane.setBorder(BorderFactory.createEmptyBorder());

        int paneHeight = numberOfRows * 165 + (numberOfRows - 1) * 10;

        pane.setMinimumSize(new Dimension(780, 165));
        pane.setPreferredSize(new Dimension(780, paneHeight));
        pane.setMaximumSize(new Dimension(780, paneHeight));

        // Allow only vertical scrolling and show the scrollbar always to prevent issues with overlapping
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private void initSectionLabel(JLabel label, Font font) {
        label.setBorder(new EmptyBorder(5, 5, 5, 5));
        label.setPreferredSize(new Dimension(0, 50));
        label.setAlignmentX(CENTER_ALIGNMENT);
//        label.setHorizontalAlignment(SwingConstants.CENTER);
//        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setFont(font);
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
        return type == DesktopSourceType.WINDOW ? windowSelectionContainer : screenSelectionContainer;
    }


    private static class UpdatePreviewTask extends SwingWorker<BufferedImage, BufferedImage> {

        private final ImageView view;
        private final BufferedImage src;

        public UpdatePreviewTask(ImageView view, BufferedImage src) {
            this.view = view;
            this.src = src;
        }

        @Override
        protected BufferedImage doInBackground() throws Exception {
            return ImageUtils.cropAndScale(src, SourcePreview.PREVIEW_IMG_WIDTH, SourcePreview.PREVIEW_IMG_HEIGHT);
        }

        @Override
        protected void done() {
            try {
                view.setImage(get());
                view.repaint();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }


    private static class SourcePreview extends JPanel implements SelectedDesktopSource {

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

        private BufferedImage frame;

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

        public BufferedImage getFrame() {
            return frame;
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

        public void updateImage(BufferedImage image) {
            new UpdatePreviewTask(imageView, image).execute();
            frame = image;
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
