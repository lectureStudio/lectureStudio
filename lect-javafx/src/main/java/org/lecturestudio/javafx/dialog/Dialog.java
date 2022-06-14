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

package org.lecturestudio.javafx.dialog;

import static java.util.Objects.nonNull;

import java.util.ResourceBundle;

import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.lecturestudio.core.view.View;

public abstract class Dialog extends Stage {

	protected final ResourceBundle resources;


	/**
	 * Creates a root Node for the scene graph.
	 *
	 * @return root The root node of the scene graph.
	 */
	abstract protected Parent createRoot();


	public Dialog(ResourceBundle resources) {
		this(null, resources);
	}

	public Dialog(Window owner) {
		this(owner, null);
	}

	public Dialog(Window owner, ResourceBundle resources) {
		this.resources = resources;

		initStyle(StageStyle.TRANSPARENT);
		initModality(Modality.WINDOW_MODAL);
		initOwner(owner);

		initialize();

		setResizable(false);
	}

	@Override
	public void close() {
		Event.fireEvent(this, new WindowEvent(this, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	public void setParent(View parent) {
		if (!Node.class.isAssignableFrom(parent.getClass())) {
			throw new IllegalArgumentException("View expected to be a JavaFX Node");
		}

		Window window = ((Node) parent).getScene().getWindow();

		initOwner(window);
		updateOwner(window);
	}

	/**
	 * Meant to be overridden by subclasses to use the Scene before it is shown.
	 */
	protected void onSceneCreated() {

	}

	/**
	 * Can be overridden to prevent closing on an shortcut event.
	 */
	protected void onShortcutClose() {
		close();
	}

	private void initialize() {
		Parent root = createRoot();
		Scene scene = new Scene(root);

		if (getStyle() == StageStyle.TRANSPARENT) {
			scene.setFill(null);
		}

		setScene(scene);

		initializeScene();

		onSceneCreated();
	}

	private void initializeScene() {
		if (nonNull(getOwner())) {
			updateOwner(getOwner());
		}

		getScene().setOnKeyPressed(event -> {
			switch (event.getCode()) {
				// Exit on escape and enter key.
				case ESCAPE, ENTER -> onShortcutClose();
				default -> {
				}
			}
		});
		getScene().setOnMouseClicked(event -> {
			if (event.getTarget() != getScene().getRoot()) {
				return;
			}

			// Exit on mouse click.
			onShortcutClose();
		});
	}

	private void updateOwner(Window owner) {
		Parent ownerRoot = owner.getScene().getRoot();
		Bounds ownerRootBounds = ownerRoot.localToScreen(ownerRoot.getLayoutBounds());

		// Set dialog bounds to the same bounds of the owner window root node.
		setX(ownerRootBounds.getMinX());
		setY(ownerRootBounds.getMinY());
		setWidth(ownerRootBounds.getWidth());
		setHeight(ownerRootBounds.getHeight());

		getScene().getRoot().setStyle(ownerRoot.getStyle());
	}
}
