/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.quiz.wordcloud;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * This class implements a horizontal word cloud layout algorithm.
 * It arranges words in a horizontal layout, ensuring they do not overlap
 * and are placed according to their frequency and specified layout settings.
 * <p>
 * The layout starts from the center of the canvas and expands outward,
 * placing words in bands with vertical variance for a more natural look.
 * It uses a FontRenderContext for accurate text measurements and collision detection.
 */
public class HorizontalWordLayout {

	/** Width of the canvas for the word cloud. */
	private final int canvasWidth;

	/** Height of the canvas for the word cloud. */
	private final int canvasHeight;

	/** List of placed words with their bounding rectangles. */
	private final List<Rectangle2D> placedWords;

	/** Font rendering context for accurate text measurements. */
	private final FontRenderContext frc;

	/** Configuration for layout settings such as padding and variance. */
	private final LayoutConfig config;

	/** Calculator for determining font sizes based on word frequency. */
	private final FontCalculator fontCalculator;

	/** Random number generator for vertical variance and random placements. */
	private final Random random;


	/**
	 * Constructs a new horizontal word cloud layout with the specified dimensions and default layout configuration.
	 *
	 * @param width  the width of the canvas in pixels.
	 * @param height the height of the canvas in pixels.
	 */
	public HorizontalWordLayout(int width, int height) {
		this(width, height, new LayoutConfig(), new TieredFontCalculator(4, 18, 48));
	}

	/**
	 * Constructs a new horizontal word cloud layout with the specified dimensions and custom configuration.
	 *
	 * @param width          the width of the canvas in pixels.
	 * @param height         the height of the canvas in pixels.
	 * @param config         the layout configuration containing padding, variance, and spacing settings.
	 * @param fontCalculator the calculator to determine font sizes based on word frequency.
	 */
	public HorizontalWordLayout(int width, int height, LayoutConfig config, FontCalculator fontCalculator) {
		this.canvasWidth = width;
		this.canvasHeight = height;
		this.config = config;
		this.fontCalculator = fontCalculator;
		this.placedWords = new ArrayList<>();
		this.random = new Random();

		// Create a temporary image to get FontRenderContext
		BufferedImage temp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = temp.createGraphics();
		this.frc = g2d.getFontRenderContext();
		g2d.dispose();
	}

	/**
	 * Lays out the words in the word cloud based on their frequency and the configured layout settings.
	 *
	 * @param words the list of words to be laid out, each with a frequency and text.
	 */
	public void layoutWords(List<WordItem> words) {
		// Sort words by frequency (largest first)
		words.sort((a, b) -> Integer.compare(b.frequency, a.frequency));

		// Calculate font sizes based on frequency.
		fontCalculator.calculateFontSizes(words);

		// Place words starting from the center.
		int centerX = canvasWidth / 2;
		int centerY = canvasHeight / 2;

		for (WordItem word : words) {
			// Calculate text bounds.
			word.bounds = word.font.getStringBounds(word.text, frc);

			// Find a non-colliding position.
			Point position = findHorizontalPosition(word, centerX, centerY);
			word.position = position;

			// Add to the placed words list (store actual bounds with padding).
			Rectangle2D placedBounds = new Rectangle2D.Double(
					position.x - config.horizontalPadding,
					position.y - word.bounds.getHeight() - config.verticalPadding,
					word.bounds.getWidth() + 2 * config.horizontalPadding,
					word.bounds.getHeight() + 2 * config.verticalPadding
			);
			placedWords.add(placedBounds);
		}
	}

