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

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.PaintConverter;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import org.lecturestudio.javafx.util.CssHelper;

public class LevelMeter extends Control {

	private static final String DEFAULT_STYLE_CLASS = "level-meter";

	private final DoubleProperty level = new SimpleDoubleProperty();

	/** The orientation of the LevelMeter dictates whether it shows the level vertically or horizontally. */
	private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>();

	private StyleableObjectProperty<Paint> backgroundFill;

	private StyleableObjectProperty<Paint> levelFill;


	public LevelMeter() {
		initialize();
	}

	public DoubleProperty levelProperty() {
		return level;
	}

	public double getLevel() {
		return levelProperty().get();
	}

	public void setLevel(double level) {
		levelProperty().set(level);
	}

	public final void setOrientation(Orientation value) {
		orientationProperty().set(value);
	}

	public final Orientation getOrientation() {
		return orientationProperty().get();
	}

	public final ObjectProperty<Orientation> orientationProperty() {
		return orientation;
	}

	public Paint getBackgroundFill() {
		return backgroundFill == null ? Color.GRAY : backgroundFill.get();
	}

	public void setBackgroundFill(Paint backgroundFill) {
		backgroundFillProperty().set(backgroundFill);
	}

	public StyleableObjectProperty<Paint> backgroundFillProperty() {
		if (isNull(backgroundFill)) {
			backgroundFill = CssHelper.createProperty(StyleableProperties.BACKGROUND_FILL, LevelMeter.this, "backgroundFill");
		}
		return backgroundFill;
	}

	public Paint getLevelFill() {
		return levelFill == null ? Color.CORNFLOWERBLUE : levelFill.get();
	}

	public void setLevelFill(Paint levelFill) {
		levelFillProperty().set(levelFill);
	}

	public StyleableObjectProperty<Paint> levelFillProperty() {
		if (isNull(levelFill)) {
			levelFill = CssHelper.createProperty(StyleableProperties.LEVEL_FILL, LevelMeter.this, "levelFill");
		}
		return levelFill;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/level-meter.css").toExternalForm();
	}

	@Override
	public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return getClassCssMetaData();
	}

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.STYLEABLES;
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new LevelMeterSkin(this);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
	}


	private static class StyleableProperties {

		private static final CssMetaData<LevelMeter, Paint> BACKGROUND_FILL = new CssMetaData<LevelMeter, Paint>("-fx-background-fill",
				PaintConverter.getInstance(), Color.GRAY) {

			@Override
			public boolean isSettable(LevelMeter control) {
				return control.backgroundFill == null || !control.backgroundFill.isBound();
			}

			@Override
			public StyleableProperty<Paint> getStyleableProperty(LevelMeter control) {
				return control.backgroundFillProperty();
			}
		};

		private static final CssMetaData<LevelMeter, Paint> LEVEL_FILL = new CssMetaData<LevelMeter, Paint>("-fx-level-fill",
				PaintConverter.getInstance(), Color.CORNFLOWERBLUE) {

			@Override
			public boolean isSettable(LevelMeter control) {
				return control.levelFill == null || !control.levelFill.isBound();
			}

			@Override
			public StyleableProperty<Paint> getStyleableProperty(LevelMeter control) {
				return control.levelFillProperty();
			}
		};

		private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

		static {
			final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<CssMetaData<? extends Styleable, ?>>(Control.getClassCssMetaData());

			Collections.addAll(styleables, BACKGROUND_FILL, LEVEL_FILL);

			STYLEABLES = Collections.unmodifiableList(styleables);
		}
	}

}
