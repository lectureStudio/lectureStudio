package org.lecturestudio.javafx.factory;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ResourceBundle;

import javafx.scene.control.ListCell;

import org.lecturestudio.core.tool.StrokeWidthSettings;
import org.lecturestudio.javafx.control.SvgIcon;

public class StrokeWidthListCell extends ListCell<StrokeWidthSettings> {

	private final ResourceBundle resourceBundle;
	SvgIcon icon = new SvgIcon();

	public StrokeWidthListCell(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	@Override
	public void updateItem(StrokeWidthSettings item, boolean empty) {
		super.updateItem(item, empty);

		if (nonNull(item)) {

			switch (item) {
				case EXTRA_SMALL -> {
					setText(resourceBundle.getString("stroke.extra_small"));
					icon.setContent("M199-199q-6-6-6-14t6-14l534-534q6-6 14-6t14 6q6 6 6 14t-6 14L227-199q-6 6-14 6t-14-6Z");
				}
				case SMALL -> {
					setText(resourceBundle.getString("stroke.small"));
					icon.setContent("M212-212q-9-9-9-21t9-21l494-494q8-8 20.5-8.5T748-748q9 9 9 21t-9 21L254-212q-9 9-21 9t-21-9Z");
				}
				case NORMAL -> {
					setText(resourceBundle.getString("stroke.medium"));
					icon.setContent("M218-218q-14-15-14-35.5t14-34.5l454-454q14-14 35-14t35 14q14 14 14 35t-14 35L288-218q-14 14-35 14t-35-14Z");
				}
				case BIG -> {
					setText(resourceBundle.getString("stroke.big"));
					icon.setContent("M236-236.141q-26-26.141-26-64T236-364l360-360q26.177-26 64.089-26Q698-750 724-723.859t26 64Q750-622 724-596L364-236q-26.177 26-64.089 26Q262-210 236-236.141Z");
				}
				case EXTRA_BIG -> {
					setText(resourceBundle.getString("stroke.extra_big"));
					icon.setContent("M242-242.118q-32-32.117-32-78Q210-366 242-398l320-320q32.148-32 78.074-32Q686-750 718-717.882q32 32.117 32 78Q750-594 718-562L398-242q-32.148 32-78.074 32Q274-210 242-242.118Z");
				}
			}

			setGraphic(icon);
		}
		if (isNull(item) || empty) {
			setText("");
			setGraphic(null);
		}
	}
}
