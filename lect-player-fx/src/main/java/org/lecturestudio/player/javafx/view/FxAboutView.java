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

package org.lecturestudio.player.javafx.view;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

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
	private TableView<SystemPropertyTableItem> systemPropertiesTable;

	@FXML
	private Pane contributorList;

	@FXML
	private Pane sponsorList;

	@FXML
	private Button closeButton;


	public FxAboutView() {
		super();
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
				Label label = new Label(sponsor.name);

				sponsorList.getChildren().add(label);
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

}
