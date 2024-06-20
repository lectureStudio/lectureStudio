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

package org.lecturestudio.editor.api.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.tool.PresetColor;

public class DefaultConfiguration extends EditorConfiguration {

	public DefaultConfiguration() {
		setApplicationName("lectureEditor");
		setLocale(Locale.getDefault());
		setCheckNewVersion(true);
		setUIControlSize(9);
		setExtendPageDimension(new Dimension2D(6.0, 4.5));
		setStartMaximized(true);
		setAdvancedUIMode(false);
		setExtendedFullscreen(false);
		setVideoExportPath(new File(System.getProperty("user.home"), "Desktop").getAbsolutePath());
		setActionsUniteThreshold(700);

		getWhiteboardConfig().setBackgroundColor(Color.WHITE);
		getWhiteboardConfig().setVerticalLinesVisible(false);
		getWhiteboardConfig().setVerticalLinesInterval(10);
		getWhiteboardConfig().setHorizontalLinesVisible(false);
		getWhiteboardConfig().setHorizontalLinesInterval(10);
		getWhiteboardConfig().setGridColor(new Color(230, 230, 230));
		getWhiteboardConfig().setShowGridOnDisplays(false);

		getDisplayConfig().setAutostart(true);
		getDisplayConfig().setBackgroundColor(Color.WHITE);

		getToolConfig().getPenSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getPenSettings().setWidth(0.003);
		getToolConfig().getHighlighterSettings().setColor(PresetColor.ORANGE.getColor());
		getToolConfig().getHighlighterSettings().setAlpha(140);
		getToolConfig().getHighlighterSettings().setWidth(0.011);
		getToolConfig().getHighlighterSettings().setScale(false);
		getToolConfig().getPointerSettings().setColor(PresetColor.RED.getColor());
		getToolConfig().getPointerSettings().setAlpha(140);
		getToolConfig().getPointerSettings().setWidth(0.011);
		getToolConfig().getArrowSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getArrowSettings().setWidth(0.003);
		getToolConfig().getLineSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getLineSettings().setWidth(0.003);
		getToolConfig().getRectangleSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getRectangleSettings().setWidth(0.003);
		getToolConfig().getEllipseSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getEllipseSettings().setWidth(0.003);
		getToolConfig().getTextSelectionSettings().setColor(PresetColor.ORANGE.getColor());
		getToolConfig().getTextSelectionSettings().setAlpha(140);
		getToolConfig().getTextSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getTextSettings().setFont(new Font("Arial", 24));
		getToolConfig().getTextSettings().setTextAttributes(new TextAttributes());
		getToolConfig().getLatexSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getLatexSettings().setFont(new TeXFont(TeXFont.Type.SERIF, 20));

		getToolConfig().getPresetColors().addAll(new ArrayList<>(6));
		
		Collections.fill(getToolConfig().getPresetColors(), Color.WHITE);

		getAudioConfig().setPlaybackVolume(1);
	}

}
