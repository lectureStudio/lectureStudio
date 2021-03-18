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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class ColorPaletteSkin extends SkinBase<ColorPalette> {

	private static final int SQUARE_SIZE = 15;
	
	private final ChangeListener<Color> colorListener = (observable, oldColor, newColor) -> {
		updateSelection(newColor);
	};

	private final ListChangeListener<Color> colorListListener = change -> {
		buildCustomColors();
	};
	
	private ColorPickerGrid colorPickerGrid;

	private Hyperlink customColorLink;
	
	private Label customColorLabel;

	private GridPane customColorGrid;

	private ColorSquare focusedSquare;

	private boolean dragDetected;

	private ColorSquare hoverSquare;
	
	
	protected ColorPaletteSkin(ColorPalette control, ResourceBundle resourceBundle) {
		super(control);

		initLayout(control, resourceBundle);
	}
	
	/** {@inheritDoc} */
	@Override
	public void dispose() {
		ColorPalette control = getSkinnable();
		control.colorProperty().removeListener(colorListener);
		control.getCustomColors().removeListener(colorListListener);

		super.dispose();
	}
	
	private void initLayout(ColorPalette control, ResourceBundle resourceBundle) {
		Pane container = new Pane();
		
		colorPickerGrid = new ColorPickerGrid();
		colorPickerGrid.getStyleClass().setAll("color-palette-grid");
		
		customColorGrid = new GridPane();
		customColorGrid.getStyleClass().setAll("color-palette-grid");
		customColorGrid.setVisible(false);
		
		customColorLabel = new Label(resourceBundle.getString("color.custom.label"));
		customColorLabel.setAlignment(Pos.CENTER_LEFT);
		
		customColorLink = new Hyperlink(resourceBundle.getString("color.custom.link"));
		customColorLink.setPrefWidth(colorPickerGrid.prefWidth(-1));
		customColorLink.setAlignment(Pos.CENTER);
		customColorLink.setFocusTraversable(true);
		customColorLink.setVisited(true);
		customColorLink.setOnAction(t -> {
//				if (customColorDialog == null) {
//					customColorDialog = new CustomColorDialog(popupControl);
//					customColorDialog.customColorProperty().addListener((ov, t1, t2) -> {
//						colorPicker.setValue(customColorDialog.customColorProperty().get());
//					});
//					customColorDialog.setOnSave(() -> {
//						Color customColor = customColorDialog.customColorProperty().get();
//						buildCustomColors();
//						colorPicker.getCustomColors().add(customColor);
//						updateSelection(customColor);
//					});
//					customColorDialog.setOnUse(() -> {
//						Event.fireEvent(colorPicker, new ActionEvent());
//						colorPicker.hide();
//					});
//				}
		});

		hoverSquare = new ColorSquare();
		hoverSquare.getStyleClass().addAll("hover-square");
		hoverSquare.setMouseTransparent(true);

		buildCustomColors();

		Separator separator = new Separator();

		VBox paletteBox = new VBox();
		paletteBox.getStyleClass().setAll("color-palette-pane");
		paletteBox.getChildren().addAll(colorPickerGrid, customColorLabel, customColorGrid /*, separator, customColorLink*/);

		container.getChildren().addAll(paletteBox, hoverSquare);

		getChildren().add(container);

		// Set current color.
		updateSelection(control.getColor());

		// Attach listeners.
		control.colorProperty().addListener(colorListener);
		control.getCustomColors().addListener(colorListListener);
	}

	private void setFocusedSquare(ColorSquare square) {
		focusedSquare = square;
		
		hoverSquare.setVisible(nonNull(focusedSquare));
		
		if (isNull(focusedSquare)) {
			return;
		}
		if (!focusedSquare.isFocused()) {
			focusedSquare.requestFocus();
		}
		
		hoverSquare.setColor(focusedSquare.getColor());

		Bounds b = square.localToScene(square.getLayoutBounds());

		double x = b.getMinX();
		double y = b.getMinY();
		double xAdjust = snapPositionX(((hoverSquare.getWidth() + 2) * hoverSquare.getScaleX()) / 2.0);
		double yAdjust = snapPositionY(((hoverSquare.getHeight() - 4) * hoverSquare.getScaleY()) / 2.0);
		
		hoverSquare.setLayoutX(snapPositionX(x - xAdjust));
		hoverSquare.setLayoutY(snapPositionY(y - yAdjust));
	}

	private void updateSelection(Color color) {
		setFocusedSquare(null);

		// Check predefined colors.
		for (ColorSquare square : colorPickerGrid.getSquares()) {
			if (square.getColor().equals(color)) {
				setFocusedSquare(square);
				return;
			}
		}

		// Check custom colors.
		for (Node node : customColorGrid.getChildren()) {
			ColorSquare square = (ColorSquare) node;

			if (square.getColor().equals(color)) {
				setFocusedSquare(square);
				return;
			}
		}
	}

	private void buildCustomColors() {
		ObservableList<Color> customColors = getSkinnable().getCustomColors();
		
		customColorGrid.getChildren().clear();
		
		if (customColors.isEmpty()) {
			customColorLabel.setVisible(false);
			customColorLabel.setManaged(false);
			customColorGrid.setVisible(false);
			customColorGrid.setManaged(false);
			return;
		}
		else {
			customColorLabel.setVisible(true);
			customColorLabel.setManaged(true);
			customColorGrid.setVisible(true);
			customColorGrid.setManaged(true);
		}

		int customColumnIndex = 0;
		int customRowIndex = 0;
		int remainingSquares = customColors.size() % NUM_OF_COLUMNS;
		int numEmpty = (remainingSquares == 0) ? 0 : NUM_OF_COLUMNS - remainingSquares;

		for (int i = 0; i < customColors.size(); i++) {
			Color c = customColors.get(i);
			ColorSquare square = new ColorSquare(c);
			
			square.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
				if (e.getCode() == KeyCode.DELETE) {
					customColors.remove(square.getColor());
					
					buildCustomColors();
				}
			});
			
			customColorGrid.add(square, customColumnIndex, customRowIndex);
			
			customColumnIndex++;
			
			if (customColumnIndex == NUM_OF_COLUMNS) {
				customColumnIndex = 0;
				customRowIndex++;
			}
		}
		
		for (int i = 0; i < numEmpty; i++) {
			ColorSquare emptySquare = new ColorSquare();
			customColorGrid.add(emptySquare, customColumnIndex, customRowIndex);
			customColumnIndex++;
		}
	}



	private class ColorSquare extends StackPane {

		private Rectangle rectangle;
		private boolean isEmpty;

		
		ColorSquare() {
			this(null);
		}

		ColorSquare(Color color) {
			// Add style class to handle selected color square.
			getStyleClass().setAll("color-grid-square");
			
			if (nonNull(color)) {
				setFocusTraversable(true);

				focusedProperty().addListener((s, ov, nv) -> {
					setFocusedSquare(nv ? this : null);
				});

				addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
					setFocusedSquare(ColorSquare.this);
				});
				addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
					setFocusedSquare(null);
				});

				addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
					if (!dragDetected && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
						if (!isEmpty) {
							Color fill = getColor();
							
							getSkinnable().setColor(fill);
							updateSelection(fill);
							
							event.consume();
						}
					}
				});
			}
			else {
				color = Color.WHITE;
				isEmpty = true;
			}
			
			rectangle = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
			rectangle.setFill(color);
			rectangle.setStrokeType(StrokeType.INSIDE);

			getChildren().add(rectangle);
		}

		public Color getColor() {
			return (Color) rectangle.getFill();
		}

		public void setColor(Color color) {
			rectangle.setFill(color);
		}
	}



	private class ColorPickerGrid extends GridPane {

		private final List<ColorSquare> squares;
		private Color mouseDragColor;

		
		ColorPickerGrid() {
			int columnIndex = 0;
			int rowIndex = 0;
			
			squares = FXCollections.observableArrayList();
			
			final int numColors = RAW_VALUES.length / 3;
			Color[] colors = new Color[numColors];
			
			for (int i = 0; i < numColors; i++) {
				colors[i] = new Color(RAW_VALUES[(i * 3)] / 255, RAW_VALUES[(i * 3) + 1] / 255, RAW_VALUES[(i * 3) + 2] / 255, 1.0);
				
				squares.add(new ColorSquare(colors[i]));
			}

			for (ColorSquare square : squares) {
				add(square, columnIndex, rowIndex);
				
				columnIndex++;
				
				if (columnIndex == NUM_OF_COLUMNS) {
					columnIndex = 0;
					rowIndex++;
				}
			}

			setOnMouseDragged(t -> {
				if (!dragDetected) {
					dragDetected = true;
					mouseDragColor = getSkinnable().getColor();
				}
				
				int xIndex = clamp(0, (int) t.getX() / (SQUARE_SIZE + 1), NUM_OF_COLUMNS - 1);
				int yIndex = clamp(0, (int) t.getY() / (SQUARE_SIZE + 1), NUM_OF_ROWS - 1);
				int index = xIndex + yIndex * NUM_OF_COLUMNS;

				Color color = squares.get(index).getColor();
				
				getSkinnable().setColor(color);
				updateSelection(color);
			});
			
			addEventHandler(MouseEvent.MOUSE_RELEASED, t -> {
				if (colorPickerGrid.getBoundsInLocal().contains(t.getX(), t.getY())) {
					updateSelection(getSkinnable().getColor());
				}
				else {
					// Restore color as mouse release happened outside the grid.
					if (mouseDragColor != null) {
						getSkinnable().setColor(mouseDragColor);
						updateSelection(mouseDragColor);
					}
				}
				dragDetected = false;
			});
		}

		int clamp(int min, int value, int max) {
			if (value < min) {
				return min;
			}
			if (value > max) {
				return max;
			}
			return value;
		}

		List<ColorSquare> getSquares() {
			return squares;
		}

		@Override
		protected double computePrefWidth(double height) {
			return (SQUARE_SIZE + 1) * NUM_OF_COLUMNS;
		}

		@Override
		protected double computePrefHeight(double width) {
			return (SQUARE_SIZE + 1) * NUM_OF_ROWS;
		}
	}



    private static final double[] RAW_VALUES = {
    		// 1st row
            250, 250, 250,
            245, 245, 245,
            238, 238, 238,
            224, 224, 224,
            189, 189, 189,
            158, 158, 158,
            117, 117, 117,
            97, 97, 97,
            66, 66, 66,
            33, 33, 33,
            
            // 2nd row
            236, 239, 241,
            207, 216, 220,
            176, 190, 197,
            144, 164, 174,
            120, 144, 156,
            96, 125, 139,
            84, 110, 122,
            69, 90, 100,
            55, 71, 79,
            38, 50, 56,
            
            // 3rd row
            255, 235, 238,
            255, 205, 210,
            239, 154, 154,
            229, 115, 115,
            239, 83, 80,
            244, 67, 54,
            229, 57, 53,
            211, 47, 47,
            198, 40, 40,
            183, 28, 28,
            
            // 4th row
            232, 234, 246,
            197, 202, 233,
            159, 168, 218,
            121, 134, 203,
            92, 107, 192,
            63, 81, 181,
            57, 73, 171,
            48, 63, 159,
            40, 53, 147,
            26, 35, 126,
            
            // 5th row
            227, 242, 253,
            187, 222, 251,
            144, 202, 249,
            100, 181, 246,
            66, 165, 245,
            33, 150, 243,
            30, 136, 229,
            25, 118, 210,
            21, 101, 192,
            13, 71, 161,

            // 6th row
            232, 245, 233,
            200, 230, 201,
            165, 214, 167,
            129, 199, 132,
            102, 187, 106,
            76, 175, 80,
            67, 160, 71,
            56, 142, 60,
            46, 125, 50,
            27, 94, 32,

            // 7th row
            255, 253, 231,
            255, 249, 196,
            255, 245, 157,
            255, 241, 118,
            255, 238, 88,
            255, 235, 59,
            253, 216, 53,
            251, 192, 45,
            249, 168, 37,
            245, 127, 23,

            // 8th row
            255, 243, 224,
            255, 224, 178,
            255, 204, 128,
            255, 183, 77,
            255, 167, 38,
            255, 152, 0,
            251, 140, 0,
            245, 124, 0,
            239, 108, 0,
            230, 81, 0,

            // 9th row
            251, 233, 231,
            255, 204, 188,
            255, 171, 145,
            255, 138, 101,
            255, 112, 67,
            255, 87, 34,
            244, 81, 30,
            230, 74, 25,
            216, 67, 21,
            191, 54, 12,

            // 10th row
            239, 235, 233,
            215, 204, 200,
            188, 170, 164,
            161, 136, 127,
            141, 110, 99,
            121, 85, 72,
            109, 76, 65,
            93, 64, 55,
            78, 52, 46,
            62, 39, 35,
    };

	private static final int NUM_OF_COLUMNS = 10;
	private static final int NUM_OF_COLORS = RAW_VALUES.length / 3;
	private static final int NUM_OF_ROWS = NUM_OF_COLORS / NUM_OF_COLUMNS;

}
