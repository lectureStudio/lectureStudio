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

package org.lecturestudio.javafx.window;

import static java.util.Objects.nonNull;

import java.awt.image.BufferedImage;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.CameraPresentationView;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.core.view.ScreenViewType;
import org.lecturestudio.core.view.SlideViewOverlay;
import org.lecturestudio.javafx.beans.converter.ColorConverter;
import org.lecturestudio.javafx.control.CameraCanvas;
import org.lecturestudio.javafx.util.FxUtils;

public class CameraWindow extends Stage implements CameraPresentationView {

	private Action visibleAction;

	/** Camera image painter. */
	private CameraCanvas canvas;


	public CameraWindow(Screen screen) {
		initialize(screen);
	}

	@Override
	public void setImage(BufferedImage image) {
		FxUtils.invoke(() -> canvas.setImage(image));
	}

	@Override
	public ScreenViewType getType() {
		return ScreenViewType.CAMERA;
	}

	@Override
	public void setBackgroundColor(Color color) {
		BackgroundFill backgroundFill = new BackgroundFill(ColorConverter.INSTANCE.to(color), CornerRadii.EMPTY, Insets.EMPTY);

		canvas.setBackground(new Background(backgroundFill));
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			super.show();
		}
		else {
			super.hide();
		}
	}

	@Override
	public boolean isVisible() {
		return super.isShowing();
	}

	@Override
	public void setOnVisible(Action action) {
		this.visibleAction = action;
	}

	@Override
	public void addOverlay(SlideViewOverlay overlay) {

	}

	@Override
	public void removeOverlay(SlideViewOverlay overlay) {

	}

	private void initialize(Screen screen) {
		initStyle(StageStyle.TRANSPARENT);
		initModality(Modality.NONE);

		setOnShown(event -> {
			if (nonNull(visibleAction)) {
				visibleAction.execute();
			}
		});

		canvas = new CameraCanvas();

		StackPane root = new StackPane();
		root.getChildren().add(canvas);

		Scene scene = new Scene(root);

		setScene(scene);
		setResizable(false);
		setAlwaysOnTop(true);

		// Set bounds to the same of the screen dimensions.
		Rectangle2D bounds = screen.getBounds();

		setX(bounds.getX());
		setY(bounds.getY());
		setWidth(bounds.getWidth());
		setHeight(bounds.getHeight());
	}
}
