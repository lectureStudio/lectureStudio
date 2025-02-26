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

package org.lecturestudio.swing.layout;

import javax.swing.*;
import java.awt.*;

/**
 * VideoTileLayout is a custom LayoutManager that arranges components in a tiled grid,
 * optimized for video conferencing or similar applications where multiple video feeds need
 * to be displayed. It dynamically calculates the optimal number of rows and columns based
 * on the available space and the number of components to maintain a balanced layout.
 * <p>
 * The layout attempts to center the tiles within the container, with a specified padding
 * between each tile. The tile sizes are calculated to maximize the use of the available space.
 *
 * @author Alex Andres
 */
public class VideoTileLayout implements LayoutManager {

	/** Padding between components in pixels. */
	private final int padding;


	/**
	 * Constructs a VideoTileLayout with default padding of 5 pixels.
	 */
	public VideoTileLayout() {
		this(5);
	}

	/**
	 * Constructs a VideoTileLayout with the specified padding between components.
	 *
	 * @param padding the padding in pixels between components
	 */
	public VideoTileLayout(int padding) {
		this.padding = padding;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// Not needed for this implementation.
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		// Not needed for this implementation.
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(800, 600);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(10, 10);
	}

	@Override
	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			int availableWidth = target.getWidth();
			int availableHeight = target.getHeight();

			if (availableWidth <= 0 || availableHeight <= 0) {
				return;
			}

			Insets insets = target.getInsets();
			int maxWidth = availableWidth - (insets.left + insets.right);
			int maxHeight = availableHeight - (insets.top + insets.bottom);

			int participantCount = target.getComponents().length;

			// Calculate the optimal tile layout.
			VideoTileCalculator.TileLayout layout = VideoTileCalculator.calculateOptimalLayout(
					maxWidth,
					maxHeight,
					participantCount,
					padding
			);

			// Position each component.
			int participantIndex = 0;
			for (int row = 0; row < layout.rows(); row++) {
				for (int col = 0; col < layout.cols(); col++) {
					if (participantIndex >= participantCount) {
						break;
					}

					Component comp = target.getComponent(participantIndex);
					if (!comp.isVisible()) {
						continue;
					}

					int cols = layout.cols();

					if (row == layout.rows() - 1) {
						// Calculate the number of columns for the last row.
						cols = participantCount - (layout.rows() - 1) * layout.cols();
					}

					// Calculate the total width needed for all columns.
					double totalWidth = cols * layout.tileWidth() + (cols + 1) * padding;
					// Calculate the left offset to center the tiles horizontally.
					int leftOffset = (int) ((maxWidth - totalWidth) / 2);

					// Calculate position for current tile
					int x = insets.left + padding + col * (layout.tileWidth() + padding) + leftOffset;
					int y = insets.top + padding + row * (layout.tileHeight() + padding);

					comp.setBounds(x, y, layout.tileWidth(), layout.tileHeight());

					participantIndex++;
				}
			}
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Resizable FlowLayout Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 400);

		JPanel panel = new JPanel(new VideoTileLayout());
		panel.setBackground(Color.LIGHT_GRAY);

		for (int i = 0; i < 5; i++) {
			JButton button = new JButton("Button " + (i + 1));
			panel.add(button);
		}

		frame.add(panel);
		frame.setVisible(true);
	}
}
