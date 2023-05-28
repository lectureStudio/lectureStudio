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
import java.util.Objects;
import java.util.function.Consumer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.javafx.util.FxStyleablePropertyFactory;
import org.lecturestudio.media.track.EventsTrack;

public class EventTimeline extends MediaTrackControlBase<EventsTrack> {

	private static final String DEFAULT_STYLE_CLASS = "event-timeline-pane";

	private static final FxStyleablePropertyFactory<EventTimeline> FACTORY =
			new FxStyleablePropertyFactory<>(Control.getClassCssMetaData());

	private final ObjectProperty<Time> duration = new SimpleObjectProperty<>();

	private Consumer<Time> showTimeCallback;

	private ConsumerAction<RecordedPage> onMovePageAction;

	private ConsumerAction<RecordedPage> onHidePageAction;

	private ConsumerAction<RecordedPage> onHideAndMoveNextPageAction;


	public EventTimeline() {
		initialize();
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

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return FACTORY.getCssMetaData();
	}

	@Override
	public String getUserAgentStylesheet() {
		return Objects.requireNonNull(getClass().getResource("/resources/css/event-timeline.css")).toExternalForm();
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

	public void setShowTimeCallback(Consumer<Time> showTimeCallback) {
		this.showTimeCallback = showTimeCallback;
	}

	public Consumer<Time> getShowTimeCallback() {
		return this.showTimeCallback;
	}

	void setOnMovePage(ConsumerAction<RecordedPage> action) {
		this.onMovePageAction = action;
	}

	public ConsumerAction<RecordedPage> getOnMovePage() {
		return this.onMovePageAction;
	}

	void setOnHidePage(ConsumerAction<RecordedPage> action) {
		this.onHidePageAction = action;
	}

	public ConsumerAction<RecordedPage> getOnHidePage() {
		return this.onHidePageAction;
	}

	void setOnHideAndMoveNextPage(ConsumerAction<RecordedPage> action) {
		this.onHideAndMoveNextPageAction = action;
	}

	public ConsumerAction<RecordedPage> getOnHideAndMoveNextPage() {
		return this.onHideAndMoveNextPageAction;
	}

}
