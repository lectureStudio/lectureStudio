package org.lecturestudio.editor.api.view;

import java.util.List;

import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.tool.StrokeWidthSettings;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;

public interface ToolbarView extends View {
	void setDocument(Document doc);

	void setOnCustomPaletteColor(ConsumerAction<Color> action);

	void setOnCustomColor(Action action);

	void setOnColor1(Action action);

	void setOnColor2(Action action);

	void setOnColor3(Action action);

	void setOnColor4(Action action);

	void setOnColor5(Action action);

	void setOnColor6(Action action);

	void setOnPenTool(Action action);

	void setOnHighlighterTool(Action action);

	void setOnPointerTool(Action action);

	void setOnTextSelectTool(Action action);

	void setOnLineTool(Action action);

	void setOnArrowTool(Action action);

	void setOnRectangleTool(Action action);

	void setOnEllipseTool(Action action);

	void setOnSelectTool(Action action);

	void setOnEraseTool(Action action);

	void setOnClearTool(Action action);

	void setOnZoomInTool(Action action);

	void setOnZoomOutTool(Action action);

	void setOnPanTool(Action action);

	void selectColorButton(ToolType toolType, PaintSettings settings);

	void selectToolButton(ToolType toolType);

	void setStrokeSettings(List<StrokeWidthSettings> strokeSettings);

	void selectStrokeWidthSettings(StrokeWidthSettings settings);

	void setOnStrokeWidthSettings(ConsumerAction<StrokeWidthSettings> action);
}
