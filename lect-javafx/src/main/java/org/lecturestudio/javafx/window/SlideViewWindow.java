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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.core.view.ScreenViewType;
import org.lecturestudio.core.view.SlidePresentationView;
import org.lecturestudio.core.view.SlideViewOverlay;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.javafx.beans.converter.ColorConverter;
import org.lecturestudio.javafx.control.SlideView;
import org.lecturestudio.javafx.util.FxUtils;

public class SlideViewWindow extends Stage implements SlidePresentationView {

	private Action visibleAction;

	private SlideView slideView;


	public SlideViewWindow(Screen screen, RenderController renderController) {
		initialize(screen, renderController);
	}

	@Override
	public void setPage(Page page) {
		FxUtils.invoke(() -> {
			slideView.setPage(page);
		});
	}

	@Override
	public void setPage(Page page, PresentationParameter parameter) {
		FxUtils.invoke(() -> {
			slideView.parameterChanged(page, parameter);
			slideView.setPage(page);
		});
	}

	@Override
	public ScreenViewType getType() {
		return ScreenViewType.SLIDE;
	}

	@Override
	public void setBackgroundColor(Color color) {
		FxUtils.invoke(() -> {
			BackgroundFill backgroundFill = new BackgroundFill(ColorConverter.INSTANCE.to(color), CornerRadii.EMPTY, Insets.EMPTY);

			slideView.setBackground(new Background(backgroundFill));
		});
	}

	@Override
	public void setVisible(boolean visible) {
		FxUtils.invoke(() -> {
			if (visible) {
				super.show();
			}
			else {
				super.hide();
			}
		});
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
		slideView.addOverlay(overlay);
	}

	@Override
	public void removeOverlay(SlideViewOverlay overlay) {
		slideView.removeOverlay(overlay);
	}

	private void initialize(Screen screen, RenderController renderController) {
		// Stage objects must be constructed and modified on the JavaFX Application Thread.

		initStyle(StageStyle.UNDECORATED);
		initModality(Modality.NONE);

		setResizable(false);
		setAlwaysOnTop(true);
		setOnShown(event -> {
			if (nonNull(visibleAction)) {
				visibleAction.execute();
			}
		});

		slideView = new SlideView();
		slideView.setPageRenderer(renderController);
		slideView.setViewType(ViewType.Presentation);
		slideView.setAlignment(Pos.CENTER);
		slideView.setFocusTraversable(false);

		StackPane root = new StackPane();
		root.getChildren().add(slideView);

		setScene(new Scene(root));

		// Set bounds to the same of the screen dimensions.
		Rectangle2D bounds = screen.getBounds();

		setX(bounds.getX());
		setY(bounds.getY());
		setWidth(bounds.getWidth());
		setHeight(bounds.getHeight());
	}

}
