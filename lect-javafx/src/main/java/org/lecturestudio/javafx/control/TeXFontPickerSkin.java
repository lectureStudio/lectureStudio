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

package org.lecturestudio.javafx.control;

import static java.util.Objects.isNull;

import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Spinner;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.javafx.factory.TeXFontCellFactory;
import org.lecturestudio.javafx.factory.TeXFontListCell;

import org.jfree.fx.FXGraphics2D;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class TeXFontPickerSkin extends SkinBase<TeXFontPicker> {

	private static final String PREVIEW_TEXT = "\\int_a^b{f(x)\\,dx} = \\sum\\limits_{n = 1}^\\infty f(2^{-n} \\left( {b - a} \\right))";

	private static final int MIN_FONT_SIZE = 1;
	private static final int MAX_FONT_SIZE = 100;

	private final ChangeListener<TeXFont> fontListener = (observable, oldFont, newFont) -> {
		onControlFont(newFont);
	};

	private Canvas previewCanvas;

	private ComboBox<TeXFont.Type> fontComboBox;

	private Spinner<Integer> sizeSpinner;


	protected TeXFontPickerSkin(TeXFontPicker control, ResourceBundle resourceBundle) {
		super(control);

		initLayout(control, resourceBundle);
	}
	
	/** {@inheritDoc} */
	@Override
	public void dispose() {
		TeXFontPicker control = getSkinnable();
		control.fontProperty().removeListener(fontListener);

		super.dispose();
	}

	private void initLayout(TeXFontPicker control, ResourceBundle resourceBundle) {
		GridPane container = new GridPane();
		container.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		container.setHgap(20);
		container.setVgap(10);

		ColumnConstraints column1 = new ColumnConstraints();
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setFillWidth(true);
		
		container.getColumnConstraints().addAll(column1, column2);

		container.getRowConstraints().addAll(
				new RowConstraints(), new RowConstraints(), new RowConstraints(),
				new RowConstraints(), new RowConstraints());

		previewCanvas = new Canvas();
		previewCanvas.setWidth(300);
		previewCanvas.setHeight(80);

		StackPane previewPane = new StackPane(previewCanvas);
		previewPane.getStyleClass().add("preview-text");

		fontComboBox = new ComboBox<>(FXCollections.observableList(Arrays.asList(TeXFont.Type.values())));
		fontComboBox.setMaxWidth(Double.MAX_VALUE);
		fontComboBox.getSelectionModel().select(0);
		fontComboBox.setButtonCell(new TeXFontListCell(resourceBundle));
		fontComboBox.setCellFactory(new TeXFontCellFactory(resourceBundle));

		StringConverter<Integer> spinnerFormatter = new StringConverter<>() {

			@Override
			public Integer fromString(String val) {
				String oldVal = String.valueOf(sizeSpinner.getValue());
				try {
					int result = Integer.parseInt(val);
					if (result >= MIN_FONT_SIZE && result <= MAX_FONT_SIZE) {
						return result;
					}
					throw new Exception("");
				}
				catch (Exception e) {
					sizeSpinner.getEditor().setText(oldVal);
					return sizeSpinner.getValue();
				}
			}

			@Override
			public String toString(Integer val) {
				return val.toString();
			}
		};
		
		sizeSpinner = new Spinner<>(MIN_FONT_SIZE, MAX_FONT_SIZE, 12);
		sizeSpinner.getValueFactory().setConverter(spinnerFormatter);
		sizeSpinner.setEditable(true);
		sizeSpinner.setPrefWidth(70);

		Button okButton = new Button(resourceBundle.getString("button.ok"));
		okButton.setOnAction(control.getOkAction());

		Button cancelButton = new Button(resourceBundle.getString("button.cancel"));
		cancelButton.setOnAction(control.getCancelAction());


		HBox buttonsHBox = new HBox(5);
		buttonsHBox.setAlignment(Pos.CENTER_RIGHT);
		buttonsHBox.getChildren().addAll(cancelButton, okButton);

		container.add(new Label(resourceBundle.getString("font.preview")), 0, 0, 2, 1);
		container.add(previewPane, 0, 1, 2, 1);
		container.add(new Label(resourceBundle.getString("font")), 0, 2, 1, 1);
		container.add(fontComboBox, 1, 2, 1, 1);
		container.add(new Label(resourceBundle.getString("font.size")), 0, 3, 1, 1);
		container.add(sizeSpinner, 1, 3, 1, 1);
		container.add(buttonsHBox, 0, 4, 2, 1);
		
		// Set current font.
		onControlFont(control.getFont());
		
		control.fontProperty().addListener(fontListener);

		// Bind UI listeners.
		fontComboBox.valueProperty().addListener(observable -> onFontChange());
		sizeSpinner.valueProperty().addListener(observable -> onFontChange());

		// Bind button's actions.
		okButton.onActionProperty().bind(control.okActionProperty());
		cancelButton.onActionProperty().bind(control.cancelActionProperty());

		getChildren().add(container);
	}
	
	private void onControlFont(TeXFont font) {
		if (isNull(font)) {
			return;
		}

		updatePreview(font);
		
		fontComboBox.setValue(font.getType());
		sizeSpinner.getValueFactory().setValue(Double.valueOf(font.getSize()).intValue());
	}

	private void onFontChange() {
		int size = sizeSpinner.getValue();
		TeXFont.Type type = fontComboBox.getValue();
		TeXFont font = new TeXFont(type, size);

		getSkinnable().setFont(font);

		updatePreview(font);
	}

	private void updatePreview(TeXFont font) {
		GraphicsContext context = previewCanvas.getGraphicsContext2D();
		context.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
		context.save();

		FXGraphics2D graphics = new FXGraphics2D(context);

		TeXFormula formula = new TeXFormula(PREVIEW_TEXT);
		TeXFormula.TeXIconBuilder builder = formula.new TeXIconBuilder()
				.setStyle(TeXConstants.STYLE_DISPLAY)
				.setSize(font.getSize())
				.setType(font.getType().getValue());

		TeXIcon icon = builder.build();

		int x = (int) ((previewCanvas.getWidth() - icon.getIconWidth()) / 2);
		int y = (int) ((previewCanvas.getHeight() - icon.getIconHeight()) / 2);

		icon.paintIcon(null, graphics, x, y);

		context.restore();
	}

}
