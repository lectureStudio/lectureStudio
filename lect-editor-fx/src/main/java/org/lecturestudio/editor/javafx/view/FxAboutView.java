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

package org.lecturestudio.editor.javafx.view;

import static java.util.Objects.nonNull;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.lecturestudio.core.model.Contributor;
import org.lecturestudio.core.model.Sponsor;
import org.lecturestudio.core.view.AboutView;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.layout.ContentPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.javafx.view.model.SystemPropertyTableItem;

@FxmlView(name = "about")
public class FxAboutView extends ContentPane implements AboutView {

	@FXML
	private Label versionLabel;

	@FXML
	private Label buildDateLabel;

	@FXML
	private Hyperlink websiteLink;

	@FXML
	private Hyperlink issueLink;

	@FXML
	private TableView<SystemPropertyTableItem> systemPropertiesTable;

	@FXML
	private Pane contributorList;

	@FXML
	private Pane sponsorsList;

	@FXML
	private Button closeButton;


	public FxAboutView() {
		super();
	}

	@Override
	public void setAppName(String name) {
		FxUtils.invoke(() -> {
			setTitle(getTitle() + " " + name);
		});
	}

	@Override
	public void setAppVersion(String version) {
		FxUtils.invoke(() -> {
			versionLabel.setText(version);
		});
	}

	@Override
	public void setAppBuildDate(String date) {
		FxUtils.invoke(() -> {
			buildDateLabel.setText(date);
		});
	}

	@Override
	public void setWebsite(String website) {
		FxUtils.invoke(() -> {
			websiteLink.setUserData(website);
			websiteLink.setText(website);
		});
	}

	@Override
	public void setIssueWebsite(String website) {
		FxUtils.invoke(() -> {
			issueLink.setUserData(website);
		});
	}

	@Override
	public void setContributors(List<Contributor> contributors) {
		FxUtils.invoke(() -> {
			for (Contributor contributor : contributors) {
				org.lecturestudio.javafx.control.Contributor c = new org.lecturestudio.javafx.control.Contributor();
				c.setFirm(contributor.firm);
				c.setName(contributor.name);

				contributorList.getChildren().add(c);
			}
		});
	}

	@Override
	public void setSponsors(List<Sponsor> sponsors) {
		FxUtils.invoke(() -> {
			for (Sponsor sponsor : sponsors) {
				org.lecturestudio.javafx.control.Sponsor s = new org.lecturestudio.javafx.control.Sponsor();
				s.setOrganization(sponsor.organization);
				s.setOrganizationLink(sponsor.link.name, sponsor.link.url);
				s.setOrganizationImage(sponsor.logo);

				sponsorsList.getChildren().add(s);
			}
		});
	}

	@Override
	public void setProperties(Properties properties) {
		FxUtils.invoke(() -> {
			Enumeration<?> enumeration = properties.propertyNames();

			while (enumeration.hasMoreElements()) {
				String key = (String) enumeration.nextElement();
				String value = (String) properties.get(key);

				systemPropertiesTable.getItems().add(new SystemPropertyTableItem(key, value));
			}
		});
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@FXML
	private void initialize() {
		EventHandler<ActionEvent> linkHandler = event -> {
			Stage window = (Stage) getScene().getWindow();
			HostServices hostServices = (HostServices) window.getProperties().get("hostServices");

			if (nonNull(hostServices)) {
				Hyperlink link = (Hyperlink) event.getSource();
				hostServices.showDocument((String) link.getUserData());
			}
		};

		websiteLink.setOnAction(linkHandler);
		issueLink.setOnAction(linkHandler);
	}
}
