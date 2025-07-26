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
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An improved font calculator implementation that assigns font sizes in discrete tiers.
 * Words are grouped into tiers based on their frequency ranges, with each tier assigned a specific font size.
 * This creates a stepped visual hierarchy in the word cloud while ensuring words with the same frequency
 * receive the same font size.
 *
 * @author Alex Andres
 */
public class TieredFontCalculator implements FontCalculator {

	/** The list of font sizes for each tier, from largest to smallest. */
	private final List<Integer> tierSizes = new ArrayList<>();

	/** The number of discrete font size levels to use when rendering words. */
	private final int sizeLevels;

	/** The font family to use for word rendering. */
	private final String fontFamily;

	/** The font style to use for word rendering. */
	private final int fontStyle;

	/** The color scheme to use for word rendering. */
	private final ColorScheme colorScheme;

	/**
	 * Enum defining different color schemes for word cloud visualization.
	 */
	public enum ColorScheme {
		/** Classic green to red gradient based on frequency. */
		CLASSIC,

		/** Blue-based color scheme with varying saturation. */
		BLUE_SHADES,

		/** Rainbow spectrum colors. */
		RAINBOW
	}


	/**
	 * Creates a new TieredFontCalculator with specified parameters.
	 *
	 * @param levels     the number of discrete font size tiers to use.
	 * @param minSize    the minimum font size in pixels.
	 * @param maxSize    the maximum font size in pixels.
	 * @param fontFamily the font family to use (defaults to Sans Serif if null).
	 * @param fontStyle  the font style to use (e.g., bold).
	 * @param scheme     the color scheme to use for word rendering.
	 */
	public TieredFontCalculator(int levels, int minSize, int maxSize, String fontFamily, int fontStyle,
								ColorScheme scheme) {
		if (levels < 2) {
			throw new IllegalArgumentException("Number of levels must be at least 2.");
		}
		if (minSize <= 0 || maxSize <= 0) {
			throw new IllegalArgumentException("Font sizes must be positive.");
		}
		if (minSize >= maxSize) {
			throw new IllegalArgumentException("Maximum font size must be greater than minimum font size.");
		}

		this.sizeLevels = levels;
		this.fontFamily = (fontFamily != null) ? fontFamily : Font.SANS_SERIF;
		this.fontStyle = fontStyle;
		this.colorScheme = (scheme != null) ? scheme : ColorScheme.CLASSIC;

		// Calculate the size for each tier.
		double sizeStep = (double) (maxSize - minSize) / (sizeLevels - 1);

		for (int i = 0; i < sizeLevels; i++) {
			tierSizes.add((int) (maxSize - (i * sizeStep)));
		}
	}

	/**
	 * Creates a new TieredFontCalculator with the default font family, style, and color scheme.
	 *
	 * @param levels  the number of discrete font size tiers to use.
	 * @param minSize the minimum font size in pixels.
	 * @param maxSize the maximum font size in pixels.
	 */
	public TieredFontCalculator(int levels, int minSize, int maxSize) {
		this(levels, minSize, maxSize, Font.SANS_SERIF, Font.BOLD, ColorScheme.CLASSIC);
	}

	@Override
	public void calculateFontSizes(List<WordItem> words) {
		if (words == null || words.isEmpty()) {
			return;
		}

		int minFreq = words.get(words.size() - 1).frequency;
		int maxFreq = words.get(0).frequency;

		// If all words have the same frequency, assign them to the middle tier.
		if (maxFreq == minFreq) {
			int middleTier = sizeLevels / 2;
			int fontSize = tierSizes.get(middleTier);

			for (WordItem word : words) {
				word.font = new Font(fontFamily, fontStyle, fontSize);
				word.color = generateColor(word.frequency, maxFreq);
			}
			return;
		}

		// Group words by frequency.
		Map<Integer, List<WordItem>> frequencyGroups = words.stream()
				.collect(Collectors.groupingBy(word -> word.frequency));

		// Sort frequencies in descending order.
		List<Integer> sortedFrequencies = new ArrayList<>(frequencyGroups.keySet());
		sortedFrequencies.sort(Collections.reverseOrder());

		// Calculate frequency ranges for each tier.
		double frequencyRange = (double) (maxFreq - minFreq) / sizeLevels;

		// Assign font sizes based on frequency ranges.
		for (Integer frequency : sortedFrequencies) {
			// Calculate which tier this frequency belongs to.
			int tier;
			if (frequencyRange > 0) {
				tier = Math.min((int) ((maxFreq - frequency) / frequencyRange), sizeLevels - 1);
			}
			else {
				tier = 0; // Default to first tier if there's no range.
			}

			int fontSize = tierSizes.get(tier);

			// Assign the same font size to all words with this frequency.
			for (WordItem word : frequencyGroups.get(frequency)) {
				word.font = new Font(fontFamily, fontStyle, fontSize);
				word.color = generateColor(word.frequency, maxFreq);
			}
		}
	}

	@Override
	public Color generateColor(int frequency, int maxFreq) {
		float intensity = (maxFreq > 0) ? (float) frequency / maxFreq : 0.5f;

		return switch (colorScheme) {
			case BLUE_SHADES -> new Color(
					50 + (int) (intensity * 50),
					50 + (int) (intensity * 100),
					150 + (int) (intensity * 105)
			);
			case RAINBOW -> Color.getHSBColor(
					0.7f - (intensity * 0.7f),
					0.7f + (intensity * 0.3f),
					0.8f + (intensity * 0.2f)
			);
			default -> new Color(
					(int) (intensity * 255),
					(int) ((1 - intensity) * 255),
					100 + (int) (intensity * 155)
			);
		};
	}
}
