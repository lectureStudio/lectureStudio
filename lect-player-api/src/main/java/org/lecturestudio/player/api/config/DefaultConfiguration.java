/*
 * Copyright (C) 2016 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.player.api.config;

import java.util.ArrayList;
import java.util.Locale;

import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.tool.PresetColor;

public class DefaultConfiguration extends PlayerConfiguration {

	public DefaultConfiguration() {
		setApplicationName("lecturePlayer");
		setLocale(Locale.getDefault());
		setUIControlSize(9);
		setExtendPageDimension(new Dimension2D(6.0, 4.5));
		setDocumentsPath(System.getProperty("user.home"));
		setStartMaximized(false);
		setAdvancedUIMode(false);
		setExtendedFullscreen(true);
		
		getGridConfig().setVerticalLinesVisible(false);
		getGridConfig().setVerticalLinesInterval(10);
		getGridConfig().setHorizontalLinesVisible(false);
		getGridConfig().setHorizontalLinesInterval(10);
		getGridConfig().setColor(new Color(230, 230, 230));
		getGridConfig().setShowGridOnDisplays(false);
		
		getWhiteboardConfig().setBackgroundColor(Color.WHITE);
		
		getDisplayConfig().setAutostart(true);
		getDisplayConfig().setBackgroundColor(Color.WHITE);
		getDisplayConfig().setIpPosition(Position.BOTTOM_CENTER);

		getToolConfig().getPenSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getPenSettings().setWidth(0.02);
		getToolConfig().getHighlighterSettings().setColor(PresetColor.ORANGE.getColor());
		getToolConfig().getHighlighterSettings().setAlpha(140);
		getToolConfig().getHighlighterSettings().setWidth(0.07);
		getToolConfig().getHighlighterSettings().setScale(false);
		getToolConfig().getPointerSettings().setColor(PresetColor.RED.getColor());
		getToolConfig().getPointerSettings().setAlpha(140);
		getToolConfig().getPointerSettings().setWidth(0.07);
		getToolConfig().getArrowSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getArrowSettings().setWidth(0.02);
		getToolConfig().getLineSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getLineSettings().setWidth(0.02);
		getToolConfig().getRectangleSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getRectangleSettings().setWidth(0.02);
		getToolConfig().getEllipseSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getEllipseSettings().setWidth(0.02);
		getToolConfig().getTextSelectionSettings().setColor(PresetColor.ORANGE.getColor());
		getToolConfig().getTextSelectionSettings().setAlpha(140);
		getToolConfig().getTextSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getTextSettings().setFont(new Font("Arial", 24));
		getToolConfig().getTextSettings().setTextAttributes(new TextAttributes());
		getToolConfig().getLatexSettings().setColor(PresetColor.BLACK.getColor());
		getToolConfig().getLatexSettings().setFont(new TeXFont(TeXFont.Type.SERIF, 20));

		getToolConfig().getPresetColors().addAll(new ArrayList<>(6));
		
		getAudioConfig().setPlaybackVolume(1);
	}
	
}
