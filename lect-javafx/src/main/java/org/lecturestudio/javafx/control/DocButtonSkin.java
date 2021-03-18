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

import static java.util.Objects.nonNull;

import java.text.SimpleDateFormat;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.skin.ButtonSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.lecturestudio.core.model.RecentDocument;

public class DocButtonSkin extends ButtonSkin {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yy hh:mm");

	private final ChangeListener<Node> iconListener = (observable, oldIcon, newIcon) -> {
		setIcon(oldIcon, newIcon);
	};

	private HBox bottomBox;

	private Label nameLabel;
	
	private Label pathLabel;
	
	private Label dateLabel;
	

	protected DocButtonSkin(DocButton control) {
		super(control);
		
		initialize(control);
	}

	/** {@inheritDoc} */
	@Override
	public void dispose() {
		DocButton button = (DocButton) getSkinnable();
		button.iconProperty().removeListener(iconListener);

		super.dispose();
	}

	private void initialize(DocButton control) {
		nameLabel = new Label();
		pathLabel = new Label();
		dateLabel = new Label();

		nameLabel.getStyleClass().add("doc-name");
		pathLabel.getStyleClass().add("doc-path");
		dateLabel.getStyleClass().add("doc-date");

		pathLabel.setAlignment(Pos.TOP_LEFT);
		pathLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
		pathLabel.setMaxHeight(Double.MAX_VALUE);

		dateLabel.setMaxWidth(Double.MAX_VALUE);

		bottomBox = new HBox();
		bottomBox.getChildren().add(dateLabel);

		setDocument(control.getDocument());
		setIcon(null, control.getIcon());

		HBox.setHgrow(dateLabel, Priority.ALWAYS);
		VBox.setVgrow(pathLabel, Priority.ALWAYS);

		VBox content = new VBox();
		content.getChildren().addAll(nameLabel, pathLabel, bottomBox);

		control.iconProperty().addListener(iconListener);
		control.setGraphic(content);

		registerChangeListener(control.documentProperty(), observableValue -> setDocument(control.getDocument()));
	}

	private void setDocument(RecentDocument doc) {
		if (doc == null) {
			nameLabel.setText(null);
			pathLabel.setText(null);
			dateLabel.setText(null);
		}
		else {
			nameLabel.setText(doc.getDocumentName());
			pathLabel.setText(doc.getDocumentPath());
			dateLabel.setText(DATE_FORMATTER.format(doc.getLastModified()));
		}
	}

	private void setIcon(Node oldIcon, Node newIcon) {
		if (nonNull(oldIcon)) {
			bottomBox.getChildren().remove(oldIcon);
		}
		if (nonNull(newIcon)) {
			bottomBox.getChildren().add(newIcon);
		}
	}

}
