package org.lecturestudio.editor.javafx.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;

import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.tool.ColorPalette;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.StrokeWidthSettings;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.editor.api.view.ToolbarView;
import org.lecturestudio.javafx.beans.converter.ColorConverter;
import org.lecturestudio.javafx.beans.converter.FontConverter;
import org.lecturestudio.javafx.control.ColorPaletteButton;
import org.lecturestudio.javafx.control.ExtButton;
import org.lecturestudio.javafx.control.ExtToggleButton;
import org.lecturestudio.javafx.control.FontPickerButton;
import org.lecturestudio.javafx.control.ToolGroupButton;
import org.lecturestudio.javafx.factory.StrokeWidthCellFactory;
import org.lecturestudio.javafx.factory.StrokeWidthListCell;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "main-toolbar", presenter = org.lecturestudio.editor.api.presenter.ToolbarPresenter.class)
public class FxToolbarView extends FlowPane implements ToolbarView {

	private ConsumerAction<Color> paletteColorAction;
	private ConsumerAction<Font> textBoxFontAction;

	@FXML
	private ResourceBundle resources;

	@FXML
	private ToggleGroup colorToggleGroup;

	@FXML
	private ToggleGroup toolToggleGroup;

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
	private ExtButton clearButton;

	@FXML
	private ExtToggleButton zoomInButton;

	@FXML
	private ExtToggleButton panButton;

	@FXML
	private ExtButton zoomOutButton;
	@FXML
	private ComboBox<StrokeWidthSettings> strokeWidthComboBox;

	public FxToolbarView() {
		super();
	}

	@Override
	public void setDocument(Document doc) {
		boolean isWhiteboard = nonNull(doc) && doc.isWhiteboard();

		textSelectButton.setDisable(isWhiteboard);
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
	public void setOnClearTool(Action action) {
		FxUtils.bindAction(clearButton, action);
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

							if (toggle instanceof ToolGroupButton button) {
								button.selectToolType(toolType);
							}

							break;
						}
					}
				}
			}
		});
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

		// Prevent situation where no toggle is selected
		colorToggleGroup.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
			if (newVal == null) {
				oldVal.setSelected(true);
			}
		});
		textButton.textFontProperty().addListener((observable, oldFont, newFont) -> {
			executeAction(textBoxFontAction, FontConverter.INSTANCE.from(newFont));
		});

		strokeWidthComboBox.setCellFactory(new StrokeWidthCellFactory(resources));
		strokeWidthComboBox.setButtonCell(new StrokeWidthListCell(resources, false));
	}

	private void setButtonColor(ButtonBase button, Paint paint) {
		Labeled graphic = (Labeled) button.getGraphic();
		graphic.setBackground(new Background(new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY)));
	}

	@Override
	public void setStrokeSettings(List<StrokeWidthSettings> strokeSettings) {
		Platform.runLater(() -> {
			strokeWidthComboBox.getItems().clear();
			strokeWidthComboBox.getItems().addAll(FXCollections.observableList(strokeSettings));
		});
	}

	@Override
	public void selectStrokeWidthSettings(StrokeWidthSettings settings) {
		FxUtils.invoke(() -> {
			if (settings == null) {
				strokeWidthComboBox.setDisable(true);
			}
			else {
				strokeWidthComboBox.setDisable(false);
				strokeWidthComboBox.getSelectionModel().select(settings);
			}
		});
	}

	@Override
	public void setOnStrokeWidthSettings(ConsumerAction<StrokeWidthSettings> action) {
		strokeWidthComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			executeAction(action, newValue);
		});
	}

}