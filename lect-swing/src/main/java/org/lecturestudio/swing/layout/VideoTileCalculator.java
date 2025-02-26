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

/**
 * The {@code VideoTileCalculator} class provides utility methods for calculating the optimal layout
 * of video tiles within a container, ensuring the tiles fit within the container while maintaining
 * a specified aspect ratio.
 *
 * @author Alex Andres
 */
public class VideoTileCalculator {

	private static final double ASPECT_RATIO = 16.0 / 9.0;

	/**
	 * A record representing the layout of video tiles in a grid.
	 *
	 * @param rows       The number of rows in the grid layout.
	 * @param cols       The number of columns in the grid layout.
	 * @param tileWidth  The width of each individual tile in pixels.
	 * @param tileHeight The height of each individual tile in pixels.
	 */
	public record TileLayout(int rows, int cols, int tileWidth, int tileHeight) { }


	/**
	 * Calculates the optimal tile layout for the given container size and number of participants
	 * while maintaining the desired aspect ratio for each tile.
	 *
	 * @param containerWidth   The width of the container.
	 * @param containerHeight  The height of the container.
	 * @param participantCount The number of video tiles needed.
	 * @param padding          The padding between tiles.
	 *
	 * @return TileLayout containing the optimal arrangement
	 */
	public static TileLayout calculateOptimalLayout(
			int containerWidth,
			int containerHeight,
			int participantCount,
			int padding) {

		if (participantCount <= 0) {
			return new TileLayout(0, 0, 0, 0);
		}

		// Initialize variables to store the best layout.
		int bestRows = 1;
		int bestCols = 1;
		int maxTileWidth = 0;
		int maxTileHeight = 0;
		double bestArea = 0;

		// Try different combinations of rows and columns.
		for (int rows = 1; rows <= participantCount; rows++) {
			// Calculate needed columns for current row count.
			int cols = (int) Math.ceil((double) participantCount / rows);

			// Calculate available space considering padding.
			int availableWidth = containerWidth - (padding * (cols + 1));
			int availableHeight = containerHeight - (padding * (rows + 1));

			if (availableWidth <= 0 || availableHeight <= 0) {
				continue;
			}

			// Calculate tile size based on available space and aspect ratio.
			int tileWidth = availableWidth / cols;
			int tileHeight = availableHeight / rows;

			// Adjust tile size to maintain the specified aspect ratio.
			if ((double) tileWidth / tileHeight > ASPECT_RATIO) {
				tileWidth = (int) (tileHeight * ASPECT_RATIO);
			}
			else {
				tileHeight = (int) (tileWidth / ASPECT_RATIO);
			}

			// Calculate the area of each tile.
			double tileArea = tileWidth * tileHeight;

			// Update best layout if current layout provides larger tiles.
			if (tileArea > bestArea) {
				bestArea = tileArea;
				bestRows = rows;
				bestCols = cols;
				maxTileWidth = tileWidth;
				maxTileHeight = tileHeight;
			}
		}

		return new TileLayout(bestRows, bestCols, maxTileWidth, maxTileHeight);
	}
}