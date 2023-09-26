package org.lecturestudio.javafx.factory;

import java.util.ResourceBundle;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import org.lecturestudio.core.tool.StrokeWidthSettings;

public class StrokeWidthCellFactory implements Callback<ListView<StrokeWidthSettings>, ListCell<StrokeWidthSettings>> {

	private final ResourceBundle resourceBundle;


	public StrokeWidthCellFactory(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	@Override
	public ListCell<StrokeWidthSettings> call(ListView<StrokeWidthSettings> param) {
		return new StrokeWidthListCell(resourceBundle);
	}
}
