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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.lecturestudio.presenter.api.model.ScreenSourceVideoFrame;
import org.lecturestudio.presenter.api.model.SharedScreenSource;

public class ScreenSourceView extends JPanel {

	private static final Border BORDER = new EmptyBorder(2, 2, 2, 2);

	private static final Border BORDER_HOVER = new LineBorder(Color.GRAY, 2);

	private static final Border BORDER_SELECTED = new LineBorder(Color.BLUE, 2);

	private final List<Consumer<ScreenSourceView>> actionConsumers = new ArrayList<>();

	private final ImageView imageView;

	private final JLabel nameLabel;

	private SharedScreenSource screenSource;

	private boolean selected;


	public ScreenSourceView() {
		setBorder(BORDER);
		setLayout(new BorderLayout(2, 2));

		imageView = new ImageView();
		imageView.setBorder(new EmptyBorder(3, 0, 0, 0));

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

	private void onVideoFrame(ScreenSourceVideoFrame videoFrame) {
		imageView.createBufferedImage(videoFrame);
		imageView.repaint();
	}

	private void updateBorder(Border border) {
		setBorder(selected ? BORDER_SELECTED : border);
	}

	private void notifyAction() {
		for (var consumer : actionConsumers) {
			consumer.accept(this);
		}
	}



	private static class ImageView extends JPanel {

		private BufferedImage image;


		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g;

			if (nonNull(image)) {
				AffineTransform transform = g2.getTransform();
				AffineTransform imageTransform = new AffineTransform();
				imageTransform.translate(transform.getTranslateX(), transform.getTranslateY());

				int x = (int) ((getWidth() * transform.getScaleX() - image.getWidth(null)) / 2);
				int y = getInsets().top + getInsets().bottom;

				g2.setTransform(imageTransform);
				g2.drawImage(image, x, y, null);
				g2.setTransform(transform);
			}
		}

		void createBufferedImage(ScreenSourceVideoFrame videoFrame) {
			final ByteBuffer buf = videoFrame.buffer;
			final Dimension dim = videoFrame.frameSize;
			final int frameWidth = videoFrame.frameSize.width;
			final int frameHeight = videoFrame.frameSize.height;

			Insets insets = getInsets();
			int viewHeight = getHeight() - insets.top - insets.bottom;

			double uiScale = getGraphicsConfiguration().getDefaultTransform().getScaleX();
			double scale = viewHeight / (double) frameHeight * uiScale;
			int imageWidth = (int) (frameWidth * scale);
			int imageHeight = (int) (frameHeight * scale);

			if (isNull(image) || image.getWidth() != imageWidth || image.getHeight() != imageHeight) {
				image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
			}

			// Copy frame to buffered image.
			int bytesPerPixel = 4;
			int bufferSize = dim.width * dim.height * bytesPerPixel;

			DataBufferByte dataBuffer = new DataBufferByte(bufferSize);

			WritableRaster raster = Raster.createInterleavedRaster(dataBuffer,
					dim.width,
					dim.height,
					dim.width * bytesPerPixel,
					bytesPerPixel,
					new int[] { 2, 1, 0, 3 },
					null);

			ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
					new int[] { 8, 8, 8, 8 },
					true,
					false,
					ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

			BufferedImage tempImage = new BufferedImage(colorModel, raster, false, null);
			byte[] imageBuffer = ((DataBufferByte) tempImage.getRaster().getDataBuffer()).getData();

			buf.get(imageBuffer);

			// Draw frame.
			Graphics2D g2 = image.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(tempImage, 0, 0, imageWidth, imageHeight, null);
			g2.dispose();
		}
	}
}