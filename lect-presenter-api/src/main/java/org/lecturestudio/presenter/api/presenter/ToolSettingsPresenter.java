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

package org.lecturestudio.presenter.api.presenter;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.ToolConfiguration;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.tool.StrokeSettings;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.view.ToolSettingsView;

public class ToolSettingsPresenter extends Presenter<ToolSettingsView> {

	private final DefaultConfiguration defaultConfig;


	@Inject
	public ToolSettingsPresenter(ApplicationContext context, ToolSettingsView view) {
		super(context, view);

		this.defaultConfig = new DefaultConfiguration();
	}

	public void reset() {
		ToolConfiguration toolConfig = context.getConfiguration().getToolConfig();
		StrokeSettings highlighterSettings = toolConfig.getHighlighterSettings();
		StrokeSettings penSettings = toolConfig.getPenSettings();
		StrokeSettings pointerSettings = toolConfig.getPointerSettings();
		StrokeSettings lineSettings = toolConfig.getLineSettings();
		StrokeSettings arrowSettings = toolConfig.getArrowSettings();
		StrokeSettings rectangleSettings = toolConfig.getRectangleSettings();
		StrokeSettings ellipseSettings = toolConfig.getEllipseSettings();

		highlighterSettings.setScale(defaultConfig.getToolConfig().getHighlighterSettings().getScale());
		highlighterSettings.setWidth(defaultConfig.getToolConfig().getHighlighterSettings().getWidth());
		penSettings.setWidth(defaultConfig.getToolConfig().getPenSettings().getWidth());
		pointerSettings.setWidth(defaultConfig.getToolConfig().getPointerSettings().getWidth());
		lineSettings.setWidth(defaultConfig.getToolConfig().getLineSettings().getWidth());
		arrowSettings.setWidth(defaultConfig.getToolConfig().getArrowSettings().getWidth());
		rectangleSettings.setWidth(defaultConfig.getToolConfig().getRectangleSettings().getWidth());
		ellipseSettings.setWidth(defaultConfig.getToolConfig().getEllipseSettings().getWidth());
	}

	@Override
	public void initialize() {
		ToolConfiguration toolConfig = context.getConfiguration().getToolConfig();

		view.setScaleHighlighter(toolConfig.getHighlighterSettings().scaleProperty());
		view.setHighlighterWidth(toolConfig.getHighlighterSettings().widthProperty());
		view.setPenWidth(toolConfig.getPenSettings().widthProperty());
		view.setPointerWidth(toolConfig.getPointerSettings().widthProperty());
		view.setLineWidth(toolConfig.getLineSettings().widthProperty());
		view.setArrowWidth(toolConfig.getArrowSettings().widthProperty());
		view.setRectangleWidth(toolConfig.getRectangleSettings().widthProperty());
		view.setEllipseWidth(toolConfig.getEllipseSettings().widthProperty());
		view.setOnReset(this::reset);
	}
}
