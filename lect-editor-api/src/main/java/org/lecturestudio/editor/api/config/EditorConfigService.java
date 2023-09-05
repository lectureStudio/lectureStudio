package org.lecturestudio.editor.api.config;

import org.lecturestudio.core.app.configuration.JsonConfigurationService;

public class EditorConfigService extends JsonConfigurationService<EditorConfiguration> {

	@Override
	public void validate(EditorConfiguration config) {
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		config.getToolConfig().getPenSettings().setWidth(defaultConfig.getToolConfig().getPenSettings().getWidth());
		config.getToolConfig().getHighlighterSettings().setWidth(defaultConfig.getToolConfig().getHighlighterSettings().getWidth());
		config.getToolConfig().getPointerSettings().setWidth(defaultConfig.getToolConfig().getPointerSettings().getWidth());
		config.getToolConfig().getArrowSettings().setWidth(defaultConfig.getToolConfig().getArrowSettings().getWidth());
		config.getToolConfig().getLineSettings().setWidth(defaultConfig.getToolConfig().getLineSettings().getWidth());
		config.getToolConfig().getRectangleSettings().setWidth(defaultConfig.getToolConfig().getRectangleSettings().getWidth());
		config.getToolConfig().getEllipseSettings().setWidth(defaultConfig.getToolConfig().getEllipseSettings().getWidth());
		config.getToolConfig().getPresetColors().clear();
		config.getToolConfig().getPresetColors().addAll(defaultConfig.getToolConfig().getPresetColors());
	}
}
