package org.lecturestudio.editor.api.presenter;

import javax.inject.Inject;

import java.util.List;

import com.google.common.eventbus.Subscribe;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.bus.event.TextFontEvent;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.tool.StrokeWidthSettings;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.tool.ColorPalette;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.StrokeSettings;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.editor.api.bus.event.EditorToolSelectionEvent;
import org.lecturestudio.editor.api.controller.EditorToolController;
import org.lecturestudio.editor.api.view.ToolbarView;

public class ToolbarPresenter extends Presenter<ToolbarView> {
	private final EventBus eventBus;
	private final EditorToolController toolController;
	private ToolType toolType;

	@Inject
	protected ToolbarPresenter(ApplicationContext context,
	                           ToolbarView view,
	                           EditorToolController toolController) {
		super(context, view);

		this.eventBus = context.getEventBus();
		this.toolController = toolController;
	}

	@Subscribe
	public void onEvent(final EditorToolSelectionEvent event) {
		toolChanged(event.getToolType(), event.getPaintSettings());
	}

	private void toolChanged(ToolType toolType, PaintSettings settings) {
		this.toolType = toolType;

		// Update selected tool button.
		view.selectToolButton(toolType);

		if (ColorPalette.hasPalette(toolType)) {
			// Update color palette for the selected tool.
			view.selectColorButton(toolType, settings);
		}

		if (settings instanceof StrokeSettings strokeSettings) {
			view.selectStrokeWidthSettings(strokeSettings.getStrokeWidthSettings());
		}
		else {
			view.selectStrokeWidthSettings(null);
		}
	}

	public void customPaletteColor(Color color) {
		ColorPalette.setColor(toolType, color, 0);

		toolController.selectPaintColor(color);
	}

	public void customColor() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 0));
	}

	public void color1() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 1));
	}

	public void color2() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 2));
	}

	public void color3() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 3));
	}

	public void color4() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 4));
	}

	public void color5() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 5));
	}

	public void color6() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 6));
	}

	public void penTool() {
		toolController.selectPenTool();
	}

	public void highlighterTool() {
		toolController.selectHighlighterTool();
	}

	public void pointerTool() {
		toolController.selectPointerTool();
	}

	public void textSelectTool() {
		toolController.selectTextSelectionTool();
	}

	public void lineTool() {
		toolController.selectLineTool();
	}

	public void arrowTool() {
		toolController.selectArrowTool();
	}

	public void rectangleTool() {
		toolController.selectRectangleTool();
	}

	public void ellipseTool() {
		toolController.selectEllipseTool();
	}

	public void selectTool() {
		toolController.selectSelectTool();
	}

	public void eraseTool() {
		toolController.selectRubberTool();
	}

	public void textTool() {
		toolController.selectTextTool();
	}

	public void setTextBoxFont(Font font) {
		toolController.setTextFont(font);

		eventBus.post(new TextFontEvent(font));
	}

	public void clearTool() {
		toolController.selectDeleteAllTool();
	}

	public void zoomInTool() {
		toolController.selectZoomTool();
	}

	public void zoomOutTool() {
		toolController.selectZoomOutTool();
	}

	public void panTool() {
		try {
			toolController.selectPanningTool();
		}
		catch (Exception e) {
			handleException(e, "Select pan tool failed", e.getMessage());
		}
	}

	private void strokeWidthSettings(StrokeWidthSettings selectedStrokeWidthSettings) {
		toolController.selectStrokeWidthSettings(selectedStrokeWidthSettings);
	}

	@Override
	public void initialize() {
		eventBus.register(this);

		view.setOnCustomPaletteColor(this::customPaletteColor);
		view.setOnCustomColor(this::customColor);
		view.setOnColor1(this::color1);
		view.setOnColor2(this::color2);
		view.setOnColor3(this::color3);
		view.setOnColor4(this::color4);
		view.setOnColor5(this::color5);
		view.setOnColor6(this::color6);

		view.setOnPenTool(this::penTool);
		view.setOnHighlighterTool(this::highlighterTool);
		view.setOnPointerTool(this::pointerTool);
		view.setOnTextSelectTool(this::textSelectTool);
		view.setOnLineTool(this::lineTool);
		view.setOnArrowTool(this::arrowTool);
		view.setOnRectangleTool(this::rectangleTool);
		view.setOnEllipseTool(this::ellipseTool);
		view.setOnSelectTool(this::selectTool);
		view.setOnEraseTool(this::eraseTool);
		view.setOnTextTool(this::textTool);
		view.setOnTextBoxFont(this::setTextBoxFont);
		view.setOnClearTool(this::clearTool);

		view.setOnZoomInTool(this::zoomInTool);
		view.setOnZoomOutTool(this::zoomOutTool);
		view.setOnPanTool(this::panTool);

		view.setOnStrokeWidthSettings(this::strokeWidthSettings);
		view.setStrokeSettings(List.of(StrokeWidthSettings.values()));
		view.selectStrokeWidthSettings(StrokeWidthSettings.NORMAL);

		penTool();
	}

}
