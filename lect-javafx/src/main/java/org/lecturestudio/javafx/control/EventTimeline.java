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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import org.lecturestudio.core.model.Time;
import org.lecturestudio.javafx.util.FxStyleablePropertyFactory;
import org.lecturestudio.media.track.EventsTrack;

public class EventTimeline extends MediaTrackControl<EventsTrack> {

	private final static String DEFAULT_STYLE_CLASS = "event-timeline";

	private static final FxStyleablePropertyFactory<EventTimeline> FACTORY =
			new FxStyleablePropertyFactory<>(Control.getClassCssMetaData());

	private final ObjectProperty<Time> duration = new SimpleObjectProperty<>();

	private final StyleableObjectProperty<Paint> background;

	private final StyleableObjectProperty<Paint> textColor;

	private final StyleableObjectProperty<Paint> pageMarkColor;

	private final StyleableObjectProperty<Paint> pageMarkBackground;

	private final StyleableObjectProperty<Paint> eventMarkColor;


	public EventTimeline() {
		initialize();

		background = FACTORY.createPaintProperty(this, "background", "-fx-event-timeline-background", s -> s.background, Color.web("#2D3748"));
		textColor = FACTORY.createPaintProperty(this, "textColor", "-fx-event-timeline-text-color", s -> s.textColor, Color.web("#E2E8F0"));
		pageMarkColor = FACTORY.createPaintProperty(this, "pageMarkColor", "-fx-event-timeline-page-mark-color", s -> s.pageMarkColor, Color.web("#FEB2B2"));
		pageMarkBackground = FACTORY.createPaintProperty(this, "pageMarkBackground", "-fx-event-timeline-page-mark-background", s -> s.pageMarkBackground, Color.web("#1A202C"));
		eventMarkColor = FACTORY.createPaintProperty(this, "eventMarkColor", "-fx-event-timeline-event-mark-color", s -> s.eventMarkColor, Color.web("#F56565"));
	}

	public final Time getDuration() {
		return duration.get();
	}

	public final void setDuration(Time duration) {
		this.duration.set(duration);
	}

	public final ObjectProperty<Time> durationProperty() {
		return duration;
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

	public final void setTextColor(Paint value) {
		textColor.set(value);
	}

	public final Paint getTextColor() {
		return textColor.get();
	}

	public final ObservableValue<Paint> textColorProperty() {
		return textColor;
	}

	public final void setPageMarkColor(Paint value) {
		pageMarkColor.set(value);
	}

	public final Paint getPageMarkColor() {
		return pageMarkColor.get();
	}

	public final ObservableValue<Paint> pageMarkColorProperty() {
		return pageMarkColor;
	}

	public final void setPageMarkBackground(Paint value) {
		pageMarkBackground.set(value);
	}

	public final Paint getPageMarkBackground() {
		return pageMarkBackground.get();
	}

	public final ObservableValue<Paint> pageMarkBackgroundProperty() {
		return pageMarkBackground;
	}

	public final void setEventMarkColor(Paint value) {
		eventMarkColor.set(value);
	}

	public final Paint getEventMarkColor() {
		return eventMarkColor.get();
	}

	public final ObservableValue<Paint> eventMarkColorProperty() {
		return eventMarkColor;
	}

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return FACTORY.getCssMetaData();
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/event-timeline.css").toExternalForm();
	}

	@Override
	protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return getClassCssMetaData();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new EventTimelineSkin(this);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
	}

}
