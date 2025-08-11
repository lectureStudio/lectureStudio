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
}
