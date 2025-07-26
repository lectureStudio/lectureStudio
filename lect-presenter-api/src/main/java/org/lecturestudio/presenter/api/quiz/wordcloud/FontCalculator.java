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
import java.util.List;

/**
 * Interface for calculating font sizes for words in a word cloud.
 * Implementations of this interface define algorithms to determine appropriate
 * font sizes for words based on their frequency or importance.
 *
 * @author Alex Andres
 */
public interface FontCalculator {

	/**
	 * Calculates and assigns font sizes to a list of word items.
	 * The font sizes are typically proportional to the importance or frequency
	 * of each word in the collection.
	 *
	 * @param words The list of word items to calculate font sizes for.
	 */
	void calculateFontSizes(List<WordItem> words);

	/**
	 * Generates a color based on the frequency of the word.
	 * This method can be overridden to provide different color generation strategies.
	 *
	 * @param frequency The frequency of the word.
	 * @param maxFreq   The maximum frequency in the list of words.
	 *
	 * @return A Color object representing the color for the word.
	 */
	Color generateColor(int frequency, int maxFreq);

}
