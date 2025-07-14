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

/**
 * Configuration class for layout settings in a word cloud.
 * This class allows customization of spacing and variance for word placement in word cloud visualizations.
 * <p>
 * The configuration includes settings for:
 * <ul>
 *   <li>Horizontal spacing between words</li>
 *   <li>Vertical spacing between lines</li>
 *   <li>Vertical variance for more natural-looking layouts</li>
 *   <li>Minimum line spacing constraints</li>
 * </ul>
 * <p>
 * This class uses a fluent interface pattern to allow method chaining:
 * <pre>
 * LayoutConfig config = new LayoutConfig()
 *     .horizontalPadding(10)
 *     .verticalPadding(8)
 *     .enableVerticalVariance(true)
 *     .maxVerticalVariance(20)
 *     .minLineSpacing(12);
 * </pre>
 */
public class LayoutConfig {

	/** Space between words horizontally. */
	public int horizontalPadding = 8;

	/** Base space between lines. */
	public int verticalPadding = 5;

	/** Maximum random vertical offset. */
	public int maxVerticalVariance = 15;

	/** Vertical variance for word placement. */
	public boolean enableVerticalVariance = true;

	/** Minimum line spacing. */
	public int minLineSpacing = 10;


	/**
	 * Default constructor for LayoutConfig.
	 * Initializes the configuration with default values.
	 */
	public LayoutConfig() {
	}

	/**
	 * Sets the horizontal padding between words.
	 *
	 * @param padding the horizontal padding in pixels.
	 *
	 * @return this layout configuration instance for method chaining.
	 */
	public LayoutConfig horizontalPadding(int padding) {
		this.horizontalPadding = padding;
		return this;
	}

	/**
	 * Sets the base space between lines.
	 *
	 * @param padding the vertical padding in pixels.
	 *
	 * @return this layout configuration instance for method chaining.
	 */
	public LayoutConfig verticalPadding(int padding) {
		this.verticalPadding = padding;
		return this;
	}

	/**
	 * Sets the maximum random vertical offset for word placement.
	 *
	 * @param variance the maximum vertical variance in pixels.
	 *
	 * @return this layout configuration instance for method chaining.
	 */
	public LayoutConfig maxVerticalVariance(int variance) {
		this.maxVerticalVariance = variance;
		return this;
	}

	/**
	 * Enables or disables vertical variance for word placement.
	 *
	 * @param enable true to enable vertical variance, false to disable.
	 *
	 * @return this layout configuration instance for method chaining.
	 */
	public LayoutConfig enableVerticalVariance(boolean enable) {
		this.enableVerticalVariance = enable;
		return this;
	}

	/**
	 * Sets the minimum spacing between lines.
	 *
	 * @param spacing the minimum line spacing in pixels.
	 *
	 * @return this layout configuration instance for method chaining.
	 */
	public LayoutConfig minLineSpacing(int spacing) {
		this.minLineSpacing = spacing;
		return this;
	}
}
