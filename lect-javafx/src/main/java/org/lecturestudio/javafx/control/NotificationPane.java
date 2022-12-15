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

import java.util.ResourceBundle;

import javafx.beans.DefaultProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.javafx.util.FxUtils;

@DefaultProperty("buttons")
public class NotificationPane extends StackPane {

	private final ObjectProperty<NotificationType> type = new SimpleObjectProperty<>(NotificationType.DEFAULT);

	protected final ResourceBundle resources;

	@FXML
	private Pane contentPane;

	@FXML
	private SvgIcon errorIcon;

	@FXML
	private SvgIcon questionIcon;

	@FXML
	private SvgIcon warningIcon;

	@FXML
	private Label iconLabel;

	@FXML
	private Label titleLabel;

	@FXML
	private Label messageLabel;

	@FXML
	private HBox buttons;


	public NotificationPane(ResourceBundle resources) {
		super();

		this.resources = resources;

		load(resources);
	}

	public final NotificationType getType() {
		return type.get();
	}

	public final void setType(NotificationType type) {
		FxUtils.invoke(() -> {
			this.type.set(type);
		});
	}

	public final void setIcon(Node node) {
		FxUtils.invoke(() -> {
			boolean visible = nonNull(node);

			iconLabel.setGraphic(node);
			iconLabel.setManaged(visible);
			iconLabel.setVisible(visible);
		});
	}

	public final void setTitle(String title) {
		FxUtils.invoke(() -> {
			titleLabel.setText(title);
		});
	}

	public final void setMessage(String message) {
		FxUtils.invoke(() -> {
			boolean visible = nonNull(message) && !message.isEmpty() && !message.isBlank();

			messageLabel.setText(message);
			messageLabel.setManaged(visible);
			messageLabel.setVisible(visible);
		});
	}

	public void setContentWidth(double width) {
		FxUtils.invoke(() -> {
			contentPane.setPrefWidth(width);
			contentPane.setMaxWidth(width);

			titleLabel.setMinWidth(-1);
			titleLabel.setWrapText(true);
		});
	}

	public final ObservableList<Node> getButtons() {
		return buttons.getChildren();
	}

	public final ObjectProperty<NotificationType> typeProperty() {
		return type;
	}

	protected final ResourceBundle getResourceBundle() {
		return resources;
	}

	@FXML
	protected void initialize() {
		type.addListener((observable, oldType, newType) -> {
			updateType(newType);
		});

		BooleanBinding nonEmptyBinding = Bindings.size(buttons.getChildren()).greaterThan(0);

		buttons.visibleProperty().bind(nonEmptyBinding);
		buttons.managedProperty().bind(nonEmptyBinding);

		updateType(getType());
	}

	private void updateType(NotificationType type) {
		switch (type) {
			case DEFAULT -> setIcon(null);
			case ERROR -> setIcon(errorIcon);
			case QUESTION -> setIcon(questionIcon);
			case WARNING -> setIcon(warningIcon);
			case WAITING -> {
				ProgressIndicator indicator = new ProgressIndicator();
				indicator.setProgress(-1);
				setIcon(indicator);
			}
			default -> {
			}
		}
	}

	private void load(ResourceBundle resources) {
		FxUtils.load("/resources/fxml/notification.fxml", resources, this, this);
	}

}
