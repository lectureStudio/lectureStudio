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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.javafx.event.ScreenActionEvent;

public class ScreenViewSkin extends SkinBase<ScreenView> {

	private final Rectangle2D virtualArea;

	private final List<ScreenRect> rectList;


	/**
	 * Creates a new ScreenViewSkin.
	 *
	 * @param control The control for which this Skin should attach to.
	 */
	ScreenViewSkin(ScreenView control) {
		super(control);

		virtualArea = new Rectangle2D();
		rectList = new ArrayList<>();

		initLayout(control);
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return leftInset + 50 + rightInset;
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return topInset + 50 + bottomInset;
	}

	@Override
	protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
		double scale = contentHeight / virtualArea.getHeight();

		if (virtualArea.getWidth() * scale > contentWidth) {
			scale = contentWidth / virtualArea.getWidth();
		}

		// Virtual screen space to node space.
		double vX = virtualArea.getX() * scale;
		double vY = virtualArea.getY() * scale;
		double vWidth = virtualArea.getWidth() * scale;
		double vHeight = virtualArea.getHeight() * scale;

		double centerX = (contentWidth - vWidth) / 2 - vX + snappedLeftInset();
		double centerY = (contentHeight - vHeight) / 2 - vY + snappedTopInset();

		for (Node node : rectList) {
			ScreenRect screenRect = (ScreenRect) node;
			Rectangle2D bounds = screenRect.getScreenBounds();

			double x = bounds.getX() * scale;
			double y = bounds.getY() * scale;
			double w = bounds.getWidth() * scale;
			double h = bounds.getHeight() * scale;

			screenRect.setX(x + centerX);
			screenRect.setY(y + centerY);
			screenRect.setWidth(w);
			screenRect.setHeight(h);
		}
	}

	private void initLayout(ScreenView control) {
		ObservableList<Screen> screens = control.getScreens();
		screens.addListener((InvalidationListener) c -> {
			rectList.clear();
			getChildren().clear();

			loadScreens(screens);
		});

		loadScreens(screens);
	}

	private void loadScreens(List<Screen> screens) {
		virtualArea.setRect(0, 0, 0, 0);

		int count = screens.size();

		for (int i = 0; i < count; i++) {
			Screen screen = screens.get(i);

			Text idText = createIdText(i);
			Text ratioText = createRatioText(screen);

			ScreenRect rect = new ScreenRect(screen, idText, ratioText);
			rect.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
				getSkinnable().getOnAction().handle(new ScreenActionEvent(screen));
			});

			getChildren().addAll(rect, idText, ratioText);

			rectList.add(rect);

			virtualArea.union(screen.getBounds());
		}
	}

	private Text createIdText(int screenNumber) {
		Text text = new Text(Integer.toString(screenNumber));
		text.getStyleClass().add("screen-number-text");
		text.setTextOrigin(VPos.TOP);
		text.setManaged(false);
		text.setMouseTransparent(true);

		return text;
	}

	private Text createRatioText(Screen screen) {
		Rectangle2D bounds = screen.getBounds();

		BigInteger bW = BigInteger.valueOf((long) bounds.getWidth());
		BigInteger bH = BigInteger.valueOf((long) bounds.getHeight());

		int gcd = bW.gcd(bH).intValue();

		int ratioX = (int) (bounds.getWidth() / gcd);
		int ratioY = (int) (bounds.getHeight() / gcd);

		Text text = new Text(String.format("%d:%d", ratioX, ratioY));
		text.getStyleClass().add("screen-ratio-text");
		text.setBoundsType(TextBoundsType.VISUAL);
		text.setTextOrigin(VPos.TOP);
		text.setManaged(false);
		text.setMouseTransparent(true);

		return text;
	}



	private static class ScreenRect extends Rectangle {

		private final Screen screen;

		private final Text idText;

		private final Text ratioText;


		ScreenRect(Screen screen, Text idText, Text ratioText) {
			getStyleClass().add("screen-rect");

			this.screen = screen;
			this.idText = idText;
			this.ratioText = ratioText;

			xProperty().addListener(observable -> updateTextPos());
			yProperty().addListener(observable -> updateTextPos());
			widthProperty().addListener(observable -> updateTextPos());
			heightProperty().addListener(observable -> updateTextSize());
		}

		Rectangle2D getScreenBounds() {
			return screen.getBounds();
		}

		private void updateTextSize() {
			// Scale in percent.
			double textScale = getLayoutBounds().getHeight() * 0.01;

			idText.setScaleX(textScale);
			idText.setScaleY(textScale);

			ratioText.setScaleX(textScale);
			ratioText.setScaleY(textScale);

			updateTextPos();
		}

		private void updateTextPos() {
			// Center id number text.
			double x = getX() + (getWidth() - idText.getLayoutBounds().getWidth()) / 2;
			double y = getY() + (getHeight() - idText.getLayoutBounds().getHeight()) / 2;

			idText.setX(x);
			idText.setY(y);

			// Align the ratio text to the top-left corner.
			x = getX() + ratioText.getLayoutBounds().getWidth() / 2 * (ratioText.getScaleX() - 1);
			y = getY() + ratioText.getLayoutBounds().getHeight() / 2 * (ratioText.getScaleY() - 1);
			// Padding.
			x += 5 * ratioText.getScaleX();
			y += 5 * ratioText.getScaleX();

			ratioText.setX(x);
			ratioText.setY(y);
		}
	}
}
