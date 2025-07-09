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
import java.awt.geom.Rectangle2D;

/**
 * Represents a word item in a word cloud, containing its text, frequency,
 * font, color, bounding rectangle, and position.
 * <p>
 * This class encapsulates all properties needed to render a word in a word cloud
 * visualization. Each word is positioned based on its frequency and other layout
 * algorithms. Words with higher frequency are typically rendered with larger fonts.
 * The bounds and position properties are used during layout calculations to ensure
 * words don't overlap.
 */
public class WordItem {

	/** The text of the word. */
	String text;

	/** The frequency of the word, indicating how often it appears. */
	int frequency;

	/** The font used to render the word. */
	Font font;

	/** The color of the word in the word cloud. */
	Color color;

	/** The bounding rectangle of the word in the word cloud. */
	Rectangle2D bounds;

	/** The position of the word in the word cloud. */
	Point position;


	// ::::::::::::::: ADDITIONAL ATTRIBUTES :::::::::::::::



	/**
	 * Constructs a WordItem with the specified text and frequency.
	 *
	 * @param text      The text of the word.
	 * @param frequency The frequency of the word.
	 */
	public WordItem(String text, int frequency) {
		this.text = text;
		this.frequency = frequency;
		this.position = new Point();
	}



	// ======================================================================
	//                          GETTERS AND SETTERS
	// ======================================================================


	/**
	 * Retrieves the text of the word item.
	 *
	 * @return the text of this word item as a String.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text of the word item.
	 *
	 * @param text the new text to be assigned to the word item.
	 */
	public void setText(final String text) {
		this.text = text;
	}

	/**
	 * Retrieves the frequency of the word.
	 *
	 * @return the frequency of the word, indicating how often it appears.
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * Sets the frequency of the word.
	 *
	 * @param frequency the new frequency value to be assigned to the word.
	 */
	public void setFrequency(final int frequency) {
		this.frequency = frequency;
	}

	/**
	 * Retrieves the font used to render the word in the word cloud.
	 *
	 * @return the font assigned to this word.
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * Sets the font used to render the word in the word cloud.
	 *
	 * @param font the font to be applied to the word.
	 */
	public void setFont(Font font) {
		this.font = font;
	}

	/**
	 * Retrieves the color of the word in the word cloud.
	 * The color represents the visual appearance used to render the word.
	 *
	 * @return the color of the word as a Color object.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color of the word.
	 *
	 * @param color the color to be assigned to the word.
	 *                 This value determines
	 *              the appearance of the word in the word cloud visualization.
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Retrieves the bounding rectangle of the word item.
	 * The bounding rectangle represents the area occupied by the word,
	 * which is used for positioning and collision detection in the word cloud layout.
	 *
	 * @return the bounding rectangle of the word item as a {@code Rectangle2D} object.
	 */
	public Rectangle2D getBounds() {
		return bounds;
	}

	/**
	 * Sets the bounding rectangle for the word item in the word cloud.
	 * The bounding rectangle defines the area occupied by the word, which is used
	 * for positioning and collision detection within the word cloud layout.
	 *
	 * @param bounds the bounding rectangle for the word item.
	 */
	public void setBounds(final Rectangle2D bounds) {
		this.bounds = bounds;
	}

	/**
	 * Retrieves the position of the word item.
	 * The position is represented as a point in a 2D coordinate system
	 * and is used to determine the placement of the word in the word cloud layout.
	 *
	 * @return the current position of the word item as a {@code Point} object.
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * Sets the position of the word in the word cloud.
	 *
	 * @param position the new position to be assigned to this word item.
	 */
	public void setPosition(final Point position) {
		this.position = position;
	}

	/**
	 * Updates the position of the word item in the word cloud.
	 * This method sets the position of the word item using the specified x and y coordinates.
	 * It internally creates a new {@code Point} object to represent the position.
	 *
	 * @param x the x-coordinate of the new position.
	 * @param y the y-coordinate of the new position.
	 */
	public void setPosition(int x, int y) {
		setPosition(new Point(x, y));
	}

	/**
	 * Determines if this {@code WordItem} will collide with another {@code WordItem}
	 * based on their bounding rectangles.
	 *
	 * @param other the {@code WordItem} to check for collision with this one.
	 * @return {@code true} if the bounding rectangles of the two {@code WordItem} objects intersect;
	 *         {@code false} otherwise.
	 */
	public boolean willCollide(final WordItem other) {
		// Check if the bounding rectangles of two words overlap
		return this.bounds.intersects(other.bounds);
	}

}
