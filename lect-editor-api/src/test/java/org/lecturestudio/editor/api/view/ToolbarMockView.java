package org.lecturestudio.editor.api.view;

import java.util.List;

import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.StrokeWidthSettings;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;

public class ToolbarMockView implements ToolbarView {
	public Document doc;
	public ConsumerAction<Color> setOnCustomPaletteColor;
	public Action setOnCustomColor;
	public Action setOnColor1;
	public Action setOnColor2;
	public Action setOnColor3;
	public Action setOnColor4;
	public Action setOnColor5;
	public Action setOnColor6;
	public Action setOnPenTool;
	public Action setOnHighlighterTool;
	public Action setOnPointerTool;
	public Action setOnTextSelectTool;
	public Action setOnLineTool;
	public Action setOnArrowTool;
	public Action setOnRectangleTool;
	public Action setOnEllipseTool;
	public Action setOnSelectTool;
	public Action setOnTextTool;
	public ConsumerAction<Font> setOnTextBoxFont;
	public Action setOnEraseTool;
	public Action setOnClearTool;
	public Action setOnZoomInTool;
	public Action setOnZoomOutTool;
	public Action setOnPanTool;
	public ToolType selectColorButtonToolType;
	public PaintSettings selectColorButtonPaintSettings;
	public ToolType selectToolButtonToolType;
	public StrokeWidthSettings selectStrokeWidthSettings;
	public ConsumerAction<StrokeWidthSettings> setOnStrokeWidthSettings;
	public List<StrokeWidthSettings> strokeWidthSettings;
	public PresentationParameter presentationParameter;
	public Page page;

	@Override
	public void setDocument(Document doc) {
		this.doc = doc;
	}

	@Override
	public void setOnCustomPaletteColor(ConsumerAction<Color> action) {
		this.setOnCustomPaletteColor = action;
	}

	@Override
	public void setOnCustomColor(Action action) {
		this.setOnCustomColor = action;
	}

	@Override
	public void setOnColor1(Action action) {
		this.setOnColor1 = action;
	}

	@Override
	public void setOnColor2(Action action) {
		this.setOnColor2 = action;
	}

	@Override
	public void setOnColor3(Action action) {
		this.setOnColor3 = action;
	}

	@Override
	public void setOnColor4(Action action) {
		this.setOnColor4 = action;
	}

	@Override
	public void setOnColor5(Action action) {
		this.setOnColor5 = action;
	}

	@Override
	public void setOnColor6(Action action) {
		this.setOnColor6 = action;
	}

	@Override
	public void setOnPenTool(Action action) {
		this.setOnPenTool = action;
	}

	@Override
	public void setOnHighlighterTool(Action action) {
		this.setOnHighlighterTool = action;
	}

	@Override
	public void setOnPointerTool(Action action) {
		this.setOnPointerTool = action;
	}

	@Override
	public void setOnTextSelectTool(Action action) {
		this.setOnTextSelectTool = action;
	}

	@Override
	public void setOnLineTool(Action action) {
		this.setOnLineTool = action;
	}

	@Override
	public void setOnArrowTool(Action action) {
		this.setOnArrowTool = action;
	}

	@Override
	public void setOnRectangleTool(Action action) {
		this.setOnRectangleTool = action;
	}

	@Override
	public void setOnEllipseTool(Action action) {
		this.setOnEllipseTool = action;
	}

	@Override
	public void setOnSelectTool(Action action) {
		this.setOnSelectTool = action;
	}

	@Override
	public void setOnEraseTool(Action action) {
		this.setOnEraseTool = action;
	}

	@Override
	public void setOnTextTool(Action action) {
		this.setOnTextTool = action;
	}

	@Override
	public void setOnTextBoxFont(ConsumerAction<Font> action) {
		this.setOnTextBoxFont = action;
	}

	@Override
	public void setOnClearTool(Action action) {
		this.setOnClearTool = action;
	}

	@Override
	public void setOnZoomInTool(Action action) {
		this.setOnZoomInTool = action;
	}

	@Override
	public void setOnZoomOutTool(Action action) {
		this.setOnZoomOutTool = action;
	}

	@Override
	public void setOnPanTool(Action action) {
		this.setOnPanTool = action;
	}

	@Override
	public void selectColorButton(ToolType toolType, PaintSettings settings) {
		this.selectColorButtonToolType = toolType;
		this.selectColorButtonPaintSettings = settings;
	}

	@Override
	public void selectToolButton(ToolType toolType) {
		this.selectToolButtonToolType = toolType;
	}

	@Override
	public void setStrokeSettings(List<StrokeWidthSettings> strokeSettings) {
		this.strokeWidthSettings = strokeSettings;
	}

	@Override
	public void selectStrokeWidthSettings(StrokeWidthSettings settings) {
		this.selectStrokeWidthSettings = settings;
	}

	@Override
	public void setOnStrokeWidthSettings(ConsumerAction<StrokeWidthSettings> action) {
		this.setOnStrokeWidthSettings = action;
	}

	@Override
	public void setPage(Page page, PresentationParameter parameter) {
		this.page = page;
		this.presentationParameter = parameter;
	}
}