	/**
	 * Renders the word cloud onto the provided Graphics2D context.
	 * This method draws each word at its calculated position with its assigned font and color.
	 *
	 * @param g2d   the Graphics2D context to render the word cloud onto.
	 * @param words the list of words to render, each with its position, font, and color.
	 */
	public void renderWordCloud(Graphics2D g2d, List<WordItem> words) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Clear background
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, canvasWidth, canvasHeight);

		// Draw words
		for (WordItem word : words) {
			g2d.setFont(word.font);
			g2d.setColor(word.color);
			g2d.drawString(word.text, word.position.x, word.position.y);
		}

		// Optional: Draw bounding boxes for debugging
		// drawBoundingBoxes(g2d);
	}

	/**
	 * Finds a valid horizontal position for the word, trying to center it around the given coordinates.
	 * It expands outward in bands to find a non-colliding position.
	 *
	 * @param word     the word item to place.
	 * @param centerX  the x-coordinate to center around.
	 * @param centerY  the y-coordinate to center around.
	 *
	 * @return a Point representing the valid position for the word, or null if no position is found.
	 */
	private Point findHorizontalPosition(WordItem word, int centerX, int centerY) {
		int wordWidth = (int) word.bounds.getWidth();
		int wordHeight = (int) word.bounds.getHeight();

		// Calculate baseline spacing with extra padding for vertical variance
		int extraPadding = config.enableVerticalVariance ? config.maxVerticalVariance * 2 : 0;
		int baseLineSpacing = Math.max(wordHeight + config.verticalPadding + extraPadding, config.minLineSpacing);

		// Try positions in expanding horizontal bands with proper line spacing
		for (int lineOffset = 0; lineOffset < 20; lineOffset++) {
			// Try multiple positions with variance for each line
			for (int attempt = 0; attempt < 5; attempt++) {
				// Try above center
				int baseY = centerY - (lineOffset * baseLineSpacing);
				int y = applyVerticalVariance(baseY, wordHeight);
				if (isValidYPosition(y, wordHeight)) {
					Point position = findValidXPosition(word, y, wordWidth, wordHeight);
					if (position != null) {
						return position;
					}
				}

				// Try below center (if different from above)
				if (lineOffset > 0) {
					baseY = centerY + (lineOffset * baseLineSpacing);
					y = applyVerticalVariance(baseY, wordHeight);
					if (isValidYPosition(y, wordHeight)) {
						Point position = findValidXPosition(word, y, wordWidth, wordHeight);
						if (position != null) {
							return position;
						}
					}
				}
			}
		}

		// Fallback position without variance
		return new Point(10, centerY);
	}

	/**
	 * Applies vertical variance to the base Y position of the word.
	 * This adds a random offset within the configured maximum variance.
	 *
	 * @param baseY      the base Y position to adjust.
	 * @param wordHeight the height of the word to ensure it fits within bounds.
	 *
	 * @return the adjusted Y position with variance applied.
	 */
	private int applyVerticalVariance(int baseY, int wordHeight) {
		if (!config.enableVerticalVariance || config.maxVerticalVariance <= 0) {
			return baseY;
		}

		// Apply random vertical offset
		int variance = random.nextInt(config.maxVerticalVariance * 2 + 1) - config.maxVerticalVariance;
		int adjustedY = baseY + variance;

		// Ensure the word stays within canvas bounds
		adjustedY = Math.max(wordHeight, adjustedY);
		adjustedY = Math.min(canvasHeight, adjustedY);

		return adjustedY;
	}

	/**
	 * Checks if the given Y position is valid for placing a word.
	 * The position must be within the canvas height and allow for the word's height.
	 *
	 * @param y          the Y position to check.
	 * @param wordHeight the height of the word to ensure it fits within bounds.
	 *
	 * @return true if the position is valid, false otherwise.
	 */
	private boolean isValidYPosition(int y, int wordHeight) {
		return y - wordHeight >= 0 && y <= canvasHeight;
	}

	/**
	 * Finds a valid X position for the word at the specified Y coordinate.
	 * It tries to center the word and expands outward to find a non-colliding position.
	 *
	 * @param word       the word item to place.
	 * @param y          the Y coordinate where the word should be placed.
	 * @param wordWidth  the width of the word.
	 * @param wordHeight the height of the word.
	 *
	 * @return a Point representing the valid X position, or null if no position is found.
	 */
	private Point findValidXPosition(WordItem word, int y, int wordWidth, int wordHeight) {
		int centerX = canvasWidth / 2;

		// Try center first
		if (canPlaceAtWithPadding(centerX - wordWidth / 2, y, wordWidth, wordHeight)) {
			return new Point(centerX - wordWidth / 2, y);
		}

		// Try positions expanding from the center
		for (int offset = 10; offset < canvasWidth / 2; offset += 5) {
			// Try right of the center
			int x = centerX + offset;
			if (x + wordWidth <= canvasWidth &&
					canPlaceAtWithPadding(x, y, wordWidth, wordHeight)) {
				return new Point(x, y);
			}

			// Try left of the center
			x = centerX - offset - wordWidth;
			if (x >= 0 && canPlaceAtWithPadding(x, y, wordWidth, wordHeight)) {
				return new Point(x, y);
			}
		}

		// Scan the entire width for any available space
		for (int x = 0; x <= canvasWidth - wordWidth; x += 3) {
			if (canPlaceAtWithPadding(x, y, wordWidth, wordHeight)) {
				return new Point(x, y);
			}
		}

		// No space available
		return null;
	}

	/**
	 * Checks if a rectangle can be placed at the specified position with padding.
	 * This checks against all previously placed words and ensures it fits within canvas boundaries.
	 *
	 * @param x      the X coordinate to place the rectangle.
	 * @param y      the Y coordinate to place the rectangle.
	 * @param width  the width of the rectangle.
	 * @param height the height of the rectangle.
	 *
	 * @return true if the rectangle can be placed, false otherwise.
	 */
	private boolean canPlaceAtWithPadding(int x, int y, int width, int height) {
		// Create rectangle with padding included
		Rectangle2D testRect = new Rectangle2D.Double(
				x - config.horizontalPadding,
				y - height - config.verticalPadding,
				width + 2 * config.horizontalPadding,
				height + 2 * config.verticalPadding
		);

		// Check against all placed words
		for (Rectangle2D placed : placedWords) {
			if (testRect.intersects(placed)) {
				return false;
			}
		}

		// Check canvas boundaries (without padding for actual placement)
		return x >= 0 && y - height >= 0 && x + width <= canvasWidth && y <= canvasHeight;
	}

	private void drawBoundingBoxes(Graphics2D g2d) {
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.setStroke(new BasicStroke(1));
		for (Rectangle2D rect : placedWords) {
			g2d.draw(rect);
		}
	}
}
