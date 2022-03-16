/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import static java.util.Objects.nonNull;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SponsorSkin extends SkinBase<Sponsor> {

	/**
	 * Constructor for all SponsorSkin instances.
	 *
	 * @param control The control for which this Skin should attach to.
	 */
	public SponsorSkin(Sponsor control) {
		super(control);

		initialize(control);
	}

	private void initialize(Sponsor control) {
		EventHandler<ActionEvent> linkHandler = event -> {
			Stage window = (Stage) control.getScene().getWindow();
			HostServices hostServices = (HostServices) window.getProperties().get("hostServices");

			if (nonNull(hostServices)) {
				Hyperlink link = (Hyperlink) event.getSource();
				hostServices.showDocument((String) link.getUserData());
			}
		};

		VBox pane = new VBox();
		pane.getStyleClass().add("sponsor-container");

		Label organizationLabel = new Label(control.getOrganization());
		organizationLabel.getStyleClass().add("sponsor-organization");

		Hyperlink linkLabel = new Hyperlink(control.getOrganizationLinkName());
		linkLabel.getStyleClass().add("sponsor-link");
		linkLabel.setUserData(control.getOrganizationLinkUrl());
		linkLabel.setOnAction(linkHandler);

		var imageStream = getClass().getResourceAsStream(
				"/resources/gfx/icons/" + control.getOrganizationImage());

		ImageView imageView = new ImageView();
		imageView.getStyleClass().add("sponsor-image");
		imageView.setImage(new Image(imageStream));
		imageView.setFitHeight(50);
		imageView.setPreserveRatio(true);

		pane.getChildren().addAll(organizationLabel, linkLabel, imageView);

		getChildren().add(pane);
	}
}
