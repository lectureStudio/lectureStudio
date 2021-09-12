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

package org.lecturestudio.presenter.javafx.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Labeled;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.tool.ColorPalette;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.javafx.beans.converter.ColorConverter;
import org.lecturestudio.javafx.beans.converter.FontConverter;
import org.lecturestudio.javafx.control.ColorPaletteButton;
import org.lecturestudio.javafx.control.ExtButton;
import org.lecturestudio.javafx.control.ExtToggleButton;
import org.lecturestudio.javafx.control.FontPickerButton;
import org.lecturestudio.javafx.control.TeXFontPickerButton;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.ToolbarView;
import org.lecturestudio.presenter.javafx.control.RecordButton;
import org.lecturestudio.presenter.javafx.control.ToolGroupButton;

@FxmlView(name = "main-toolbar", presenter = org.lecturestudio.presenter.api.presenter.ToolbarPresenter.class)
public class FxToolbarView extends FlowPane implements ToolbarView {

	private ConsumerAction<Color> paletteColorAction;

	private ConsumerAction<Font> textBoxFontAction;

	private ConsumerAction<TeXFont> texBoxFontAction;

	@FXML
	private ToggleGroup colorToggleGroup;

	@FXML
	private ToggleGroup toolToggleGroup;

	@FXML
	private ExtButton undoButton;

	@FXML
	private ExtButton redoButton;

	@FXML
	private ColorPaletteButton customColorButton;

	@FXML
	private ExtToggleButton colorButton1;

	@FXML
	private ExtToggleButton colorButton2;

	@FXML
	private ExtToggleButton colorButton3;

	@FXML
	private ExtToggleButton colorButton4;

	@FXML
	private ExtToggleButton colorButton5;

	@FXML
	private ExtToggleButton colorButton6;

	@FXML
	private ExtToggleButton penButton;

	@FXML
	private ExtToggleButton highlighterButton;

	@FXML
	private ExtToggleButton pointerButton;

	@FXML
	private ExtToggleButton textSelectButton;

	@FXML
	private ExtToggleButton lineButton;

	@FXML
	private ExtToggleButton arrowButton;

	@FXML
	private ExtToggleButton rectangleButton;

	@FXML
	private ExtToggleButton ellipseButton;

	@FXML
	private ToolGroupButton selectButton;

	@FXML
	private ExtToggleButton eraseButton;

	@FXML
	private FontPickerButton textButton;

	@FXML
	private TeXFontPickerButton texButton;

	@FXML
	private ExtButton clearButton;

	@FXML
	private ExtToggleButton gridButton;

	@FXML
	private ExtToggleButton extendButton;

	@FXML
	private ExtToggleButton whiteboardButton;

	@FXML
	private ExtToggleButton displaysButton;

	@FXML
	private ExtToggleButton zoomInButton;

	@FXML
	private ExtToggleButton panButton;

	@FXML
	private ExtButton zoomOutButton;

	@FXML
	private RecordButton startRecordingButton;

	@FXML
	private ExtButton stopRecordingButton;


	public FxToolbarView() {
		super();
	}

	@Override
	public void setDocument(Document doc) {
		boolean isWhiteboard = nonNull(doc) && doc.isWhiteboard();

		textSelectButton.setDisable(isWhiteboard);
		whiteboardButton.setSelected(isWhiteboard);
	}

	@Override
	public void setPage(Page page, PresentationParameter parameter) {
		boolean hasUndo = false;
		boolean hasRedo = false;
		boolean extended = false;
		boolean hasGrid = false;
		boolean zoomedIn = false;

		if (nonNull(page)) {
			hasUndo = page.hasUndoActions();
			hasRedo = page.hasRedoActions();
		}
		if (nonNull(parameter)) {
			extended = parameter.isExtended();
			hasGrid = parameter.showGrid();
			zoomedIn = parameter.isZoomMode();
		}

		undoButton.setDisable(!hasUndo);
		redoButton.setDisable(!hasRedo);
		extendButton.setSelected(extended);
		gridButton.setSelected(hasGrid);
		selectButton.setDisable(!hasUndo);
		eraseButton.setDisable(!hasUndo);
		panButton.setDisable(!zoomedIn);
		zoomOutButton.setDisable(!zoomedIn);
	}

