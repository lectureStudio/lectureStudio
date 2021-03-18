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
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Spinner;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class FontPickerSkin extends SkinBase<FontPicker> {

	private static final String PREVIEW_TEXT = "AaBbYyZz 012345";
	
	private static final int MIN_FONT_SIZE = 1;
	private static final int MAX_FONT_SIZE = 100;
	
	private final ChangeListener<Font> fontListener = (observable, oldFont, newFont) -> {
		onFontChange(newFont);
	};
	
	private Label previewLabel;
	private Text previewText;
	
	private ComboBox<String> fontComboBox;
	
	private Spinner<Integer> sizeSpinner;
	
	private CheckBox boldCheckBox;
	private CheckBox italicCheckBox;


	protected FontPickerSkin(FontPicker control, ResourceBundle resourceBundle) {
		super(control);

		initLayout(control, resourceBundle);
	}
	
	/** {@inheritDoc} */
	@Override
	public void dispose() {
		FontPicker control = getSkinnable();
		control.fontProperty().removeListener(fontListener);

		super.dispose();
	}

	private void initLayout(FontPicker control, ResourceBundle resourceBundle) {
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
				new RowConstraints(), new RowConstraints(), new RowConstraints());

		previewLabel = new Label(PREVIEW_TEXT);
		previewLabel.getStyleClass().add("preview-text");

		StackPane previewPane = new StackPane(previewLabel);
		previewPane.setMinWidth(300);
		previewPane.setMaxWidth(300);
		previewPane.setPrefWidth(300);
		previewPane.setMinHeight(80);
		previewPane.setPrefHeight(80);
		previewPane.setMaxHeight(80);

		List<String> families = Font.getFamilies();
		
		fontComboBox = new ComboBox<>(FXCollections.observableList(families));
		fontComboBox.setMaxWidth(Double.MAX_VALUE);
		fontComboBox.getSelectionModel().select(0);
		fontComboBox.setCellFactory((ListView<String> listView) -> new ListCell<>() {

			@Override
			public void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);

				if (nonNull(item)) {
					setText(item);
					setFont(new Font(item, 12));
				}
			}
		});

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

		boldCheckBox = new CheckBox(resourceBundle.getString("font.bold"));
		italicCheckBox = new CheckBox(resourceBundle.getString("font.italic"));
		
		HBox styleHBox = new HBox(10);
		styleHBox.setAlignment(Pos.CENTER_LEFT);
		styleHBox.getChildren().addAll(boldCheckBox, italicCheckBox);

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
		container.add(new Label(resourceBundle.getString("font.style")), 0, 4, 1, 1);
		container.add(styleHBox, 1, 4, 1, 1);
		container.add(buttonsHBox, 0, 5, 2, 1);
		
		// Set current font.
		onFontChange(control.getFont());
		
		control.fontProperty().addListener(fontListener);

		// Bind UI listeners.
		fontComboBox.valueProperty().addListener(observable -> changeFont());
		sizeSpinner.valueProperty().addListener(observable -> changeFont());
		boldCheckBox.selectedProperty().addListener(observable -> changeFont());
		italicCheckBox.selectedProperty().addListener(observable -> changeFont());

		// Bind button's actions.
		okButton.onActionProperty().bind(control.okActionProperty());
		cancelButton.onActionProperty().bind(control.cancelActionProperty());

		getChildren().add(container);
	}
	
	private void onFontChange(Font font) {
		if (isNull(font)) {
			return;
		}
		
		previewLabel.setFont(font);
		
		fontComboBox.setValue(font.getFamily());
		
		sizeSpinner.getValueFactory().setValue(Double.valueOf(font.getSize()).intValue());
		
		boldCheckBox.setSelected(getWeight(font) == FontWeight.BOLD);
		italicCheckBox.setSelected(getPosture(font) == FontPosture.ITALIC);
	}

	private void changeFont() {
		if (isNull(previewText)) {
			previewText = (Text) previewLabel.lookup(".text");
		}
		
		FontWeight weight = boldCheckBox.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
		FontPosture posture = italicCheckBox.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;

		int size = sizeSpinner.getValue();
		String family = fontComboBox.getValue();
		Font font = Font.font(family, weight, posture, size);

		previewLabel.setFont(font);
		
		if (!font.equals(getSkinnable().getFont())) {
			getSkinnable().setFont(font);
		}
	}
	
	private static FontPosture getPosture(Font font) {
		String[] styles = font.getStyle().split(" ");
		
		for (String style : styles) {
			FontPosture posture = FontPosture.findByName(style);
			
			if (nonNull(posture)) {
				return posture;
			}
		}
		
		return FontPosture.REGULAR;
	}
	
	private static FontWeight getWeight(Font font) {
		String[] styles = font.getStyle().split(" ");
		
		for (String style : styles) {
			FontWeight weight = FontWeight.findByName(style);
			
			if (nonNull(weight)) {
				return weight;
			}
		}
		
		return FontWeight.NORMAL;
	}

}
