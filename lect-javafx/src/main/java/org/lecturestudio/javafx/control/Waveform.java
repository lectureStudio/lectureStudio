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

import java.util.List;

import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import org.lecturestudio.javafx.util.FxStyleablePropertyFactory;
import org.lecturestudio.media.track.AudioTrack;

public class Waveform extends MediaTrackControlBase<AudioTrack> {

	private final static String DEFAULT_STYLE_CLASS = "waveform";

	private static final FxStyleablePropertyFactory<Waveform> FACTORY =
			new FxStyleablePropertyFactory<>(Control.getClassCssMetaData());

	private final StyleableObjectProperty<Paint> background;

	private final StyleableObjectProperty<Paint> wave;

	private final StyleableObjectProperty<Paint> waveCenter;


	public Waveform() {
		initialize();

		background = FACTORY.createPaintProperty(this, "background", "-fx-waveform-background", s -> s.background, Color.web("#2D3748"));
		wave = FACTORY.createPaintProperty(this, "wave", "-fx-waveform-color", s -> s.wave, Color.web("#2B6CB0"));
		waveCenter = FACTORY.createPaintProperty(this, "waveCenter", "-fx-waveform-center", s -> s.waveCenter, Color.web("#2C5282"));
	}

	public final void setBackgroundColor(Paint value) {
		background.set(value);
	}

	public final Paint getBackgroundColor() {
		return background.get();
	}

	public final ObservableValue<Paint> backgroundColorProperty() {
		return background;
	}

	public final void setWaveColor(Paint value) {
		wave.set(value);
	}

	public final Paint getWaveColor() {
		return wave.get();
	}

	public final ObservableValue<Paint> waveColorProperty() {
		return wave;
	}

	public final void setWaveCenterColor(Paint value) {
		waveCenter.set(value);
	}

	public final Paint getWaveCenterColor() {
		return waveCenter.get();
	}

	public final ObservableValue<Paint> waveCenterColorProperty() {
		return waveCenter;
	}

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return FACTORY.getCssMetaData();
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/waveform.css").toExternalForm();
	}

	@Override
	protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return getClassCssMetaData();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new WaveformSkin(this);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
	}

}