	@Override
	public void setScreensAvailable(boolean screensAvailable) {
		FxUtils.invoke(() -> {
			displaysButton.setDisable(!screensAvailable);
		});
	}

	@Override
	public void setPresentationViewsVisible(boolean viewsVisible) {
		FxUtils.invoke(() -> {
			displaysButton.setSelected(viewsVisible);
		});
	}

	@Override
	public void setRecordingState(ExecutableState state) {
		FxUtils.invoke(() -> {
			boolean started = state == ExecutableState.Started;
			boolean stopped = state == ExecutableState.Stopped;

			startRecordingButton.setRecording(started);
			stopRecordingButton.setDisable(stopped);
		});
	}

	@Override
	public void setStreamingState(ExecutableState state) {

	}

	@Override
	public void showRecordNotification(boolean show) {
		FxUtils.invoke(() -> {
			startRecordingButton.setBlink(show);
		});
	}

	@Override
	public void setOnUndo(Action action) {
		FxUtils.bindAction(undoButton, action);
	}

	@Override
	public void setOnRedo(Action action) {
		FxUtils.bindAction(redoButton, action);
	}

	@Override
	public void setOnCustomPaletteColor(ConsumerAction<Color> action) {
		this.paletteColorAction = action;
	}

	@Override
	public void setOnCustomColor(Action action) {
		FxUtils.bindAction(customColorButton, action);
	}

	@Override
	public void setOnColor1(Action action) {
		FxUtils.bindAction(colorButton1, action);
	}

	@Override
	public void setOnColor2(Action action) {
		FxUtils.bindAction(colorButton2, action);
	}

	@Override
	public void setOnColor3(Action action) {
		FxUtils.bindAction(colorButton3, action);
	}

	@Override
	public void setOnColor4(Action action) {
		FxUtils.bindAction(colorButton4, action);
	}

	@Override
	public void setOnColor5(Action action) {
		FxUtils.bindAction(colorButton5, action);
	}

	@Override
	public void setOnColor6(Action action) {
		FxUtils.bindAction(colorButton6, action);
	}

	@Override
	public void setOnPenTool(Action action) {
		FxUtils.bindAction(penButton, action);
	}

	@Override
	public void setOnHighlighterTool(Action action) {
		FxUtils.bindAction(highlighterButton, action);
	}

	@Override
	public void setOnPointerTool(Action action) {
		FxUtils.bindAction(pointerButton, action);
	}

	@Override
	public void setOnTextSelectTool(Action action) {
		FxUtils.bindAction(textSelectButton, action);
	}

	@Override
	public void setOnLineTool(Action action) {
		FxUtils.bindAction(lineButton, action);
	}

	@Override
	public void setOnArrowTool(Action action) {
		FxUtils.bindAction(arrowButton, action);
	}

	@Override
	public void setOnRectangleTool(Action action) {
		FxUtils.bindAction(rectangleButton, action);
	}

	@Override
	public void setOnEllipseTool(Action action) {
		FxUtils.bindAction(ellipseButton, action);
	}

	@Override
	public void setOnSelectTool(Action action) {
		FxUtils.bindAction(selectButton, action);
	}

	@Override
	public void setOnEraseTool(Action action) {
		FxUtils.bindAction(eraseButton, action);
	}

	@Override
	public void setOnTextTool(Action action) {
		FxUtils.bindAction(textButton, action);
	}

	@Override
	public void setOnTextBoxFont(ConsumerAction<Font> action) {
		this.textBoxFontAction = action;
	}

	@Override
	public void setOnTeXTool(Action action) {
		FxUtils.bindAction(texButton, action);
	}

	@Override
	public void setOnClearTool(Action action) {
		FxUtils.bindAction(clearButton, action);
	}

