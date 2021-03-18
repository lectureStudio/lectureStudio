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

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.util.converter.DateStringConverter;

public class MessageViewSkin extends SkinBase<MessageView> {

	private Label dateLabel;

	private Label hostLabel;

	private ScrollPane messagePane;

	private Region icon;


	protected MessageViewSkin(MessageView control) {
		super(control);

		initLayout(control);
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return getSkinnable().getMinWidth();
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		double height = Math.max(hostLabel.prefHeight(-1), dateLabel.prefHeight(-1)) + messagePane.prefHeight(-1);

		// Add margin to avoid the vertical scrollbar.
		height += snapSpaceX(messagePane.getInsets().getTop() + messagePane.getInsets().getBottom());
		height += snapSpaceY(messagePane.getPadding().getTop() + messagePane.getPadding().getBottom());

		return topInset + height + bottomInset;
	}

	@Override
	protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
		final double iconWidth = snapSizeX(icon.prefWidth(-1));
		final double iconHeight = snapSizeY(icon.prefHeight(-1));

		layoutInArea(hostLabel, contentX + iconWidth, contentY, contentWidth, contentHeight, -1, HPos.LEFT, VPos.TOP);
		layoutInArea(dateLabel, contentX, contentY, contentWidth, contentHeight, -1, HPos.RIGHT, VPos.TOP);

		double headHight = Math.max(Math.max(hostLabel.prefHeight(-1), dateLabel.prefHeight(-1)), iconHeight);
		double messageY = headHight + contentY;

		icon.resize(iconWidth, iconHeight);

		messagePane.setPrefWidth(contentWidth);

		layoutInArea(icon, snapPositionX(contentX), snapPositionY(contentY), iconWidth, iconHeight, -1, HPos.LEFT, VPos.TOP);
		layoutInArea(messagePane, contentX, messageY, contentWidth, contentHeight - messageY, -1, HPos.LEFT, VPos.TOP);
	}

	private void initLayout(MessageView control) {
		dateLabel = new Label();
		dateLabel.getStyleClass().add("date");

		hostLabel = new Label(control.getHost());
		hostLabel.getStyleClass().add("host");
		hostLabel.setVisible(false);
		hostLabel.setManaged(false);

		Label messageLabel = new Label(control.getMessage());
		messageLabel.setWrapText(true);
		messageLabel.textProperty().bind(control.messageProperty());

		messagePane = new ScrollPane();
		messagePane.getStyleClass().setAll("message");
		messagePane.setFitToWidth(true);
		messagePane.setContent(messageLabel);

		icon = new Region();
		icon.getStyleClass().setAll("icon");
		icon.setOnMousePressed(event -> {
			hostLabel.setVisible(!hostLabel.isVisible());
			hostLabel.setManaged(!hostLabel.isVisible());
		});

		dateLabel.textProperty().bindBidirectional(control.dateProperty(), new DateStringConverter(control.getDateFormat()));
		hostLabel.textProperty().bind(control.hostProperty());

		getChildren().addAll(icon, hostLabel, dateLabel, messagePane);
	}

}
