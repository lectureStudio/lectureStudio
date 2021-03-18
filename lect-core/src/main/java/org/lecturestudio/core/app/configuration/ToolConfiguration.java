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

package org.lecturestudio.core.app.configuration;

import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.tool.LatexToolSettings;
import org.lecturestudio.core.tool.StrokeSettings;
import org.lecturestudio.core.tool.TextSelectionSettings;
import org.lecturestudio.core.tool.TextSettings;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;

/**
 * The ToolConfiguration specifies tool related properties and settings. Tool
 * specific properties are encapsulated in the respective separate configuration
 * classes.
 *
 * @author Alex Andres
 */
public class ToolConfiguration {

	/** The list of pre-defined tool colors. */
	private final ObservableList<Color> presetColors = new ObservableArrayList<>();

	/** The pen tool settings. */
	private final StrokeSettings penSettings = new StrokeSettings();

	/** The highlighter tool settings. */
	private final StrokeSettings highlighterSettings = new StrokeSettings();

	/** The pointer tool settings. */
	private final StrokeSettings pointerSettings = new StrokeSettings();

	/** The arrow tool settings. */
	private final StrokeSettings arrowSettings = new StrokeSettings();

	/** The line tool settings. */
	private final StrokeSettings lineSettings = new StrokeSettings();

	/** The rectangle tool settings. */
	private final StrokeSettings rectangleSettings = new StrokeSettings();

	/** The ellipse tool settings. */
	private final StrokeSettings ellipseSettings = new StrokeSettings();

	/** The text selection tool settings. */
	private final TextSelectionSettings textSelectionSettings = new TextSelectionSettings();

	/** The text tool settings. */
	private final TextSettings textSettings = new TextSettings();

	/** The LaTeX tool settings. */
	private final LatexToolSettings latexSettings = new LatexToolSettings();


	/**
	 * Obtain the observable list of pre-defined tool colors.
	 *
	 * @return the observable list of pre-defined tool colors.
	 */
	public ObservableList<Color> getPresetColors() {
		return presetColors;
	}

	/**
	 * Obtain the pen tool settings.
	 *
	 * @return the pen tool settings.
	 */
	public StrokeSettings getPenSettings() {
		return penSettings;
	}

	/**
	 * Obtain the highlighter tool settings.
	 *
	 * @return the highlighter tool settings.
	 */
	public StrokeSettings getHighlighterSettings() {
		return highlighterSettings;
	}

	/**
	 * Obtain the pointer tool settings.
	 *
	 * @return the pointer tool settings.
	 */
	public StrokeSettings getPointerSettings() {
		return pointerSettings;
	}

	/**
	 * Obtain the arrow tool settings.
	 *
	 * @return the arrow tool settings.
	 */
	public StrokeSettings getArrowSettings() {
		return arrowSettings;
	}

	/**
	 * Obtain the line tool settings.
	 *
	 * @return the line tool settings.
	 */
	public StrokeSettings getLineSettings() {
		return lineSettings;
	}

	/**
	 * Obtain the rectangle tool settings.
	 *
	 * @return the rectangle tool settings.
	 */
	public StrokeSettings getRectangleSettings() {
		return rectangleSettings;
	}

	/**
	 * Obtain the ellipse tool settings.
	 *
	 * @return the ellipse tool settings.
	 */
	public StrokeSettings getEllipseSettings() {
		return ellipseSettings;
	}

	/**
	 * Obtain the text selection tool settings.
	 *
	 * @return the text selection tool settings.
	 */
	public TextSelectionSettings getTextSelectionSettings() {
		return textSelectionSettings;
	}

	/**
	 * Obtain the text tool settings.
	 *
	 * @return the text tool settings.
	 */
	public TextSettings getTextSettings() {
		return textSettings;
	}

	/**
	 * Obtain the LaTeX tool settings.
	 *
	 * @return the LaTeX tool settings.
	 */
	public LatexToolSettings getLatexSettings() {
		return latexSettings;
	}

}