	@Override
	public void setOnShowGrid(Action action) {
		FxUtils.bindAction(gridButton, action);
	}

	@Override
	public void setOnExtend(Action action) {
		FxUtils.bindAction(extendButton, action);
	}

	@Override
	public void setOnWhiteboard(Action action) {
		FxUtils.bindAction(whiteboardButton, action);
	}

	@Override
	public void setOnEnableDisplays(ConsumerAction<Boolean> action) {
		FxUtils.bindAction(displaysButton, action);
	}

	@Override
	public void setOnZoomInTool(Action action) {
		FxUtils.bindAction(zoomInButton, action);
	}

	@Override
	public void setOnZoomOutTool(Action action) {
		FxUtils.bindAction(zoomOutButton, action);
	}

	@Override
	public void setOnPanTool(Action action) {
		FxUtils.bindAction(panButton, action);
	}

	@Override
	public void setOnStartRecording(Action action) {
		FxUtils.bindAction(startRecordingButton, action);
	}

	@Override
	public void setOnStopRecording(Action action) {
		FxUtils.bindAction(stopRecordingButton, action);
	}

	@Override
	public void selectColorButton(ToolType toolType, PaintSettings settings) {
		FxUtils.invoke(() -> {
			ObservableList<Toggle> toggles = colorToggleGroup.getToggles();

			for (int i = 0; i < toggles.size(); i++) {
				Toggle button = toggles.get(i);
				Color color = ColorPalette.getColor(toolType, i);

				if (isNull(color)) {
					throw new IllegalArgumentException("No color assigned to the color-button.");
				}

				// Select button with assigned brush color.
				if (nonNull(settings) && color.equals(settings.getColor())) {
					colorToggleGroup.selectToggle(button);
				}

				setButtonColor((ButtonBase) button, ColorConverter.INSTANCE.to(color));
			}
		});
	}

	@Override
	public void selectToolButton(ToolType toolType) {
		FxUtils.invoke(() -> {
			ObservableList<Toggle> toolToggles = toolToggleGroup.getToggles();

			for (Toggle toggle : toolToggles) {
				Object userData = toggle.getUserData();

				if (isNull(userData)) {
					continue;
				}

				// Mapping may contain multiple type entries.
				String[] types = userData.toString().split(",");

				if (types.length == 1) {
					ToolType buttonType = ToolType.valueOf(types[0].trim());

					if (toolType == buttonType) {
						toolToggleGroup.selectToggle(toggle);
						break;
					}
				}
				else {
					// Handle multiple type entries.
					for (String type : types) {
						ToolType buttonType = ToolType.valueOf(type.trim());

						if (toolType == buttonType) {
							toolToggleGroup.selectToggle(toggle);

							if (toggle instanceof ToolGroupButton) {
								ToolGroupButton button = (ToolGroupButton) toggle;
								button.selectToolType(toolType);
							}

							break;
						}
					}
				}
			}
		});
	}

	@Override
	public void bindEnableStream(BooleanProperty enable) {

	}

	@Override
	public void bindEnableStreamMicrophone(BooleanProperty enable) {

	}

	@Override
	public void bindEnableStreamCamera(BooleanProperty enable) {

	}

	@FXML
	private void initialize() {
		// Disable button focus.
		for (Node node : getChildren()) {
			node.setFocusTraversable(false);
		}

		customColorButton.colorProperty().addListener((observable, oldColor, newColor) -> {
			setButtonColor(customColorButton, newColor);

			executeAction(paletteColorAction, ColorConverter.INSTANCE.from(newColor));
		});
		textButton.textFontProperty().addListener((observable, oldFont, newFont) -> {
			executeAction(textBoxFontAction, FontConverter.INSTANCE.from(newFont));
		});
		texButton.texFontProperty().addListener((observable, oldFont, newFont) -> {
			executeAction(texBoxFontAction, newFont);
		});
	}

	private void setButtonColor(ButtonBase button, Paint paint) {
		Labeled graphic = (Labeled) button.getGraphic();
		graphic.setBackground(new Background(new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY)));
	}

}
